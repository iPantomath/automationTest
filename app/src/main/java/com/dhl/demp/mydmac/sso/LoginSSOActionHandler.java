package com.dhl.demp.mydmac.sso;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.dhl.demp.mydmac.activity.EnterPasscodeActivity;

/**
 * Created by robielok on 11/15/2017.
 */

public class LoginSSOActionHandler extends AbsSSOActionHandler {
    @Override
    public void onAction(Context context, String appId, String state, String callback, Bundle params) {
        PendingIntent pendingAction = SSOService.createSendResponsePendingAction(context, appId, state, callback, true);
        EnterPasscodeActivity.start(context, pendingAction, Intent.FLAG_ACTIVITY_NEW_TASK);
    }
}
