package com.dhl.demp.mydmac.sso;

import android.content.Context;
import android.os.Bundle;

/**
 * Created by robielok on 11/14/2017.
 */

public class SendResponseSSOActionHandler extends AbsSSOActionHandler {
    private static final String PARAM_REFRESH_TOKEN = "refresh_token";

    public static Bundle createParams(boolean refreshToken) {
        Bundle params = new Bundle(1);
        params.putBoolean(PARAM_REFRESH_TOKEN, refreshToken);

        return params;
    }

    @Override
    public void onAction(Context context, String appId, String state, String callback, Bundle params) {
        if (params != null && params.getBoolean(PARAM_REFRESH_TOKEN, false)) {
            refreshTokenIfExpired(context);
        }

        String response = createStandardResponse(context, appId, state, callback);
        sendResponse(context, response);
    }
}
