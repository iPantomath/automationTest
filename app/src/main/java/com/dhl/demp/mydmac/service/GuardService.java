package com.dhl.demp.mydmac.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.dhl.demp.mydmac.activity.ActivitySplash;
import com.dhl.demp.mydmac.utils.Constants;
import com.dhl.demp.mydmac.utils.Utils;

import mydmac.R;

/**
 * Created by petlebed on 7/26/2017.
 */
//DMAC-341

public class GuardService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags,
                              final int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public void onTaskRemoved(Intent intent) {

        Intent i = new Intent(getApplicationContext() , ActivitySplash.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 6, i, PendingIntent.FLAG_CANCEL_CURRENT);

        Utils.createImportantNotificationChannel(this);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), Constants.NOTIFICATION_CHANNEL_IMPORTANT);
        Bitmap background = BitmapFactory.decodeResource(getResources(), R.drawable.background);

        NotificationCompat.WearableExtender wearableExtender =
                new NotificationCompat.WearableExtender()
//                            .setDismissalId("com.dhl.dmac")
                        .setBackground(background);

        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_notification_wrapper)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentTitle(getString(R.string.app_name))
                .setContentIntent(pendingIntent)
                .setContentText(getString(R.string.not_stopped_properly));

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        notificationManager.notify(0,builder.build());
        super.onTaskRemoved(intent);
    }
}
