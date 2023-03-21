package fr.uge.plutus.plugins

import fr.uge.plutus.routes.fileStorageRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        route("/api") {
            fileStorageRoutes()
        }
    }
}
