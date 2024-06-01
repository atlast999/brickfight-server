package com.atlast.data.repository.impl

import com.atlast.data.dao.facade.AuthenticationDao
import com.atlast.data.dto.LoginRequest
import com.atlast.data.dto.LoginResponse
import com.atlast.data.dto.SignupRequest
import com.atlast.data.dto.SignupResponse
import com.atlast.data.dto.toUser
import com.atlast.data.repository.AuthenticationRepository

class AuthenticationRepositoryImpl(
    private val authenticationDao: AuthenticationDao,
) : AuthenticationRepository {
    override suspend fun signUp(signupRequest: SignupRequest): SignupResponse {
        val user = authenticationDao.getUser(username = signupRequest.username)
        if (user != null) {
            throw IllegalStateException("User already exists")
        }
        val createdUser = authenticationDao.createUser(
            user = signupRequest.toUser()
        ) ?: throw IllegalStateException("Failed to create user")

        return SignupResponse(
            token = createdUser.id.toString()
        )
    }

    override suspend fun login(loginRequest: LoginRequest): LoginResponse {
        val user = authenticationDao.getUser(username = loginRequest.username)
            ?: throw IllegalStateException("User not found")
        if (user.password != loginRequest.password) {
            throw IllegalStateException("Invalid password")
        }
        return LoginResponse(
            token = user.id.toString()
        )
    }

}