package com.libri.app.data.remote.api

import com.libri.app.data.remote.dto.CreateReservationRequest
import com.libri.app.data.remote.dto.ReservationRemoteDto
import retrofit2.http.*

interface ReservationApi {
    @POST("api/reservations")
    suspend fun createReservation(@Body request: CreateReservationRequest): ReservationRemoteDto

    @DELETE("api/reservations/{id}")
    suspend fun cancelReservation(@Path("id") id: Long)

    @GET("api/reservations/user/{userId}")
    suspend fun getUserReservations(@Path("userId") userId: Long): List<ReservationRemoteDto>
}
