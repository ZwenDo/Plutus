package fr.uge.plutus.core

import io.ktor.server.application.*

object Environment {

    lateinit var application: Application
    private val config get() = application.environment.config

    val jwtSecret: String get() = config.property("plutus.jwt.secret").getString()
    val jwtRealm: String get() = config.property("plutus.jwt.realm").getString()
}