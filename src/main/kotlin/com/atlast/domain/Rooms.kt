package com.atlast.domain

data class Room(
    val id: Int = -1,
    val name: String,
)

data class RoomMember(
    val id: Int,
    val name: String,
)

data class RoomMembership(
    val userId: Int,
    val isHost: Boolean,
)