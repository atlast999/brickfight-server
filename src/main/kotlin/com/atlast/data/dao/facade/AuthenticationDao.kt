package com.atlast.data.dao.facade

import com.atlast.domain.User

interface AuthenticationDao {
    fun getUser(username: String): User?
    fun createUser(user: User): User?
    fun getUser(userId: Int): User?
}