package com.libri.app.data.remote.api

import com.libri.app.data.remote.dto.BookRemoteDto
import com.libri.app.data.remote.dto.BooksResponse
import com.libri.app.data.remote.dto.CreateBookRequest
import retrofit2.http.*

interface BookApi {
    @GET("api/books")
    suspend fun getBooks(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): BooksResponse

    @GET("api/books/{id}")
    suspend fun getBook(@Path("id") id: Long): BookRemoteDto

    @GET("api/books/search")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("status") status: String? = null
    ): BooksResponse

    @POST("api/books")
    suspend fun createBook(@Body book: CreateBookRequest): BookRemoteDto

    @PUT("api/books/{id}")
    suspend fun updateBook(@Path("id") id: Long, @Body book: BookRemoteDto): BookRemoteDto

    @DELETE("api/books/{id}")
    suspend fun deleteBook(@Path("id") id: Long)
}
