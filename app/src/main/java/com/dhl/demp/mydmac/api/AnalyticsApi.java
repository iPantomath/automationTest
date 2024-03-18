package com.dhl.demp.mydmac.api;

import com.dhl.demp.mydmac.api.request.AppDownloadEventRequest;
import com.dhl.demp.mydmac.api.request.AppInfoEventRequest;
import com.dhl.demp.mydmac.api.request.DmacApiEventRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface AnalyticsApi {
    @Headers("Accept: application/json")
    @POST("app_tracking/{app_id}")
    Call<Void> sendAppInfoEvent(@Path("app_id") String appId, @Header("Authorization") String accessToken, @Body AppInfoEventRequest body);

    @Headers("Accept: application/json")
    @POST("app_tracking/{app_id}")
    Call<Void> sendAppDownloadEvent(@Path("app_id") String appId, @Header("Authorization") String accessToken, @Body AppDownloadEventRequest body);

    @Headers("Accept: application/json")
    @POST("app_tracking/{app_id}")
    Call<Void> sendDmacApiEvent(@Path("app_id") String appId, @Header("Authorization") String accessToken, @Body DmacApiEventRequest body);
}
