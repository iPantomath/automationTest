package com.dhl.demp.dmac.data.network

import com.dhl.demp.dmac.data.network.dto.AppInfoExtendedDTO
import com.dhl.demp.dmac.data.network.dto.AppInfoListDTO
import com.dhl.demp.dmac.data.network.dto.ApproveRequestDTO
import com.dhl.demp.dmac.data.network.dto.ApproveResponseDTO
import com.dhl.demp.mydmac.api.response.NotificationResponse
import com.dhl.demp.mydmac.obj.DeviceAndSimOwner
import retrofit2.Response
import retrofit2.http.*

interface AppService {
    @GET("v2/apps-data/api/applications")
    suspend fun getAppList(): Response<AppInfoListDTO>

    @GET("v2/apps-data/api/applications/{app}")
    suspend fun getAppDetails(@Path("app") appId: String): Response<AppInfoExtendedDTO>

    @POST("v2/approval-gsn-bbd/api/approve_user")
    suspend fun requestApprove(@Body body: ApproveRequestDTO): Response<ApproveResponseDTO>

    @Headers("Connection: close")
    @PATCH("v2/registration/api/registered_device/{clientId}")
    suspend fun updateDeviceAndSimOwner(
        @Path("clientId") clientId: String,
        @Body body: DeviceAndSimOwner
    ): Response<Void>

    @Headers("Connection: close")
    @GET("v2/notification/api/devices/{clientId}/notifications")
    suspend fun getNotifications(
        @Path("clientId") clientId: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
    ): Response<NotificationResponse>
}