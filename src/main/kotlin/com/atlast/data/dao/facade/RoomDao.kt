package com.atlast.data.dao.facade

import com.atlast.domain.Room
import com.atlast.domain.RoomMember
import com.atlast.domain.RoomMembership

interface RoomDao {
    fun createRoom(room: Room): Room
    fun getRooms(limit: Int, offset: Long): List<Room>
    fun getRoomCount(): Long
    fun getRoomMembers(roomId: Int): List<RoomMember>
    fun getRoom(roomId: Int): Room
    fun addMember(roomId: Int, memberId: Int, isOwner: Boolean)
    fun deleteRoom(roomId: Int)
    fun removeMember(roomId: Int, memberId: Int)
    fun getRoomMembership(roomId: Int): List<RoomMembership>
    fun updateRoomOwnership(roomId: Int, memberId: Int)
}