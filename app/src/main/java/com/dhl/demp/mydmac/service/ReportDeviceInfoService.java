package com.dhl.demp.mydmac.service;

import android.app.IntentService;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.dhl.demp.mydmac.LauncherPreference;
import com.dhl.demp.mydmac.api.Api;
import com.dhl.demp.mydmac.api.ApiFactory;
import com.dhl.demp.mydmac.obj.DeviceInfo;
import com.dhl.demp.mydmac.utils.Utils;

import java.io.IOException;

import mydmac.R;
import retrofit2.Call;
import retrofit2.Response;

public class ReportDeviceInfoService extends IntentService {
    private static final String TAG = "ReportDeviceInfoService";
    private static final int RDI_NOTIFICATION_ID = 118;

    public static void start(Context context) {
        Intent starter = new Intent(context, ReportDeviceInfoService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(starter);
        } else {
            context.startService(starter);
        }
    }

    public ReportDeviceInfoService() {
        super("ReportDeviceInfoService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //For android API 26+ need to make this service foreground
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = Utils.createFGServiceNotificationChannel(this);

            Notification notification = new NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentText(getString(R.string.app_name))
                    .build();

            startForeground(RDI_NOTIFICATION_ID, notification);
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Utils.logI(TAG, "Report device info started");

        String token = LauncherPreference.getToken(this);
        if (TextUtils.isEmpty(token)) {
            //user is not logged in
            return;
        }

        //check if data changed
        DeviceInfo info = new DeviceInfo(this);
        DeviceInfo reportedInfo = LauncherPreference.loadReportedDeviceInfo(this);

        if (!info.equals(reportedInfo)) {
            reportDeviceInfoChanged(info, token);
        }
    }

    private void reportDeviceInfoChanged(DeviceInfo deviceInfo, String token) {
        String clientId = LauncherPreference.getId(this);
        Api api = ApiFactory.buildApi();

        Call<Void> request = api.reportDeviceInf("Bearer " + token, clientId, deviceInfo);
        try {
            Response response = request.execute();
            if (response.isSuccessful()) {
                LauncherPreference.saveReportedDeviceInfo(this, deviceInfo);
            }
        } catch (IOException e) {
            Utils.logE(TAG, "Failed to report device info: " + e.toString());
        }
    }
}


