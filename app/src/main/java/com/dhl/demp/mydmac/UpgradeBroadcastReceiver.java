package com.dhl.demp.mydmac;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dhl.demp.mydmac.service.ReportDeviceInfoService;

public class UpgradeBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ReportDeviceInfoService.start(context);
    }
}
