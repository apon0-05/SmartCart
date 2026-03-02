package com.example.smartcard.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApi {

    @POST("auth/register")
    suspend fun register(@Body req: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body req: LoginRequest): AuthResponse

}
