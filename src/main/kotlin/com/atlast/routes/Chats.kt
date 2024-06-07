package com.atlast.routes

import com.atlast.utils.JWTExt
import io.ktor.server.application.Application
import io.ktor.server.auth.principal
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
            try {
                println("Client connected: $userId")
                chatService.onClientConnected(
                    roomId = roomId,
                    client = client,
                )
            } finally {
                println("Client disconnected: $userId with reason: ${closeReason.await()}")
                chatService.onClientDisconnected(
                    roomId = roomId,
                    client = client,
                )
            }
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
            try {
                println("Client connected: $userId")
                chatService.onClientConnected(
                    roomId = roomId,
                    client = client,
                )
            } finally {
                println("Client disconnected: $userId with reason: ${closeReason.await()}")
                chatService.onClientDisconnected(
                    roomId = roomId,
                    client = client,
                )
            }
        }
    }
}

class ClientConnection(
    val userId: Int,
    val session: DefaultWebSocketSession,
    val sendEcho: Boolean = false,
)

typealias RoomId = Int

class ChatService {
    private val rooms = ConcurrentHashMap<RoomId, MutableSet<ClientConnection>>()

    suspend fun onClientConnected(roomId: RoomId, client: ClientConnection) {
        rooms.getOrPut(roomId) {
            Collections.synchronizedSet(HashSet())
        }.add(client)
        activeOnRoom(roomId = roomId, client = client)
    }

    fun onClientDisconnected(roomId: RoomId, client: ClientConnection) {
        rooms[roomId]?.remove(client)
    }

    private suspend fun activeOnRoom(roomId: RoomId, client: ClientConnection) {
        client.session.incoming.consumeEach { frame ->
            val roomMembers = rooms[roomId] ?: return run {
                client.session.close(
                    CloseReason(
                        CloseReason.Codes.VIOLATED_POLICY,
                        "Room no longer exists"
                    )
                )
            }
            roomMembers.forEach { member ->
                if (member.userId == client.userId && client.sendEcho.not()) return@forEach
                runCatching {
                    member.session.send(frame.copy()) //Cannot reuse frame across multiple send operations
                }.onFailure {
                    member.session.close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, ""))
                }
            }
        }
    }
}