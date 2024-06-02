package com.atlast.data.dao.facade.impl

import com.atlast.data.dao.entities.Users
import com.atlast.data.dao.facade.AuthenticationDao
import com.atlast.domain.User
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class AuthenticationDaoImpl : AuthenticationDao {

    private fun resultRowToUser(row: ResultRow) = User(
        id = row[Users.id].value,
        email = row[Users.email],
        username = row[Users.username],
        password = row[Users.password],
    )

    override fun getUser(username: String): User? = Users.selectAll()
        .where { Users.username eq username }
        .singleOrNull()?.let(::resultRowToUser)

    override fun createUser(user: User): User? = Users.insert {
        it[email] = user.email
        it[username] = user.username
        it[password] = user.password
    }.resultedValues?.single()?.let(::resultRowToUser)

    override fun getUser(userId: Int): User? = Users.selectAll()
        .where { Users.id eq userId }
        .singleOrNull()?.let(::resultRowToUser)

}