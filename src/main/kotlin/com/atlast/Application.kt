package com.atlast

import com.atlast.data.dao.DatabaseInstance
import com.atlast.plugins.configureDatabases
import com.atlast.plugins.configureHTTP
import com.atlast.plugins.configureMonitoring
import com.atlast.plugins.configureRouting
import com.atlast.plugins.configureSecurity
import com.atlast.plugins.configureSerialization
import com.atlast.plugins.configureSocket
import com.atlast.routes.configureAuthenticationRoutes
import com.atlast.routes.configureChatRoutes
import com.atlast.routes.configureRoomRoutes
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.cors.routing.CORS

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
    configureSocket()
    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)
        allowHeader(HttpHeaders.Accept)
        allowHeader(HttpHeaders.AcceptCharset)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentLength)
        allowHeader(HttpHeaders.ContentType)
        allowCredentials = true
    }

    configureAuthenticationRoutes()
    configureRoomRoutes()
    configureChatRoutes()
}
