package com.dhl.demp.mydmac.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.dhl.demp.dmac.ui.MainActivity;
import com.dhl.demp.mydmac.utils.Constants;
import com.dhl.demp.mydmac.utils.Utils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import mydmac.R;

/**
 *
 * Created by rd on 15/08/16.
 */

public class MyFirebaseNotificationService extends FirebaseMessagingService {
    private static final String TAG = MyFirebaseNotificationService.class.getCanonicalName();
    private static final String UPDATE_GROUP = "update_group";
    private static int notiID = 0;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);



        if (remoteMessage != null) {
            Map<String, String> data = remoteMessage.getData();

            String title = getApplicationContext().getString(R.string.app_name);
            String message = "default message";

            if (data != null) {
                if (data.containsKey("title")) {
                    title = data.get("title");
                }
                if (data.containsKey("message")) {
                    message = data.get("message");
                }
            }

            if (remoteMessage.getNotification() != null) {
                if(remoteMessage.getNotification().getTitle() != null) {
                    title = remoteMessage.getNotification().getTitle();
                }
                if(remoteMessage.getNotification().getBody() != null) {
                    message = remoteMessage.getNotification().getBody();
                }
            }

            Intent intent = new Intent(getApplicationContext() , MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            //pass all custom data
            if (data != null) {
                for (String key : data.keySet()) {
                    intent.putExtra(key, data.get(key));
                }
            }
            int requestCode = (int) System.currentTimeMillis();
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            Utils.createImportantNotificationChannel(this);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), Constants.NOTIFICATION_CHANNEL_IMPORTANT);
            Bitmap background = BitmapFactory.decodeResource(getResources(), R.drawable.background);

            NotificationCompat.WearableExtender wearableExtender =
                    new NotificationCompat.WearableExtender()
                            .setBackground(background);

            builder.setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setSmallIcon(R.drawable.ic_notification_wrapper)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setGroup(UPDATE_GROUP)
                    .extend(wearableExtender)
                    .setContentTitle(title)
                    .setContentIntent(pendingIntent)
                    .setContentText(message)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
            notificationManager.notify(notiID ,builder.build());

            Utils.log(TAG, "onMessageReceived: " + remoteMessage.getNotification() );
            Utils.log(TAG, "onNotificationData: " + remoteMessage.getData() );
            notiID++;

        }
    }
}
