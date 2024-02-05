import java.util.Properties

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

val exposed_version: String by project
val h2_version: String by project

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinxSerialization)
    id("io.ktor.plugin") version "2.3.5"
}

val secrets = rootProject.file("SECRETS.properties").let {
    Properties().apply { load(it.inputStream()) }
}

fun String.LOAD_KEY(): String {
    return System.getenv(this) ?: secrets.getProperty(this)
}

application {
    mainClass.set("ntu26.ss.parkinpeace.server.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf(
        "-Dio.ktor.development=$isDevelopment",
        "-ea",
        "-DMAPBOX_TOKEN=${"SERVER_MAPBOX_ACCESS_TOKEN".LOAD_KEY()}",
        "-DURA_TOKEN=${"SERVER_URA_ACCESS_TOKEN".LOAD_KEY()}",
        "-DONEMAP_USER=${"SERVER_ONEMAP_USERNAME".LOAD_KEY()}",
        "-DONEMAP_PASS=${"SERVER_ONEMAP_PASSWORD".LOAD_KEY()}",
        "-DLTA_KEY=${"SERVER_LTA_ACCOUNT_KEY".LOAD_KEY()}",
    )
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
    targetHierarchy.default()

    jvm { withJava() }

    sourceSets {
        val commonMain by getting {
            dependencies {
                //put your multiplatform dependencies here
                implementation(projects.shared)
                implementation(projects.libs.svy21)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
                implementation("io.ktor:ktor-server-host-common-jvm:$ktor_version")
                implementation("io.ktor:ktor-server-resources:$ktor_version")
                implementation("io.ktor:ktor-server-openapi:$ktor_version")
                implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
                implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
                implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
                implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
                implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
                implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposed_version")
                implementation("com.h2database:h2:$h2_version")
                implementation(libs.postgresql)
                implementation(libs.sqlite.jdbc)
                implementation("io.ktor:ktor-server-cors-jvm:$ktor_version")
                implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
                implementation("ch.qos.logback:logback-classic:$logback_version")
                implementation(libs.ulid)
                implementation(libs.kotlinx.serialization)
                implementation(libs.retrofit)
                implementation(libs.retrofit2.kotlinx.serialization.converter)
                implementation(libs.okhttp)
                implementation(libs.logging.interceptor)
                implementation("io.insert-koin:koin-ktor:3.5.1")
                implementation("io.insert-koin:koin-logger-slf4j:3.5.1")
                implementation("com.sletmoe.bucket4k:bucket4k:1.0.0")
                //                testImplementation("io.ktor:ktor-server-tests-jvm")
                //                testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
            }
        }
    }

}