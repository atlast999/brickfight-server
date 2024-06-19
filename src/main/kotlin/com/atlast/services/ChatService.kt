package com.atlast.services

import com.atlast.data.repository.RoomRepository
import com.atlast.utils.LogExt
import io.ktor.websocket.CloseReason
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.close
import kotlinx.coroutines.channels.consumeEach
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

class ClientConnection(
    val userId: Int,
    val session: DefaultWebSocketSession,
    val sendEcho: Boolean = false,
)

typealias RoomId = Int

class RoomManager {
    private val rooms = ConcurrentHashMap<RoomId, MutableSet<ClientConnection>>()
    fun addMember(roomId: RoomId, client: ClientConnection) {
        rooms.getOrPut(roomId) {
            Collections.synchronizedSet(HashSet())
        }.add(client)
    }

    fun removeMember(
        roomId: RoomId,
        client: ClientConnection,
    ) {
        rooms[roomId]?.remove(client)
        if (rooms[roomId].isNullOrEmpty()) {
            rooms.remove(roomId)
        }
    }

    fun getMembers(roomId: RoomId): Set<ClientConnection>? {
        return rooms[roomId]
    }
}

class ChatService(
    private val roomRepository: RoomRepository,
) {
    private val roomManagers = RoomManager()

    suspend fun onClientConnected(roomId: RoomId, client: ClientConnection) {
        roomManagers.addMember(roomId, client)
        try {
            LogExt.i("Client connected: ${client.userId}")
            activeOnRoom(roomId = roomId, client = client)
        } finally {
            LogExt.i("Client disconnected: ${client.userId} with reason: ${client.session.closeReason.await()}")
            //in case of client did not call leave room api
            kotlin.runCatching { //may throw exception when user already left the room by calling leave room api
                LogExt.i("Make client leave room: ${client.userId}")
                roomRepository.leaveRoom(
                    roomId = roomId,
                    userId = client.userId,
                )
            }.onFailure {
                LogExt.e("Failed to leave room: ${client.userId} with reason: ${it.message}")
            }
            this.onClientDisconnected(
                roomId = roomId,
                client = client,
            )
        }
    }

    private fun onClientDisconnected(roomId: RoomId, client: ClientConnection) {
        roomManagers.removeMember(
            roomId = roomId,
            client = client,
        )
    }

    private suspend fun activeOnRoom(roomId: RoomId, client: ClientConnection) {
        val roomMembers = roomManagers.getMembers(roomId) ?: return run {
            client.session.close(
                CloseReason(
                    CloseReason.Codes.VIOLATED_POLICY,
                    "Room no longer exists"
                )
            )
        }
        client.session.incoming.consumeEach { frame ->
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