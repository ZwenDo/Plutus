package fr.uge.plutus

import fr.uge.plutus.core.Environment
import io.ktor.server.application.*
import fr.uge.plutus.plugins.*
import io.ktor.server.netty.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress("UNUSED") // referenced by application.yaml
fun Application.module() {
    Environment.application = this
    configureCORS()
    configureExceptionAdvisors()
    configureSecurity()
    configureRouting()
}
