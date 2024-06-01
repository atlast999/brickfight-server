package com.atlast.data.dao.facade

import com.atlast.domain.Room
import com.atlast.domain.RoomMember

interface RoomDao {
    fun createRoom(room: Room): Room
    fun getRooms(limit: Int, offset: Long): List<Room>
    fun getRoomCount(): Long
    fun getRoomMembers(roomId: Int): List<RoomMember>
    fun getRoom(roomId: Int): Room
    fun deleteRoom(roomId: Int)
    fun addMember(roomId: Int, memberId: Int, isOwner: Boolean)
    fun removeMember(roomId: Int, memberId: Int)

}