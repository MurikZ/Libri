package com.libri.app.data.remote.api

import com.libri.app.data.remote.dto.LoginRequest
import com.libri.app.data.remote.dto.LoginResponse
import com.libri.app.data.remote.dto.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): LoginResponse
}
