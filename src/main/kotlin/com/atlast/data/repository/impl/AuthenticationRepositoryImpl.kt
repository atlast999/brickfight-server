package com.atlast.data.repository.impl

import com.atlast.data.dao.DatabaseInstance
import com.atlast.data.dao.facade.AuthenticationDao
import com.atlast.data.repository.AuthenticationRepository
import com.atlast.domain.User

class AuthenticationRepositoryImpl(
    private val authenticationDao: AuthenticationDao,
) : AuthenticationRepository {
    override suspend fun signUp(user: User): User = DatabaseInstance.dbQuery {
        val existedUser = authenticationDao.getUser(username = user.username)
        if (existedUser != null) {
            throw IllegalStateException("User already exists")
        }
        authenticationDao.createUser(
            user = user
        ) ?: throw IllegalStateException("Failed to create user")
    }

    override suspend fun login(username: String, password: String): User =
        DatabaseInstance.dbQuery {
            val existedUser = authenticationDao.getUser(username = username)
                ?: throw IllegalStateException("User not found")
            if (existedUser.password != password) {
                throw IllegalStateException("Invalid password")
            }
            existedUser
        }

}