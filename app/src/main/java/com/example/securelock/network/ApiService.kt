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
        @Query("deviceId") deviceId: Int,
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

    @POST("api/setup/install")
    suspend fun setupInstall(
        @Body request: SetupInstallRequest
    ): Response<SetupInstallResponse>

    @GET("api/admin/building/vehicles")
    suspend fun getAdminBuildingVehicles(
        @Query("adminUserId") adminUserId: Int
    ): Response<AdminBuildingVehiclesResponse>

    @POST("api/admin/vehicle/create")
    suspend fun createVehicle(
        @Body request: CreateVehicleRequest
    ): Response<GenericResponse>

    @POST("api/admin/vehicle/delete")
    suspend fun deleteVehicle(
        @Body request: DeleteVehicleRequest
    ): Response<GenericResponse>

    @POST("api/admin/slot/assignvehicle")
    suspend fun assignVehicleToSlot(
        @Body request: AssignVehicleRequest
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
    val adminUserId: Int
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
    val deviceId: Int,
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
    val deviceId: Int? = null,
    val status: String? = null,
    val hasKey: Boolean? = null,
    val lastUpdated: String? = null,
    val vehicleName: String? = null,
    val vehicleType: String? = null
)

data class SlotActionRequest(
    val userId: Int,
    val deviceId: Int,
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
    val slotId: Int,
    val deviceId: Int,
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

data class SetupInstallRequest(
    val superAdminId: Int,
    val buildingName: String,
    val buildingAddress: String,
    val lat: Double,
    val lng: Double,
    val adminFullName: String,
    val adminUsername: String,
    val adminPassword: String,
    val numberOfSlots: Int,
    val deviceUid: String
)

data class SetupInstallResponse(
    val success: Boolean,
    val message: String,
    val buildingId: Int? = null,
    val adminId: Int? = null,
    val slotsCreated: Int? = null
)

data class CreateVehicleRequest(
    val adminUserId: Int,
    val name: String,
    val type: String
)

data class DeleteVehicleRequest(
    val adminUserId: Int,
    val vehicleId: Int
)

data class AssignVehicleRequest(
    val adminUserId: Int,
    val slotId: Int,
    val vehicleId: Int?
)

data class AdminBuildingVehiclesResponse(
    val success: Boolean,
    val message: String,
    val buildingId: Int? = null,
    val vehicles: List<VehicleItem> = emptyList()
)

data class VehicleItem(
    val id: Int,
    val name: String,
    val type: String
)