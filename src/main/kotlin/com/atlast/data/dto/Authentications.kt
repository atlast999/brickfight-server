package com.atlast.data.dto

import com.atlast.domain.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SignupRequest(
    @SerialName("email") val email: String,
    @SerialName("username") val username: String,
    @SerialName("password") val password: String,
)

fun SignupRequest.toUser(): User = User(
    email = email,
    username = username,
    password = password
)

@Serializable
data class LoginRequest(
    @SerialName("username") val username: String,
    @SerialName("password") val password: String,
)

@Serializable
data class LoginResponse(
    @SerialName("token") val token: String,
)

@Serializable
data class SignupResponse(
    @SerialName("token") val token: String,
)