package com.atlast.data.dao.entities

import org.jetbrains.exposed.dao.id.IntIdTable

object Users : IntIdTable() {
    val email = varchar("email", 50)
    val username = varchar("username", 50)
    val password = varchar("password", 50)
}