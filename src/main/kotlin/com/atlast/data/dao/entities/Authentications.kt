package com.atlast.data.dao.entities

import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 50)
    val password = varchar("password", 50)

    override val primaryKey = PrimaryKey(id)
}