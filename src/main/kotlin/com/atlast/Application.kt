package com.atlast

import com.atlast.data.dao.DatabaseInstance
import com.atlast.plugins.configureDatabases
import com.atlast.plugins.configureHTTP
import com.atlast.plugins.configureMonitoring
import com.atlast.plugins.configureRouting
import com.atlast.plugins.configureSecurity
import com.atlast.plugins.configureSerialization
import com.atlast.routes.configureAuthenticationRoutes
import com.atlast.routes.configureRoomRoutes
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    DatabaseInstance.init()
    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureDatabases()
    configureSecurity()
    configureRouting()

    configureAuthenticationRoutes()
    configureRoomRoutes()
}
