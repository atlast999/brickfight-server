package com.atlast.services

import com.atlast.data.dto.CreateRoomRequest
import com.atlast.data.dto.CreateRoomResponse
import com.atlast.data.dto.RoomDto
import com.atlast.data.dto.toRoom
import com.atlast.data.dto.toRoomDto
import com.atlast.data.dto.wrapper.PagingModel
import com.atlast.data.repository.RoomRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.math.ceil

class RoomService(
    private val roomRepository: RoomRepository,
) {
    suspend fun createRoom(
        userId: Int,
        request: CreateRoomRequest
    ): CreateRoomResponse {
        val createdRoom = roomRepository.createRoom(
            userId = userId,
            room = request.toRoom()
        )
        return CreateRoomResponse(id = createdRoom.id)
    }

    suspend fun getRooms(page: Int, pageSize: Int): PagingModel<RoomDto> = coroutineScope {
        val roomCount = async {
            roomRepository.getRoomCount()
        }
        val rooms = roomRepository.getRooms(page, pageSize).map { room ->
            async {
                val members = roomRepository.getRoomMembers(roomId = room.id)
                room.toRoomDto(
                    members = members,
                )
            }
        }.awaitAll()

        val totalPage = ceil(roomCount.await().toDouble() / pageSize).toInt()
        PagingModel(
            page = page,
            pageSize = pageSize,
            totalPage = totalPage,
            items = rooms,
        )
    }

    suspend fun getRoom(roomId: Int): RoomDto {
        val room = roomRepository.getRoom(roomId = roomId)
        val members = roomRepository.getRoomMembers(roomId = roomId)
        return room.toRoomDto(members = members)
    }

    suspend fun joinRoom(userId: Int, roomId: Int) {
        return roomRepository.joinRoom(
            userId = userId,
            roomId = roomId
        )
    }

    suspend fun leaveRoom(userId: Int, roomId: Int) {
        roomRepository.leaveRoom(
            userId = userId,
            roomId = roomId
        )
    }

}
