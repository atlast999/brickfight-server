package com.atlast.routes

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

fun Application.configureAuthenticationRoutes() {

    val authenticationDao = AuthenticationDaoImpl()

    val authenticationRepository =
        AuthenticationRepositoryImpl(authenticationDao = authenticationDao)

    val authenticationService =
        AuthenticationService(authenticationRepository = authenticationRepository)

    routing {

        get("/fake_signup") {
            val request = SignupRequest(
                username = "User 1",
                password = "",
            )
            val response = authenticationService.signUp(
                signupRequest = request,
            )
            call.respond(
                message = AppResponse(
                    data = response,
                )
            )
        }

        get("/fake_login") {
            val request = LoginRequest(
                username = "User 1",
                password = "",
            )
            val response = authenticationService.login(
                loginRequest = request,
            )
            call.respond(
                message = AppResponse(
                    data = response,
                )
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