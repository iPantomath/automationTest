package com.dhl.demp.mydmac;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dhl.demp.mydmac.activity.LauncherActivity;
import com.dhl.demp.mydmac.utils.Utils;

import mydmac.BuildConfig;

public class AppUpgradedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!LauncherPreference.isLauncherSwitchedManually(context)) {
            Utils.setLauncherEnabled(context, true);

            //restart Launcher
            String launcherApp = Utils.getLauncherAppPackageName(context);
            if (BuildConfig.APPLICATION_ID.equals(launcherApp)) {
                context.startActivity(new Intent(context, LauncherActivity.class));
            }
        }
    }
}