package com.libri.app.data.remote.api

import com.libri.app.data.remote.dto.BorrowRequest
import com.libri.app.data.remote.dto.LoanRemoteDto
import com.libri.app.data.remote.dto.ReturnResponse
import retrofit2.http.*

interface LoanApi {
    @POST("api/loans")
    suspend fun borrowBook(@Body request: BorrowRequest): LoanRemoteDto

    @PUT("api/loans/{id}/return")
    suspend fun returnBook(@Path("id") id: Long): ReturnResponse

    @GET("api/loans/user/{userId}")
    suspend fun getUserLoans(@Path("userId") userId: Long): List<LoanRemoteDto>

    @GET("api/loans/active")
    suspend fun getActiveLoans(): List<LoanRemoteDto>
}
