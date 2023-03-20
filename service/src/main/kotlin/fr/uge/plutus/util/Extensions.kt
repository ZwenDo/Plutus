package fr.uge.plutus.util

import fr.uge.plutus.core.MissingHeaderException
import io.ktor.server.application.*

fun ApplicationCall.getHeader(header: String) = request.headers[header] ?: throw MissingHeaderException(header)