package fr.uge.plutus.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import fr.uge.plutus.core.Environment
import fr.uge.plutus.core.ExpiredOrInvalidTokenException
import fr.uge.plutus.util.Constants
import fr.uge.plutus.util.getHeader
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureSecurity() {
    authentication {
        configureJWT(Constants.AUTH_JWT, Environment.jwtSecret) { call, _ ->
            val token = call.getHeader("Authorization").replace("Bearer ", "")
            val id = JWT.decode(token).subject ?: throw ExpiredOrInvalidTokenException()
            BookFilePrincipal(id)
        }
    }
}

private fun AuthenticationConfig.configureJWT(
    name: String,
    secret: String,
    block: (ApplicationCall, JWTCredential) -> Principal
) = jwt(name) {
    realm = Environment.jwtRealm

    val jwtVerifier = JWT.require(Algorithm.HMAC256(secret)).build()
    verifier(jwtVerifier)

    validate { credential ->
        runCatching {
            block(this, credential)
        }.onFailure {
            if (it !is IllegalArgumentException) throw it
        }.getOrNull()
    }

    challenge { _, _ -> throw ExpiredOrInvalidTokenException() }
}

data class BookFilePrincipal(val id: String) : Principal
