package com.dhl.demp.mydmac.sso;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.dhl.demp.mydmac.LauncherApplication;
import com.dhl.demp.mydmac.LauncherPreference;
import com.dhl.demp.mydmac.api.ApiFactory;
import com.dhl.demp.mydmac.api.UnifiedApi;
import com.dhl.demp.mydmac.api.request.RefreshTokenRequest;
import com.dhl.demp.mydmac.api.response.RefreshTokenResponse;
import com.dhl.demp.mydmac.obj.DeviceInfo;
import com.dhl.demp.mydmac.obj.RefreshAccessTokenErrorResponse;
import com.dhl.demp.mydmac.utils.Utils;

import java.io.IOException;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by robielok on 11/13/2017.
 */

public abstract class AbsSSOActionHandler implements SSOActionHandler {
    private static final String RESPONSE_PATTERN = "%s?state=%s&pin_verified=1&user_active=%d&user_deactivated=%d&token_expired=%d&app_valid=%d";
    protected static final int TRUE = 1;
    protected static final int FALSE = 0;

    protected void sendResponse(Context context, String response) {
        Uri uri = Uri.parse(response);
        if (!Utils.sendDMACCallback(context, uri, null)) {
            showToast(context, "Not valid callback : " + response.substring(0, response.indexOf("?")), Toast.LENGTH_LONG);
        }
    }

    /**
     * Method refreshes an access token if it was expired. Be careful, this method does a blocking network call, so don't call it in the main thread
     * @param context
     */
    protected void refreshTokenIfExpired(Context context) {
        long prefTokenExpiry = LauncherPreference.getExpiresIn(context);
        if (Calendar.getInstance().getTimeInMillis() > prefTokenExpiry) {
            refreshToken(context);
        }
    }

    protected String createStandardResponse(Context ctx, String appId, String state, String callback) {
        //pokud refresh token neni platny token_expired = 1
        int userActive = LauncherPreference.getRefToken(ctx).isEmpty() ? FALSE : TRUE;
        int userDeactivated = LauncherPreference.getUserActivated(ctx) ? FALSE : TRUE;
        int appValid = isValidApp(ctx, appId);

        //pokud access token neni platny user_active=0, pokus o refresh
        int tokenExpired;
        long prefTokenExpiry = LauncherPreference.getExpiresIn(ctx);
        if (Calendar.getInstance().getTimeInMillis() > prefTokenExpiry || (LauncherPreference.getTokenExpired(ctx)) ) {
            tokenExpired = TRUE;
        } else {
            tokenExpired = FALSE;
        }

        String response = String.format(RESPONSE_PATTERN, callback, state, userActive, userDeactivated, tokenExpired, appValid);
        response = addChecksum(response);
        return response;
    }

    protected int isValidApp(Context ctx, String appId) {
        LauncherApplication launcherApplication = (LauncherApplication) ctx.getApplicationContext();

        if(Utils.appListExpired(ctx)) {
            launcherApplication.appRepositoryLegacy.get().fetchAndSaveAppsList();
        }

        boolean appExists = launcherApplication.appRepositoryLegacy.get().hasApp(appId);

        return appExists ? TRUE : FALSE;
    }

    protected String addChecksum(String response) {
        String checksum = "&cs=" + Utils.getChecksum(response);
        return response + checksum;
    }

    private void refreshToken(Context context) {
        String refreshToken = LauncherPreference.getRefToken(context);
        DeviceInfo deviceInfo = Utils.buildDeviceInfo(context);
        UnifiedApi api = ApiFactory.buildUnifiedApi();

        Call<RefreshTokenResponse> refreshTokenRequest = api.refreshToken(
                new RefreshTokenRequest(refreshToken, ApiFactory.getScope(), deviceInfo),
                ApiFactory.getApiKey());
        try {
            Response<RefreshTokenResponse> response = refreshTokenRequest.execute();
            if (response.isSuccessful()) {
                Utils.updateTokens(context, response.body());
            } else {
                //try to relogin
                boolean wasReloginned = false;
                if (Utils.canAutoRelogin(context, response.code())) {
                    wasReloginned = Utils.autoReloginBlocking(context);
                }

                if (wasReloginned) {
                    try {
                        RefreshAccessTokenErrorResponse errorResponse = ApiFactory.gson.fromJson(response.errorBody().string(), RefreshAccessTokenErrorResponse.class);
                        if (errorResponse.needLogoutUser()) {
                            Utils.deleteAllTokens(context);
                        }
                    } catch (Exception e) {
                    }
                }
            }
        } catch (IOException e) {
        }
    }

    private void showToast(final Context context, final String text, final int duration) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, text, duration).show();
            }
        });
    }
}
