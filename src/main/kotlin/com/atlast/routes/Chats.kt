package com.atlast.routes

import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.DefaultWebSocketSession
import kotlinx.coroutines.channels.consumeEach

fun Application.configureChatRoutes() {
    val chatService = ChatService()
    routing {
        webSocket("/ws/{roomId}") {
            val roomId = call.parameters["roomId"]?.toIntOrNull() ?: return@webSocket
            val client = ClientConnection(this)
            runCatching {
                chatService.onClientConnected(
                    roomId = roomId,
                    client = client,
                )
            }.onFailure {
                chatService.onClientDisconnected(
                    roomId = roomId,
                    client = client,
                )
            }
        }
    }
}

class ClientConnection(val session: DefaultWebSocketSession)

typealias RoomId = Int

class ChatService {
    private val rooms = mutableMapOf<RoomId, MutableSet<ClientConnection>>()

    suspend fun onClientConnected(roomId: RoomId, client: ClientConnection) {
        rooms.getOrPut(roomId) { mutableSetOf() }.add(client)
        activeOnRoom(roomId = roomId, client = client)
    }

    fun onClientDisconnected(roomId: RoomId, client: ClientConnection) {
        rooms[roomId]?.remove(client)
    }

    private suspend fun activeOnRoom(roomId: RoomId, client: ClientConnection) {
        val roomMembers = rooms[roomId] ?: return

        client.session.incoming.consumeEach { frame ->
            roomMembers.forEach { member ->
                member.session.send(frame)
            }
        }
    }
}