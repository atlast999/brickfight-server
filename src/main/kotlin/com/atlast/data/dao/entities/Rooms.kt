package com.atlast.data.dao.entities

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table

object Rooms : IntIdTable() {
    val name = varchar("name", 50)
}

object RoomMembers : Table() {
    val roomID = reference("room_id", Rooms)
    val userID = reference("user_id", Users)
    val isHost = bool("is_host")

    override val primaryKey = PrimaryKey(roomID, userID)

}