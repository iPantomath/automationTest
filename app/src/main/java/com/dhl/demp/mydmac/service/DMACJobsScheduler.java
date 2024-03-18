package com.dhl.demp.mydmac.service;

import android.content.Context;

/**
 * Created by robielok on 3/21/2018.
 */

public class DMACJobsScheduler {
    public static void cancelAll(Context context) {
        DMACJobService.cancelAll(context);
    }

    public static void scheduleRefreshToken(Context context) {
        DMACJobService.refreshToken(context);
    }

    public static void scheduleSendDeviceInfo(Context context, long intervalMillis) {
        DMACJobService.sendDeviceInfo(context, intervalMillis);
    }
}
