package com.atlast.data.repository

import com.atlast.domain.User

interface AuthenticationRepository {
     suspend fun signUp(user: User): User

     suspend fun login(username: String, password: String): User
}