package com.dhl.demp.mydmac.service;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import com.dhl.demp.mydmac.sso.SSOService;
import com.dhl.demp.mydmac.utils.Constants;
import com.dhl.demp.mydmac.utils.Utils;

import mydmac.R;

/**
 * This IntentService supply situation on some devices that broadcast not wake up application. Huawei,Xiaomi devices example
 * Created by petlebed on 21/04/17.
 */

//NOTE!!!!! Don't move this class to any other package!!!! It is used outside the app by it's name in the next way:
//    Intent proxyIntent = new Intent();
//    proxyIntent.setComponent(new ComponentName("com.dhl.demp.dmac", "com.dhl.demp.mydmac.service.HelpBroadcastService"));
//    proxyIntent.putExtra("callIntent", intent);
//    startService(proxyIntent);
public class HelpBroadcastService extends IntentService {
    private static final String TAG = HelpBroadcastService.class.getCanonicalName();
    private static final int SSO_NOTIFICATION_ID = 117;

    public HelpBroadcastService() {
        super("HelpBroadcastService");

    }

    @Override
    public void onCreate() {
        super.onCreate();

        //For android API 26+ need to make this service foreground
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Utils.createImportantNotificationChannel(this);
            Notification notification = new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_IMPORTANT)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentText(getString(R.string.app_name))
                    .build();

            startForeground(SSO_NOTIFICATION_ID, notification);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Utils.log(TAG,"intent="+intent);
        Intent callIntent = (Intent)intent.getExtras().get("callIntent");

        SSOService.start(this, callIntent.getData());
    }
}