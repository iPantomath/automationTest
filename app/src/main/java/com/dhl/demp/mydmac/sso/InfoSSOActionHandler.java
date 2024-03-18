package com.dhl.demp.mydmac.sso;

import android.app.PendingIntent;
import android.content.Context;
import android.os.Bundle;

import com.dhl.demp.mydmac.LauncherPreference;
import com.dhl.demp.mydmac.service.RefresherService;

import java.util.Calendar;

/**
 * Created by robielok on 11/14/2017.
 */

public class InfoSSOActionHandler extends AbsSSOActionHandler {
    @Override
    public void onAction(Context context, String appId, String state, String callback, Bundle params) {
        long prefTokenExpiry = LauncherPreference.getExpiresIn(context);
        if (Calendar.getInstance().getTimeInMillis() > prefTokenExpiry) {
            //first try to update an access token
            PendingIntent pendingAction = SSOService.createSendResponsePendingAction(context, appId, state, callback, false);
            RefresherService.start(context, pendingAction);
        } else {
            String response = createStandardResponse(context, appId, state, callback);
            sendResponse(context, response);
        }
    }
}
