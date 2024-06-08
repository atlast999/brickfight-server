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
        val memberships = roomDao.getRoomMembership(roomId = roomId)
        val userMembership = memberships.firstOrNull { it.userId == userId } ?: run {
            throw IllegalStateException("User not in room")
        }
        if (userMembership.isHost) { // room owner left
            val leftovers = memberships.filter { it.userId != userId }
            if (leftovers.isEmpty()) { //there is no one else in room
                roomDao.deleteRoom(roomId = roomId)
            } else { //promote one of the remaining members to room owner
                val newOwner = leftovers.first()
                roomDao.removeMember(
                    roomId = roomId,
                    memberId = userId,
                )
                roomDao.updateRoomOwnership(
                    roomId = roomId,
                    memberId = newOwner.userId,
                )
            }
        } else { //member left
            roomDao.removeMember(
                roomId = roomId,
                memberId = userId,
            )
        }
    }
}