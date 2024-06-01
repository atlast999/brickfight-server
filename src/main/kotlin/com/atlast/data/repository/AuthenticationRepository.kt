package com.atlast.data.repository

import com.atlast.data.dto.LoginRequest
import com.atlast.data.dto.LoginResponse
import com.atlast.data.dto.SignupRequest
import com.atlast.data.dto.SignupResponse

interface AuthenticationRepository {
     suspend fun signUp(signupRequest: SignupRequest): SignupResponse

     suspend fun login(loginRequest: LoginRequest): LoginResponse
}