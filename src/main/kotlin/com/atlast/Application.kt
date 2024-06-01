package com.atlast

import com.atlast.plugins.*
import com.atlast.data.dao.DatabaseInstance
import com.atlast.routes.configureAuthenticationRoutes
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    DatabaseInstance.init()
    configureAuthenticationRoutes()
    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureDatabases()
    configureSecurity()
    configureRouting()
}
