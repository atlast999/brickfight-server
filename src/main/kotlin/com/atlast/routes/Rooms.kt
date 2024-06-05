package com.atlast.routes

import com.atlast.data.dao.DatabaseInstance
import com.atlast.data.dao.entities.RoomMembers
import com.atlast.data.dao.facade.impl.RoomDaoImpl
import com.atlast.data.dto.CreateRoomRequest
import com.atlast.data.dto.wrapper.AppResponse
import com.atlast.data.repository.impl.RoomRepositoryImpl
import com.atlast.services.RoomService
import com.atlast.utils.JWTExt
import com.atlast.utils.pageAndSize
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import org.jetbrains.exposed.sql.selectAll

fun Application.configureRoomRoutes() {

    val roomDao = RoomDaoImpl()
    val roomRepository = RoomRepositoryImpl(
        roomDao = roomDao
    )
    val roomService = RoomService(roomRepository)

    routing {

        get("/ownership") {//testing purposes only
            val roomId = call.parameters["roomId"]?.toInt()
            val response = DatabaseInstance.dbQuery {
                RoomMembers.selectAll().apply {
                    roomId?.let {
                        where {
                            RoomMembers.roomID eq roomId
                        }
                    }
                }.map {
                    mapOf(
                        "userId" to it[RoomMembers.userID].value.toString(),
                        "roomId" to it[RoomMembers.roomID].value.toString(),
                        "isHost" to it[RoomMembers.isHost].toString(),
                    )
                }
            }
            call.respond(
                message = response
            )
        }

        authenticate("auth-jwt") {

            post("/room") {
                val request = call.receive<CreateRoomRequest>()
                val userId = JWTExt.extractUserId(
                    principal = call.principal()
                )
                val response = roomService.createRoom(
                    userId = userId,
                    request = request
                )
                call.respond(
                    status = HttpStatusCode.Created,
                    message = AppResponse(
                        data = response,
                    )
                )
            }

            get("/room") {
                val (page, pageSize) = call.parameters.pageAndSize()
                val response = roomService.getRooms(
                    page = page,
                    pageSize = pageSize,
                )
                call.respond(
                    message = AppResponse(
                        data = response,
                    )
                )
            }

            get("/room/{id}") {
                val roomId = call.parameters["id"]?.toInt()!!
                val response = roomService.getRoom(
                    roomId = roomId,
                )
                call.respond(
                    message = AppResponse(
                        data = response,
                    )
                )
            }

            put("/room/{id}/join") {
                val roomId = call.parameters["id"]?.toInt()!!
                val userId = JWTExt.extractUserId(
                    principal = call.principal()
                )
                roomService.joinRoom(
                    userId = userId,
                    roomId = roomId,
                )
                call.respond(HttpStatusCode.Accepted)
            }

            put("/room/{id}/leave") {
                val roomId = call.parameters["id"]?.toInt()!!
                val userId = JWTExt.extractUserId(
                    principal = call.principal()
                )
                roomService.leaveRoom(
                    userId = userId,
                    roomId = roomId,
                )
                call.respond(HttpStatusCode.Accepted)
            }
        }
    }
}