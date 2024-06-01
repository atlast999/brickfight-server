package com.atlast.data.dao.facade.impl

import com.atlast.data.dao.DatabaseInstance
import com.atlast.data.dao.entities.Users
import com.atlast.data.dao.facade.AuthenticationDao
import com.atlast.domain.User
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class AuthenticationDaoImpl : AuthenticationDao {

    private fun resultRowToUser(row: ResultRow) = User(
        id = row[Users.id].value,
        username = row[Users.username],
        password = row[Users.password]
    )

    override suspend fun getUser(username: String): User? = DatabaseInstance.dbQuery {
        Users.selectAll().where { Users.username eq username }.map(::resultRowToUser).singleOrNull()
    }

    override suspend fun createUser(user: User): User? = DatabaseInstance.dbQuery {
        Users.insert {
            it[username] = user.username
            it[password] = user.password
        }.resultedValues?.firstOrNull()?.let(::resultRowToUser)
    }

    override suspend fun getUser(userId: Int): User? = DatabaseInstance.dbQuery {
        Users.selectAll().where {
            Users.id eq userId
        }.map(::resultRowToUser).singleOrNull()
    }
}