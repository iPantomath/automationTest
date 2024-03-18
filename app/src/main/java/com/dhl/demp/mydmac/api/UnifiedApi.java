package com.dhl.demp.mydmac.api;

import com.dhl.demp.mydmac.api.request.GetPinRequest;
import com.dhl.demp.mydmac.api.request.GetTokenRequest;
import com.dhl.demp.mydmac.api.request.RefreshTokenRequest;
import com.dhl.demp.mydmac.api.request.RegistrationRequest;
import com.dhl.demp.mydmac.api.request.UnregisterRequest;
import com.dhl.demp.mydmac.api.response.GetPinResponse;
import com.dhl.demp.mydmac.api.response.GetTokenResponse;
import com.dhl.demp.mydmac.api.response.RefreshTokenResponse;
import com.dhl.demp.mydmac.api.response.RegistrationResponse;

import mydmac.BuildConfig;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface UnifiedApi {
    @Headers("Accept: application/json")
    @POST(BuildConfig.DMAC_APP_ID)
    Call<RegistrationResponse> registration(@Body RegistrationRequest body, @Header("Authorization") String apiKey);

    @Headers("Accept: application/json")
    @POST(BuildConfig.DMAC_APP_ID)
    Call<GetTokenResponse> getToken(@Body GetTokenRequest body, @Header("Authorization") String apiKey);

    @Headers("Accept: application/json")
    @POST(BuildConfig.DMAC_APP_ID)
    Call<GetTokenResponse> autoReg(@Body GetTokenRequest body, @Header("Authorization") String apiKey); //grantType == dmac-auto, username = IMEI, password = hashed-IMEI+S/N, device info extentended for express

    @Headers("Accept: application/json")
    @POST(BuildConfig.DMAC_APP_ID)
    Call<GetPinResponse> getPin(@Body GetPinRequest body, @Header("Authorization") String apiKey);

    @Headers("Accept: application/json")
    @POST(BuildConfig.DMAC_APP_ID)
    Call<RefreshTokenResponse> refreshToken(@Body RefreshTokenRequest body, @Header("Authorization") String apiKey);

    @Headers("Accept: application/json")
    @POST(BuildConfig.DMAC_APP_ID)
    Call<Void> unregister(@Body UnregisterRequest body, @Header("Authorization") String apiKey);
}


