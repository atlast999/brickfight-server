package com.atlast.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.auth.jwt.JWTPrincipal
import java.util.Date

object JWTExt {
    // Please read the jwt property from the config file if you are using EngineMain
    const val JWT_AUDIENCE = "jwt-audience"
    const val JWT_DOMAIN = "https://jwt-provider-domain/"
    const val JWT_REALM = "brick_fight"
    const val JWT_SECRET = "secret"

    const val USER_ID_KEY = "user_id"
    fun generateToken(userId: Int): String {
        return JWT.create()
            .withAudience(JWT_AUDIENCE)
            .withIssuer(JWT_DOMAIN)
            .withClaim(USER_ID_KEY, userId)
            .withExpiresAt(Date(System.currentTimeMillis() + 3600000))
            .sign(Algorithm.HMAC256(JWT_SECRET))
    }

    fun extractUserId(principal: JWTPrincipal?): Int =
        principal!!.payload.getClaim(USER_ID_KEY).asInt()
}
