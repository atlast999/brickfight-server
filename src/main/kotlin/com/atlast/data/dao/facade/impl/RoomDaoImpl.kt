package com.atlast.data.dao.facade.impl

import com.atlast.data.dao.entities.RoomMembers
import com.atlast.data.dao.entities.Rooms
import com.atlast.data.dao.entities.Users
import com.atlast.data.dao.facade.RoomDao
import com.atlast.domain.Room
import com.atlast.domain.RoomMember
import com.atlast.domain.RoomMembership
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class RoomDaoImpl : RoomDao {

    private fun resultRowToRoom(row: ResultRow) = Room(
        id = row[Rooms.id].value,
        name = row[Rooms.name],
    )

    private fun resultRowToRoomMember(row: ResultRow) = RoomMember(
        id = row[RoomMembers.userID].value,
        name = row[Users.username],
    )

    private fun resultRowToRoomMembership(row: ResultRow) = RoomMembership(
        userId = row[RoomMembers.userID].value,
        isHost = row[RoomMembers.isHost],
    )

    override fun createRoom(room: Room) = Rooms.insert {
        it[name] = room.name
    }.resultedValues?.firstOrNull()?.let(::resultRowToRoom)!!


    override fun getRooms(limit: Int, offset: Long) =
        Rooms.selectAll().limit(limit, offset).map(this::resultRowToRoom)

    override fun getRoomCount() = Rooms.selectAll().count()

    override fun getRoomMembers(roomId: Int) = RoomMembers.innerJoin(Users)
        .select(RoomMembers.userID, Users.username)
        .where { RoomMembers.roomID eq roomId }
        .map(this::resultRowToRoomMember)

    override fun getRoom(roomId: Int) = Rooms.selectAll()
        .where { Rooms.id eq roomId }
        .single()
        .let(this::resultRowToRoom)

    override fun deleteRoom(roomId: Int) {
        Rooms.deleteWhere { id eq roomId }
    }

    override fun addMember(roomId: Int, memberId: Int, isOwner: Boolean) {
        RoomMembers.insert {
            it[roomID] = roomId
            it[userID] = memberId
            it[isHost] = isOwner
        }
    }

    override fun removeMember(roomId: Int, memberId: Int) {
        RoomMembers.deleteWhere {
            (roomID eq roomId) and (userID eq memberId)
        }
    }

    override fun getRoomMembership(roomId: Int): List<RoomMembership> =
        RoomMembers.selectAll()
            .where { RoomMembers.roomID eq roomId }
            .map(this::resultRowToRoomMembership)

    override fun updateRoomOwnership(roomId: Int, memberId: Int) {
        RoomMembers.update(
            where = {
                (RoomMembers.roomID eq roomId) and (RoomMembers.userID eq memberId)
            },
            body = {
                it[isHost] = true
            }
        )
    }
}