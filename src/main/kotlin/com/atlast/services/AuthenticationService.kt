package com.atlast.services

import com.atlast.data.dto.LoginRequest
import com.atlast.data.dto.LoginResponse
import com.atlast.data.dto.SignupRequest
import com.atlast.data.dto.SignupResponse
import com.atlast.data.dto.toUser
import com.atlast.data.repository.AuthenticationRepository
import com.atlast.utils.JWTExt

class AuthenticationService(
    private val authenticationRepository: AuthenticationRepository
) {

    suspend fun signUp(signupRequest: SignupRequest): SignupResponse {
        val createdUser = authenticationRepository.signUp(user = signupRequest.toUser())
        return SignupResponse(
            token = JWTExt.generateToken(userId = createdUser.id)
        )
    }

    suspend fun login(loginRequest: LoginRequest): LoginResponse {
        val user = authenticationRepository.login(
            username = loginRequest.username,
            password = loginRequest.password,
        )
        return LoginResponse(
            token = JWTExt.generateToken(userId = user.id),
        )
    }


}