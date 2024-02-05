package ntu26.ss.parkinpeace.server.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.openapi.openAPI
import io.ktor.server.resources.Resources
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    install(Resources)
    routing {
        get("/") {
            call.respond(HttpStatusCode.OK, mapOf("result" to true))
        }
        // Static plugin. Try to access `/static/index.html`
        staticResources("/static", "static")

        openAPI(path = "openapi", swaggerFile = "openapi/documentation.yaml")
    }
}
