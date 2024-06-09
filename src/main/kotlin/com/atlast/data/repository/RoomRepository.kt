package com.atlast.data.repository

import com.atlast.domain.Room
import com.atlast.domain.RoomMember

interface RoomRepository {
    suspend fun createRoom(userId: Int, room: Room): Room

    suspend fun getRooms(page: Int, pageSize: Int): List<Room>

    suspend fun getRoomMembers(roomId: Int): List<RoomMember>

    suspend fun getRoomCount(): Long

    suspend fun getRoom(roomId: Int): Room

    suspend fun joinRoom(userId: Int, roomId: Int)

    suspend fun leaveRoom(userId: Int, roomId: Int)
}
