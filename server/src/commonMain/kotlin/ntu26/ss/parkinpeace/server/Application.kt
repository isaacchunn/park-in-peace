package ntu26.ss.parkinpeace.server

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock
import ntu26.ss.parkinpeace.server.api.datagovsg.DatagovsgApi
import ntu26.ss.parkinpeace.server.api.datagovsg.DatagovsgApiV1Impl
import ntu26.ss.parkinpeace.server.api.lta.LtaApi
import ntu26.ss.parkinpeace.server.api.lta.LtaApiImpl
import ntu26.ss.parkinpeace.server.api.mapbox.MapBoxApi
import ntu26.ss.parkinpeace.server.api.mapbox.MapBoxApiImpl
import ntu26.ss.parkinpeace.server.api.onemap.OneMapApi
import ntu26.ss.parkinpeace.server.api.onemap.OneMapApiImpl
import ntu26.ss.parkinpeace.server.api.ura.UraApi
import ntu26.ss.parkinpeace.server.api.ura.UraApiImpl
import ntu26.ss.parkinpeace.server.controllers.CarparkController
import ntu26.ss.parkinpeace.server.data.RawDbCarpark
import ntu26.ss.parkinpeace.server.data.db.*
import ntu26.ss.parkinpeace.server.data.repositories.AvailabilityRepository
import ntu26.ss.parkinpeace.server.data.repositories.AvailabilityRepositoryImpl
import ntu26.ss.parkinpeace.server.data.repositories.CarparkRepository
import ntu26.ss.parkinpeace.server.data.repositories.CarparkRepositoryImpl
import ntu26.ss.parkinpeace.server.plugins.configureDatabases
import ntu26.ss.parkinpeace.server.plugins.configureHTTP
import ntu26.ss.parkinpeace.server.plugins.configureRouting
import ntu26.ss.parkinpeace.server.plugins.configureSerialization
import ntu26.ss.parkinpeace.server.services.*
import org.jetbrains.exposed.sql.Database
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main() {
    embeddedServer(
        Netty, port = 8080, host = "0.0.0.0", module = Application::module
    ).start(wait = true)
}

private fun Module.withH2Prod() {
    single<Clock> { Clock.System }

    single {
        Database.connect(
            url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
            user = "root",
            driver = "org.h2.Driver",
            password = ""
        )
    }
}

private fun Module.withSqliteProd() {
    single<Clock> { Clock.System }

    single {
        Database.connect("jdbc:sqlite:parkinpeace.db", "org.sqlite.JDBC")
    }
}

private fun Module.withExternalApis() {
    singleOf(::UraApiImpl) { bind<UraApi>() }
    singleOf(::OneMapApiImpl) { bind<OneMapApi>() }
    singleOf(::MapBoxApiImpl) { bind<MapBoxApi>() }
    singleOf(::LtaApiImpl) { bind<LtaApi>() }
    singleOf(::DatagovsgApiV1Impl) { bind<DatagovsgApi>() }
}

private fun Module.withDatabaseWrappers() {
    single { RawDbCarpark.Factory(clock = get()) }

    single {
        RawCarparkDao(get()).also {
            RatesDao(get())
            FeaturesDao(get())
            CarparkHashesDao(get())
        }
    }

    single {
        get<RawCarparkDao>() // in case it was not loaded
        AvailabilityDao(get())
    }

    single<CarparkRepository> {
        CarparkRepositoryImpl(
            get(), get(), get<LocationService>().asCarparkResolver(), get()
        )
    }

    singleOf(::AvailabilityRepositoryImpl) { bind<AvailabilityRepository>() }
}

private fun Module.withServices() {
    singleOf(::LocationServiceImpl) { bind<LocationService>() }

    single<PollingService> {
        PollingServiceImpl(
            scope = get(StringQualifier("workerScope")),
            uraApi = get(),
            ltaApi = get(),
            datagovsgApi = get(),
            clock = get(),
        )
    }

    single<DataManager> {
        DataManagerImpl(
            scope = get(StringQualifier("workerScope")),
            pollingService = get(),
            carparkRepository = get(),
            availabilityRepository = get()
        )
    }
}

private val appModule = module {
    single(StringQualifier("workerScope")) {
        CoroutineScope(Dispatchers.Default + CoroutineName("Worker"))
    }

    withSqliteProd()
    withDatabaseWrappers()
    withExternalApis()
    withServices()
}

fun Application.module() {
    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }
    configureHTTP()
    configureSerialization()
    configureDatabases()
    configureRouting()

    val dataManager: DataManager by inject()
    dataManager.start()

    CarparkController(this)
}
