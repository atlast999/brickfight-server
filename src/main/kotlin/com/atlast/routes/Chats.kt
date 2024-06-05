package com.atlast.routes

import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.close
import kotlinx.coroutines.channels.consumeEach
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

fun Application.configureChatRoutes() {
    val chatService = ChatService()
    routing {
        webSocket("/ws/{roomId}/{name}") {
            val roomId = call.parameters["roomId"]?.toIntOrNull() ?: return@webSocket
            val client = ClientConnection(
                name = call.parameters["name"] ?: "unknown",
                session = this
            )
            println("Client connected: ${client.name}")
            try {
                chatService.onClientConnected(
                    roomId = roomId,
                    client = client,
                )
            } finally {
                println("Client disconnected: ${client.name} with reason: $closeReason")
                chatService.onClientDisconnected(
                    roomId = roomId,
                    client = client,
                )
            }
        }
    }
}

class ClientConnection(
    val name: String,
    val session: DefaultWebSocketSession,
)

typealias RoomId = Int

class ChatService {
    private val rooms = ConcurrentHashMap<RoomId, MutableSet<ClientConnection>>()

    suspend fun onClientConnected(roomId: RoomId, client: ClientConnection) {
        rooms.getOrPut(roomId) {
            Collections.synchronizedSet(HashSet())
        }.add(client).also {
            println("Client ${client.name} connected to room $roomId with result: $it")
        }
        activeOnRoom(roomId = roomId, client = client)
    }

    fun onClientDisconnected(roomId: RoomId, client: ClientConnection) {
        rooms[roomId]?.remove(client)
    }

    private suspend fun activeOnRoom(roomId: RoomId, client: ClientConnection) {
        client.session.incoming.consumeEach { frame ->
            val roomMembers = rooms[roomId] ?: return
            println("Received message from client ${client.name} of room: $roomId")
            println("Room members: ${roomMembers.joinToString { it.name }}")
            roomMembers.forEach { member ->
                println("Sending message to client ${member.name} of room: $roomId")
                runCatching {
                    member.session.send(frame.copy()) //Cannot reuse frame across multiple send operations
                }.onFailure {
                    member.session.close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, ""))
                }
            }
        }
    }
}