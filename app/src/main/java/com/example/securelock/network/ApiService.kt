package com.example.securelock.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// Chiamate HTTP

interface ApiService {
    // Invia face embedding.
    // Se autorizzato, darà accesso all'area personale
    @POST("api/auth/face")
    suspend fun authWithFace(
        @Body body: FaceAuthRequest
    ): Response<AuthResponse>

    // Conferma impronta verificata.
    // No dati biometrici: solo confermato + userId
    @POST("api/auth/fingerprint")
    suspend fun authWithFingerprint(
        @Body body: FingerprintAuthRequest
    ): Response<AuthResponse>

    // Login classico con username e password.
    @POST("api/auth/login")
    suspend fun login(
        @Body body: LoginRequest
    ): Response<AuthResponse>
}

// Modelli request
data class FaceAuthRequest(
    val embedding: List<Float>, // vettore 128val (di FaceNet)
)

data class FingerprintAuthRequest(
    val userId: Int,
)

data class LoginRequest(
    val username: String,
    val password: String
)

// Modelli response
data class AuthResponse(
    val success: Boolean,
    val message: String,
    val drawerId: Int?  // null => accesso negato
)