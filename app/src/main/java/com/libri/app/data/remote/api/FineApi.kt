package com.libri.app.data.remote.api

import com.libri.app.data.remote.dto.FineRemoteDto
import retrofit2.http.*

interface FineApi {
    @GET("api/fines/user/{userId}")
    suspend fun getUserFines(@Path("userId") userId: Long): List<FineRemoteDto>

    @GET("api/fines/user/{userId}/unpaid")
    suspend fun getUnpaidFines(@Path("userId") userId: Long): List<FineRemoteDto>

    @PUT("api/fines/{id}/pay")
    suspend fun payFine(@Path("id") id: Long): FineRemoteDto
}
