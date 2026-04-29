package com.example.securelock.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

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

    @GET("api/user/dashboard")
    suspend fun getDashboard(
        @Query("userId") userId: Int
    ): Response<DashboardResponse>

    @GET("api/slot/detail")
    suspend fun getSlotDetail(
        @Query("userId") userId: Int,
        @Query("slotId") slotId: Int
    ): Response<SlotDetailResponse>

    @POST("api/slot/action")
    suspend fun slotAction(
        @Body request: SlotActionRequest
    ): Response<GenericResponse>

    @GET("api/admin/building/slots")
    suspend fun getAdminBuildingSlots(
        @Query("adminUserId") adminUserId: Int
    ): Response<AdminBuildingSlotsResponse>

    @GET("api/admin/building/users")
    suspend fun getAdminBuildingUsers(
        @Query("adminUserId") adminUserId: Int
    ): Response<AdminBuildingUsersResponse>

    @POST("api/admin/user/delete")
    suspend fun deleteUser(
        @Body request: DeleteUserRequest
    ): Response<GenericResponse>

    @POST("api/admin/newAdmin")
    suspend fun createAdminUser(
        @Body request: CreateAdminRequest
    ): Response<GenericResponse>

    @POST("api/admin/newBuilding")
    suspend fun createBuilding(
        @Body request: CreateBuildingRequest
    ): Response<GenericResponse>

    @POST("api/admin/newSlots")
    suspend fun createAdminSlots(
        @Body request: CreateSlotsRequest
    ): Response<GenericResponse>
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
    val buildingId: Int?,
    val url: String?
)

data class DashboardResponse(
    val success: Boolean,
    val message: String,
    val userId: Int,
    val userName: String?,
    val username: String?,
    val userRole: String?,
    val isAdmin: Boolean,
    val buildingId: Int? = null,
    val slots: List<DashboardSlot> = emptyList()
)

data class DashboardSlot(
    val slotId: Int,
    val status: String,
    val hasKey: Boolean,
    val lastUpdated: String? = null,
    val vehicleName: String? = null,
    val vehicleType: String? = null
)

data class SlotDetailResponse(
    val success: Boolean,
    val message: String,
    val slotId: Int? = null,
    val status: String? = null,
    val hasKey: Boolean? = null,
    val lastUpdated: String? = null,
    val vehicleName: String? = null,
    val vehicleType: String? = null,
    val buildingId: Int? = null
)

data class SlotActionRequest(
    val userId: Int,
    val slotId: Int,
    val action: String
)

data class GenericResponse(
    val success: Boolean,
    val message: String,
    val userId: Int? = null,
    val userName: String? = null,
    val userRole: String? = null,
    val buildingId: Int? = null
)

data class AdminBuildingSlotsResponse(
    val success: Boolean,
    val message: String,
    val buildingId: Int? = null,
    val slots: List<AdminSlotItem> = emptyList()
)

data class AdminSlotItem(
    val id: Int,
    val status: String,
    val hasKey: Boolean,
    val vehicleName: String? = null,
    val vehicleType: String? = null
)

data class AdminBuildingUsersResponse(
    val success: Boolean,
    val message: String,
    val buildingId: Int? = null,
    val users: List<BuildingUserItem> = emptyList()
)

data class BuildingUserItem(
    val id: Int,
    val username: String,
    val name: String? = null
)

data class DeleteUserRequest(
    val adminUserId: Int,
    val userId: Int
)

data class CreateAdminRequest(
    val fullName: String,
    val username: String,
    val password: String
)

data class CreateBuildingRequest(
    val name: String,
    val address: String?
)

data class CreateSlotsRequest(
    val buildingId: Int,
    val numberOfSlots: Int
)