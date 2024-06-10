package com.atlast.routes

import com.atlast.data.dao.facade.impl.RoomDaoImpl
import com.atlast.data.repository.impl.RoomRepositoryImpl
import com.atlast.services.ChatService
import com.atlast.services.ClientConnection
import com.atlast.services.RoomId
import com.atlast.utils.JWTExt
import com.atlast.utils.LogExt
import io.ktor.server.application.Application
import io.ktor.server.auth.principal
import io.ktor.server.routing.routing
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

fun Application.configureChatRoutes() {
    val roomDao = RoomDaoImpl()
    val roomRepository = RoomRepositoryImpl(roomDao = roomDao)
    val chatService = ChatService(roomRepository = roomRepository)
    val callService = CallService()
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

        webSocket("/call") {
//            val roomId = call.parameters["roomId"]?.toIntOrNull() ?: return@webSocket run {
//                close(
//                    reason = CloseReason(
//                        code = CloseReason.Codes.VIOLATED_POLICY,
//                        message = "Room is undefined"
//                    )
//                )
//            }
            val client = ClientConnection(
                userId = -1,
                session = this,
                sendEcho = true,
            )
            callService.onClientConnected(
                roomId = -1,
                client = client,
            )
        }
    }
}

class CallService {

    private val rooms = ConcurrentHashMap<RoomId, MutableSet<ClientConnection>>()
    suspend fun onClientConnected(roomId: RoomId, client: ClientConnection) {
        rooms.getOrPut(roomId) {
            Collections.synchronizedSet(HashSet())
        }.add(client)
        try {
            LogExt.i("Client connected: ${client.userId}")
            activeOnRoom(roomId = roomId, client = client)
        } finally {
            LogExt.i("Client disconnected: ${client.userId} with reason: ${client.session.closeReason.await()}")
            this.onClientDisconnected(
                roomId = roomId,
                client = client,
            )
        }
    }

    private fun onClientDisconnected(roomId: RoomId, client: ClientConnection) {
        rooms[roomId]?.remove(client)
        if (rooms[roomId].isNullOrEmpty()) {
            rooms.remove(roomId)
        }
    }

    private val streamChannel = Channel<Frame>(
        capacity = 300,
        onBufferOverflow = BufferOverflow.DROP_LATEST,
    )

    private suspend fun activeOnRoom(roomId: RoomId, client: ClientConnection) = coroutineScope {
        val roomMembers = rooms[roomId] ?: return@coroutineScope run {
            client.session.close(
                CloseReason(
                    CloseReason.Codes.VIOLATED_POLICY,
                    "Room no longer exists"
                )
            )
        }
        launch(Dispatchers.IO) {
            client.session.incoming.consumeEach { frame ->
                LogExt.d("Receive byte: ${frame.data.takeLast(5).take(3).joinToString()}")

                streamChannel.send(frame)
            }
        }
        launch(Dispatchers.IO) {
            streamChannel.consumeEach { frame ->
                roomMembers.forEach { member ->
                    if (member.userId == client.userId && client.sendEcho.not()) return@forEach
                    launch {
                        runCatching {
                            LogExt.d("Send byte: ${frame.data.takeLast(5).take(3).joinToString()}")
                            member.session.send(frame.copy()) //Cannot reuse frame across multiple send operations
                        }.onFailure {
                            member.session.close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, ""))
                        }
                    }
                }
            }
        }
    }
}