package com.atlast.data.repository.impl

import com.atlast.data.dao.DatabaseInstance
import com.atlast.data.dao.facade.RoomDao
import com.atlast.data.repository.RoomRepository
import com.atlast.domain.Room
import com.atlast.domain.RoomMember

class RoomRepositoryImpl(
    private val roomDao: RoomDao,
) : RoomRepository {
    override suspend fun createRoom(userId: Int, room: Room): Room = DatabaseInstance.dbQuery {
        val createdRoom = roomDao.createRoom(room = room)
        roomDao.addMember(
            roomId = createdRoom.id,
            memberId = userId,
            isOwner = true,
        )
        createdRoom
    }

    override suspend fun getRooms(page: Int, pageSize: Int): List<Room> = DatabaseInstance.dbQuery {
        roomDao.getRooms(
            limit = pageSize,
            offset = page.minus(1) * pageSize.toLong(),
        )
    }

    override suspend fun getRoomMembers(roomId: Int): List<RoomMember> = DatabaseInstance.dbQuery {
        roomDao.getRoomMembers(roomId = roomId)
    }

    override suspend fun getRoomCount(): Long = DatabaseInstance.dbQuery {
        roomDao.getRoomCount()
    }

    override suspend fun getRoom(roomId: Int): Room = DatabaseInstance.dbQuery {
        roomDao.getRoom(roomId = roomId)
    }

    override suspend fun joinRoom(userId: Int, roomId: Int) = DatabaseInstance.dbQuery {
        roomDao.addMember(
            roomId = roomId,
            memberId = userId,
            isOwner = false,
        )
    }

    override suspend fun leaveRoom(userId: Int, roomId: Int) = DatabaseInstance.dbQuery {
        val isRoomOwner = roomDao.isRoomOwner(roomId = roomId, memberId = userId)
        if (isRoomOwner) {
            roomDao.deleteRoom(roomId = roomId)
        } else {
            roomDao.removeMember(
                roomId = roomId,
                memberId = userId,
            )
        }
    }
}