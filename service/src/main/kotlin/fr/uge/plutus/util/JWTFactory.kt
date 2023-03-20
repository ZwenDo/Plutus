package fr.uge.plutus.util

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import fr.uge.plutus.core.Environment
import java.util.*

object JWTFactory {

    fun from(id: String): String = JWT.create()
        .withSubject(id)
        .withIssuedAt(Date())
        .sign(Algorithm.HMAC256(Environment.jwtSecret))
}
