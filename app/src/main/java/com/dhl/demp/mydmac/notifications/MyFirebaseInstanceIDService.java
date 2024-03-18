package com.dhl.demp.mydmac.notifications;

import android.text.TextUtils;

import com.dhl.demp.mydmac.LauncherPreference;
import com.dhl.demp.mydmac.api.ApiFactory;
import com.dhl.demp.mydmac.api.UnifiedApi;
import com.dhl.demp.mydmac.api.request.RefreshTokenRequest;
import com.dhl.demp.mydmac.api.response.RefreshTokenResponse;
import com.dhl.demp.mydmac.obj.DeviceInfo;
import com.dhl.demp.mydmac.utils.Utils;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by rd on 15/08/16.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = MyFirebaseInstanceIDService.class.getCanonicalName();

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        String oldToken = LauncherPreference.getFCMToken(this);
        String token = FirebaseInstanceId.getInstance().getToken();
        Utils.log(TAG, "pushToken FCM Token: " + token);
        if (token != null) {
            LauncherPreference.setFCMToken(getApplicationContext(), token);
        }

        String refreshToken = LauncherPreference.getRefToken(this);
        //if user is logged in and the we have the new token
        if (!TextUtils.isEmpty(refreshToken) && !Utils.equals(token, oldToken)) {
            sendFirebaseToken(refreshToken);
        }
    }

    private void sendFirebaseToken(String refreshToken) {
        //workaround - update push token by sending refresh_token request
        UnifiedApi api = ApiFactory.buildUnifiedApi();
        DeviceInfo deviceInfo = Utils.buildDeviceInfo(this);
        api.refreshToken(
                new RefreshTokenRequest(refreshToken, ApiFactory.getScope(), deviceInfo),
                ApiFactory.getApiKey()
        ).enqueue(new Callback<RefreshTokenResponse>() {
            @Override
            public void onResponse(Call<RefreshTokenResponse> call, Response<RefreshTokenResponse> response) { /*nothing to do*/ }

            @Override
            public void onFailure(Call<RefreshTokenResponse> call, Throwable t) { /*nothing to do*/ }
        });
    }
}
