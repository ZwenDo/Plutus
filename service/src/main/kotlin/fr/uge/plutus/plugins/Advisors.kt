package fr.uge.plutus.plugins

import fr.uge.plutus.core.ExpiredOrInvalidTokenException
import fr.uge.plutus.core.MissingHeaderException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureExceptionAdvisors() {
    install(StatusPages) {
        exception(::handleException)
    }
}

private suspend fun handleException(call: ApplicationCall, cause: Throwable) = when (cause) {
    is MissingHeaderException -> call.badRequestMessage(cause)

    is ExpiredOrInvalidTokenException -> call.unauthorizedMessage(cause)

    else -> call.internalServerError(cause)
}

private suspend fun ApplicationCall.badRequestMessage(cause: Throwable) = respondText(
    text = "400: ${cause.message ?: cause.javaClass.name}",
    status = HttpStatusCode.BadRequest
)

private suspend fun ApplicationCall.unauthorizedMessage(cause: Throwable) = respondText(
    text = "401: ${cause.message ?: cause.javaClass.name}",
    status = HttpStatusCode.Unauthorized
)

private suspend fun ApplicationCall.internalServerError(cause: Throwable) = respondText(
    text = "500: Internal Server Error",
    status = HttpStatusCode.InternalServerError
).also {
    application.log.error("Unexpected error", cause)
}