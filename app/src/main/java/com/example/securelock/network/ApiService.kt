package com.example.securelock.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    // Endpoint retrofit
    @POST("api/auth/face")
    suspend fun authWithFace(
        @Body body: FaceAuthRequest
    ): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(
        @Body body: LoginRequest
    ): Response<AuthResponse>

    @POST("/api/admin/users")
    suspend fun createUser(
        @Body request: CreateUserRequest
    ): Response<GenericResponse>
}

// Modelli request

data class FaceAuthRequest(
    val embedding: List<Float>
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class CreateUserRequest(
    val fullName: String,
    val username: String,
    val password: String,
    val faceEmbedding: List<Float>? = null,
    val drawerIds: List<Int> = emptyList()
)

// Modelli response

// Risposta uguale per tutti e tre i metodi di autenticazione
data class AuthResponse(
    val success: Boolean,
    val message: String,
    val userId: Int?,      // ID dell'utente nel DB => null se accesso negato
    val userName: String?  // Nome dell'utente => null se accesso negato
)

data class GenericResponse(
    val success: Boolean,
    val message: String,
    val userId: Int? = null,
    val userName: String? = null
)