package com.example.smartcard.data.remote

data class RegisterRequest(
    val full_name: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val id: Int,
    val full_name: String,
    val email: String
)
