package com.atlast.routes

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

fun Application.configureRoomRoutes() {

    val roomDao = RoomDaoImpl()
    val roomRepository = RoomRepositoryImpl(
        roomDao = roomDao
    )
    val roomService = RoomService(roomRepository)

    routing {
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

            put("/room/join") {
                roomService.joinRoom(
                    roomId = 1,
                )
                call.respond(HttpStatusCode.Accepted)
            }
        }
    }
}