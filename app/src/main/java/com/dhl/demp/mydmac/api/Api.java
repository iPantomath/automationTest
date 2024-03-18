package com.dhl.demp.mydmac.api;

import com.dhl.demp.mydmac.api.request.ApproveRequestBody;
import com.dhl.demp.mydmac.api.response.ApproveResponse;
import com.dhl.demp.mydmac.api.response.NotificationResponse;
import com.dhl.demp.mydmac.obj.DeviceAndSimOwner;
import com.dhl.demp.mydmac.obj.DeviceInfo;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.*;

/**
 * Created by jahofman on 3/3/2016.
 */
public interface Api {
    @Headers({
            "Accept: application/json",
            "Connection: close"
    })
    @GET("v2/apps-data/api/applications")
    Call<JsonObject> getAppList(@Header("Authorization") String accessToken);

    @GET("v2/apps-data/api/applications/{app}")
    Call<JsonObject> getAppDetail(@Path("app") String appId, @Header("Authorization") String accessToken);

    @Headers("Accept: application/json")
    @POST("v2/approval-gsn-bbd/api/approve_user")
    Call<ApproveResponse> requestApprove(@Body ApproveRequestBody body, @Header("Authorization") String accessToken);


    @Headers("Accept: application/json")
    @PUT("v2/registration/api/registered_device/{clientId}/device_info")
    Call<Void> reportDeviceInf(
            @Header("Authorization") String accessToken,
            @Path("clientId") String clientId,
            @Body DeviceInfo deviceInfo
    );

    @Headers({
            "Accept: application/json",
            "Connection: Close"
    })
    @GET("v2/notification/api/devices/{clientId}/notifications")
    Call<NotificationResponse> getNotifications(
            @Header("Authorization") String accessToken,
            @Path("clientId") String clientId,
            @Query("limit") int limit,
            @Query("offset") int offset
    );

    @Headers({
            "Accept: application/json",
            "Connection: close"
    })
    @PATCH("v2/registration/api/registered_device/{clientId}")
    Call<Void> updateDeviceAndSimOwner(
            @Header("Authorization") String accessToken,
            @Path("clientId") String clientId,
            @Body DeviceAndSimOwner deviceAndSimOwner
    );
}
