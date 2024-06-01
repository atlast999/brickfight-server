package com.atlast.data.dao.facade

import com.atlast.domain.User

interface AuthenticationDao {
    suspend fun getUser(username: String): User?
    suspend fun createUser(user: User): User?
}