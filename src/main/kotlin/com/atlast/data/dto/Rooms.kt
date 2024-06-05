package com.atlast.data.dto

import com.atlast.domain.Room
import com.atlast.domain.RoomMember
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class CreateRoomRequest(
    @SerialName("name") val name: String,
)

fun CreateRoomRequest.toRoom() = Room(
    name = name,
)

@Serializable
data class CreateRoomResponse(
    @SerialName("id") val id: Int,
)


@Serializable
data class RoomDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("members") val members: List<MemberDto>,
)

fun Room.toRoomDto(members: List<RoomMember>) = RoomDto(
    id = id,
    name = name,
    members = members.map(RoomMember::toMemberDto),
)

@Serializable
data class MemberDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
)

fun RoomMember.toMemberDto() = MemberDto(
    id = id,
    name = name,
)