package com.atlast.routes

import com.atlast.data.dao.facade.impl.RoomDaoImpl
import com.atlast.data.repository.impl.RoomRepositoryImpl
import com.atlast.services.ChatService
import com.atlast.services.ClientConnection
import com.atlast.utils.JWTExt
import io.ktor.server.application.Application
import io.ktor.server.auth.principal
import io.ktor.server.routing.routing
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.close

fun Application.configureChatRoutes() {
    val roomDao = RoomDaoImpl()
    val roomRepository = RoomRepositoryImpl(roomDao = roomDao)
    val chatService = ChatService(roomRepository = roomRepository)
    routing {
        webSocket("/ws/{roomId}/{name}") {
            val userId = JWTExt.extractUserId(principal = call.principal())
            val roomId = call.parameters["roomId"]?.toIntOrNull() ?: return@webSocket run {
                close(
                    reason = CloseReason(
                        code = CloseReason.Codes.VIOLATED_POLICY,
                        message = "Room is undefined"
                    )
                )
            }
            val client = ClientConnection(
                userId = userId,
                session = this
            )
            chatService.onClientConnected(
                roomId = roomId,
                client = client,
            )
        }

        webSocket("/chat/{roomId}/{userId}") {
            val userId = call.parameters["userId"]?.toIntOrNull() ?: return@webSocket run {
                close(
                    reason = CloseReason(
                        code = CloseReason.Codes.VIOLATED_POLICY,
                        message = "User is undefined"
                    )
                )
            }
            val roomId = call.parameters["roomId"]?.toIntOrNull() ?: return@webSocket run {
                close(
                    reason = CloseReason(
                        code = CloseReason.Codes.VIOLATED_POLICY,
                        message = "Room is undefined"
                    )
                )
            }
            val client = ClientConnection(
                userId = userId,
                session = this,
                sendEcho = true,
            )
            chatService.onClientConnected(
                roomId = roomId,
                client = client,
            )
        }
    }
}
