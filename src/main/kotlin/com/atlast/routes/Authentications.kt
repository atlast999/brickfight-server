package com.atlast.routes

import com.atlast.data.dao.DatabaseInstance
import com.atlast.data.dao.entities.Users
import com.atlast.data.dao.facade.impl.AuthenticationDaoImpl
import com.atlast.data.dto.LoginRequest
import com.atlast.data.dto.SignupRequest
import com.atlast.data.dto.wrapper.AppResponse
import com.atlast.data.repository.impl.AuthenticationRepositoryImpl
import com.atlast.services.AuthenticationService
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import org.jetbrains.exposed.sql.selectAll

fun Application.configureAuthenticationRoutes() {

    val authenticationDao = AuthenticationDaoImpl()

    val authenticationRepository =
        AuthenticationRepositoryImpl(authenticationDao = authenticationDao)

    val authenticationService =
        AuthenticationService(authenticationRepository = authenticationRepository)

    routing {

        get("/members") {//testing purposes only
            val response = DatabaseInstance.dbQuery {
                Users.selectAll().map {
                    mapOf(
                        "user_id" to it[Users.id].value.toString(),
                        "name" to it[Users.username].toString(),
                    )
                }
            }
            call.respond(
                message = response
            )
        }

        post("/register") {
            val request = call.receive<SignupRequest>()
            val response = authenticationService.signUp(
                signupRequest = request,
            )
            call.respond(
                message = AppResponse(
                    data = response,
                )
            )
        }
        post("/login") {
            val request = call.receive<LoginRequest>()
            val response = authenticationService.login(
                loginRequest = request,
            )
            call.respond(
                message = AppResponse(
                    data = response,
                )
            )
        }
    }
}