package com.atlast.plugins

import java.sql.Connection

data class User(
    val username: String,
    val password: String,
)

class AuthenticationService(
    private val connection: Connection,
) {
    companion object {

    }


}