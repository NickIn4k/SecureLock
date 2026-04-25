package com.example.securelock.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("api/auth/login")
    suspend fun login(
        @Body body: LoginRequest
    ): Response<AuthResponse>

    @POST("api/auth/face")
    suspend fun authWithFace(
        @Body body: FaceAuthRequest
    ): Response<AuthResponse>

    @POST("api/admin/users")
    suspend fun createUser(
        @Body request: CreateUserRequest
    ): Response<CreateUserResponse>

    @POST("api/admin/users/face/check")
    suspend fun checkFace(
        @Body request: FaceCheckRequest
    ): Response<FaceCheckResponse>

    @POST("api/admin/users/face")
    suspend fun saveFace(
        @Body request: SaveFaceRequest
    ): Response<GenericResponse>
}

data class FaceAuthRequest(
    val embedding: List<Float>
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class CreateUserRequest(
    val adminUserId: Int,
    val fullName: String,
    val username: String,
    val password: String,
    val slotIds: List<Int> = emptyList()
)

data class CreateUserResponse(
    val success: Boolean,
    val message: String,
    val userId: Int? = null
)

data class FaceCheckRequest(
    val faceEmbedding: List<Float>
)

data class FaceCheckResponse(
    val success: Boolean,
    val duplicate: Boolean,
    val matchedUserId: Int? = null,
    val message: String
)

data class SaveFaceRequest(
    val userId: Int,
    val faceEmbedding: List<Float>
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val userId: Int?,
    val userName: String?,
    val userRole: String?,
    val isAdmin: Boolean?
)

data class GenericResponse(
    val success: Boolean,
    val message: String,
    val userId: Int? = null,
    val userName: String? = null,
    val userRole: String? = null
)