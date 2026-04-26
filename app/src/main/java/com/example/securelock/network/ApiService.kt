package com.example.securelock.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("api/auth/login/password")
    suspend fun login(
        @Body body: LoginRequest
    ): Response<AuthResponse>

    @POST("api/auth/login/face") //Login con volto
    suspend fun authWithFace(
        @Body body: FaceAuthRequest
    ): Response<AuthResponse>

    @POST("api/admin/insertuser") //Crea utente
    suspend fun createUser(
        @Body request: CreateUserRequest
    ): Response<CreateUserResponse>

    @POST("api/admin/face/checkduplicati") //Check duplicati volto
    suspend fun checkFace(
        @Body request: FaceCheckRequest
    ): Response<FaceCheckResponse>

    @POST("api/admin/face/save") //Salva volto
    suspend fun saveFace(
        @Body request: SaveFaceRequest
    ): Response<GenericResponse>

    @POST("api/admin/login")
    suspend fun adminLogin(
        @Body body: BackLoginRequest
    ): Response<AuthResponse>
}

data class FaceAuthRequest(
    val embedding: List<Float>
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class BackLoginRequest(
    val userId: Int
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
    val isAdmin: Boolean?,
    val url: String?
)

data class GenericResponse(
    val success: Boolean,
    val message: String,
    val userId: Int? = null,
    val userName: String? = null,
    val userRole: String? = null
)