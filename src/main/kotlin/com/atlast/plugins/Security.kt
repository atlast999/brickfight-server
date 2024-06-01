package com.atlast.plugins

import com.atlast.utils.JWTExt
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond

fun Application.configureSecurity() {
    authentication {
        jwt("auth-jwt") {
            realm = JWTExt.JWT_REALM
            verifier(
                JWT.require(Algorithm.HMAC256(JWTExt.JWT_SECRET))
                    .withAudience(JWTExt.JWT_AUDIENCE)
                    .withIssuer(JWTExt.JWT_DOMAIN)
                    .build()
            )
            validate { credential ->
                val userId = credential.payload.getClaim(JWTExt.USER_ID_KEY).asInt()
                if (userId != null) {
                    JWTPrincipal(
                        payload = credential.payload
                    )
                } else null
            }
            challenge { _, _ ->
                call.respond(
                    status = HttpStatusCode.Unauthorized,
                    message = "Token is not valid or has expired",
                )
            }
        }
    }
}
