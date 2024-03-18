package com.dhl.demp.mydmac.service;

/**
 * Created by robielok on 10/12/2017.
 */

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.Nullable;

import com.dhl.demp.mydmac.LauncherPreference;
import com.dhl.demp.mydmac.api.ApiFactory;
import com.dhl.demp.mydmac.api.UnifiedApi;
import com.dhl.demp.mydmac.api.request.RefreshTokenRequest;
import com.dhl.demp.mydmac.api.response.RefreshTokenResponse;
import com.dhl.demp.mydmac.obj.DeviceInfo;
import com.dhl.demp.mydmac.obj.RefreshAccessTokenErrorResponse;
import com.dhl.demp.mydmac.utils.Utils;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

/**
 * This service is used to refresh an access token before sending "info"(@see {@link com.dhl.demp.mydmac.DMACAuthReceiver}) response
 */
public class RefresherService extends IntentService {
    private static final String TAG = "RefresherService";
    public static final String EXTRA_PENDING_ACTION = "pending_action";

    public static void start(Context context, @Nullable PendingIntent pendingAction) {
        Intent starter = new Intent(context, RefresherService.class);

        if (pendingAction != null) {
            starter.putExtra(EXTRA_PENDING_ACTION, pendingAction);
        }

        context.startService(starter);
    }

    public static Intent createIntent(Context context) {
        return new Intent(context, RefresherService.class);
    }

    public RefresherService() {
        super("RefresherService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }

        String refreshToken = LauncherPreference.getRefToken(this);
        DeviceInfo deviceInfo = Utils.buildDeviceInfo(this);
        UnifiedApi api = ApiFactory.buildUnifiedApi();

        Call<RefreshTokenResponse> refreshTokenRequest = api.refreshToken(
                new RefreshTokenRequest(refreshToken, ApiFactory.getScope(), deviceInfo),
                ApiFactory.getApiKey());
        try {
            Response<RefreshTokenResponse> response = refreshTokenRequest.execute();
            if (response.isSuccessful()) {
                processResponse(response);
            }
        } catch (IOException e) {
            //in case of the connectivity error - schedule an update of a token for the moment when the device will have an internet connection
            scheduleRefreshLater();
        }

        if (intent.hasExtra(EXTRA_PENDING_ACTION)) {
            PendingIntent pendingAction = intent.getParcelableExtra(EXTRA_PENDING_ACTION);

            performPendingAction(pendingAction);
        }
    }

    private void performPendingAction(PendingIntent pendingAction) {
        try {
            pendingAction.send();
        } catch (PendingIntent.CanceledException e) {
            Utils.logE(TAG, "Error with pending action " + e.getMessage());
        }
    }

    private void processResponse(Response<RefreshTokenResponse> response) {
        if (response.isSuccessful()) {
            Utils.updateTokens(this, response.body());
        } else {
            //try to relogin
            boolean wasReloginned = false;
            if (Utils.canAutoRelogin(this, response.code())) {
                wasReloginned = Utils.autoReloginBlocking(this);
            }

            if (!wasReloginned) {
                try {
                    RefreshAccessTokenErrorResponse errorResponse = ApiFactory.gson.fromJson(response.errorBody().string(), RefreshAccessTokenErrorResponse.class);
                    if (errorResponse.needLogoutUser()) {
                        Utils.deleteAllTokens(this);
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    private void scheduleRefreshLater() {
        DMACJobsScheduler.scheduleRefreshToken(this);
    }
}
