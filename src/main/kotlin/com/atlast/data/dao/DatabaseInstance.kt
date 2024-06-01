package com.atlast.data.dao

import com.atlast.data.dao.entities.Users
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseInstance {
    fun init() {
        val driverClassName = "org.h2.Driver"
        val jdbcURL = "jdbc:h2:mem:regular;DB_CLOSE_DELAY=-1;"
        Database.connect(
            url = jdbcURL,
            driver = driverClassName,
            user = "root",
            password = "",
        )
        transaction {
            SchemaUtils.create(Users)
        }
    }

    suspend fun <T> dbQuery(block: () -> T): T = newSuspendedTransaction(
        context = Dispatchers.IO,
    ) { block() }
}
