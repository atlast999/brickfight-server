package com.atlast.services

import com.atlast.data.dto.LoginRequest
import com.atlast.data.dto.LoginResponse
import com.atlast.data.dto.SignupRequest
import com.atlast.data.dto.SignupResponse
import com.atlast.data.repository.AuthenticationRepository

class AuthenticationService(
    private val authenticationRepository: AuthenticationRepository
) {

    suspend fun signUp(signupRequest: SignupRequest): SignupResponse {
        return authenticationRepository.signUp(
            signupRequest = signupRequest,
        )
    }

    suspend fun login(loginRequest: LoginRequest): LoginResponse {
        return authenticationRepository.login(
            loginRequest = loginRequest,
        )
    }


}