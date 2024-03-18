package com.dhl.demp.mydmac.sso;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.dhl.demp.mydmac.LauncherPreference;
import com.dhl.demp.mydmac.activity.ChoosePasscodeActivity;
import com.dhl.demp.mydmac.activity.EnterPasscodeActivity;
import com.dhl.demp.mydmac.obj.PasscodeModel;

import static com.dhl.demp.mydmac.obj.PasscodeModel.PASSCODE_EXPIRATION_TIME_MIN;

/**
 * Created by robielok on 11/15/2017.
 */

public class ActivateSSOActionHandler extends AbsSSOActionHandler {
    @Override
    public void onAction(Context context, String appId, String state, String callback, Bundle params) {
        PasscodeModel passModel = null;
        if (!TextUtils.isEmpty(appId)) {
            passModel = LauncherPreference.getPasscodeModel(context, appId, true);
        }

        if (passModel == null || passModel.hash == null || passModel.hash.length() == 0) {
            //user haven't set a passcode yet
            if (LauncherPreference.getRefToken(context).isEmpty()) {
                String response = createStandardResponse(context, appId, state, callback);
                sendResponse(context, response);
            } else {
                startChoosePasscodeActivity(context, appId, state, callback);
            }
        } else {
            Long timediff = System.currentTimeMillis() - LauncherPreference.getLastPasscodeForAppId(context, appId, true);
            if (timediff > PASSCODE_EXPIRATION_TIME_MIN * 60 * 1000 || LauncherPreference.getLocked(context)) {
                startEnterPasscodeActivity(context, appId, state, callback);
            } else {
                LauncherPreference.setLastLoginForAppId(context, appId, true);

                refreshTokenIfExpired(context);

                String response = createStandardResponse(context, appId, state, callback);
                sendResponse(context, response);
            }
        }
    }

    private void startEnterPasscodeActivity(Context context, String appId, String state, String callback) {
        PendingIntent pendingAction = SSOService.createSendResponsePendingAction(context, appId, state, callback, true);
        EnterPasscodeActivity.start(context, pendingAction, Intent.FLAG_ACTIVITY_NEW_TASK);
    }


    private void startChoosePasscodeActivity(Context context, String appId, String state, String callback) {
        PendingIntent pendingAction = SSOService.createSendResponsePendingAction(context, appId, state, callback, true);
        ChoosePasscodeActivity.start(context, pendingAction, Intent.FLAG_ACTIVITY_NEW_TASK);
    }
}
