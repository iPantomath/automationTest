package com.dhl.demp.mydmac.sso;

import android.content.Context;
import android.os.Bundle;

import com.dhl.demp.mydmac.LauncherPreference;
import com.dhl.demp.mydmac.utils.Utils;

/**
 * Created by robielok on 11/14/2017.
 */

public class TokenSSOActionHandler extends AbsSSOActionHandler {
    private static final String VALID_APP_RESPONSE_PATTERN = "%s?state=%s&client_id=%s&hash_token=%s&app_valid=%d";
    private static final String INVALID_APP_RESPONSE_PATTERN = "%s?state=%s&app_valid=%d";

    @Override
    public void onAction(Context context, String appId, String state, String callback, Bundle params) {
        //first try to refresh the token
        refreshTokenIfExpired(context);

        String response = createResponse(context, appId, callback, state);
        sendResponse(context, response);
    }

    private String createResponse(Context ctx, String appId, String callback, String state) {
        int appValid = isValidApp(ctx, appId);
        String response;

        if (appValid == TRUE) {
            String clientId = LauncherPreference.getId(ctx);
            String token = LauncherPreference.getRefToken(ctx);
            String hashToken;

            if(LauncherPreference.getHashToken(ctx)){
                hashToken = "";
            }else{
                hashToken = Utils.generateHashToken(token, clientId, appId);
            }

            response = String.format(VALID_APP_RESPONSE_PATTERN, callback, state, clientId, hashToken, appValid);
        } else {
            response = String.format(INVALID_APP_RESPONSE_PATTERN, callback, state, appValid);
        }

        response = addChecksum(response);
        return  response;
    }
}
