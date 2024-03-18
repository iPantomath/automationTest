package com.dhl.demp.mydmac.utils;

import android.content.Context;
import com.dhl.demp.mydmac.api.ApiFactory;
import com.dhl.demp.mydmac.api.response.RefreshTokenResponse;
import com.dhl.demp.mydmac.obj.RefreshAccessTokenErrorResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by robielok on 6/30/2017.
 */

public abstract class RefreshAccessTokenCallback implements Callback<RefreshTokenResponse> {
    private static final String TAG = "RefreshAccessTokenCallb";

    private final Context context;

    public RefreshAccessTokenCallback(Context context) {
        //just use application context to avoid memory leaks
        this.context = context.getApplicationContext();
    }

    @Override
    public void onResponse(Call<RefreshTokenResponse> call, Response<RefreshTokenResponse> response) {
        if (response == null) {
            return;
        }

        if (response.isSuccessful()) {
            //save new tokens
            Utils.updateTokens(context, response.body());

            onTokenRefreshed();
        } else {
            try {
                RefreshAccessTokenErrorResponse errorResponse = ApiFactory.gson.fromJson(response.errorBody().string(), RefreshAccessTokenErrorResponse.class);
                onError(errorResponse.error, errorResponse.needLogoutUser());
            } catch (Exception e) {
                onError(e.getMessage(), false);
            }
        }

        onRequestDone();
    }

    @Override
    public void onFailure(Call<RefreshTokenResponse> call, Throwable t) {
        Utils.logE(TAG, "onFailure" + t.getMessage());

        onFailure(t.getMessage());
    }

    protected void onRequestDone() {
        //default empty behaviour
    }

    protected abstract void onTokenRefreshed();

    protected void onFailure(String message) {
        //default empty behaviour
    }

    protected void onError(String error, boolean logoutUser) {
        //default empty behaviour
    }
}
