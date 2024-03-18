package com.dhl.demp.mydmac.sso;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.core.app.NotificationCompat;
import android.widget.Toast;

import com.dhl.demp.mydmac.analytics.Analytics;
import com.dhl.demp.mydmac.utils.Constants;
import com.dhl.demp.mydmac.utils.Utils;

import java.util.HashSet;
import java.util.Set;

import mydmac.R;

/**
 * Created by robielok on 11/13/2017.
 */

public class SSOService extends IntentService {
    private static String TAG = SSOService.class.getCanonicalName();
    private static final int SSO_NOTIFICATION_ID = 117;
    private static final String EXTRA_ACTION = "action";
    private static final String EXTRA_APP_ID = "app_id";
    private static final String EXTRA_STATE = "state";
    private static final String EXTRA_CALLBACK = "callback";
    private static final String EXTRA_PARAMS = "params";
    private static final String ACTION_TOKEN = "token";
    private static final String ACTION_ACTIVATE = "activate";
    private static final String ACTION_LOGIN = "login";
    private static final String ACTION_INFO = "info";
    private static final String ACTION_SEND_RESPONSE = "send_response";

    private static final String QP_APP_ID = "appid";
    private static final String QP_STATE = "state";
    private static final String QP_CALLBACK = "callBackURL";

    private static final ArrayMap<String, SSOActionHandler> handlers = new ArrayMap() {{
        put(ACTION_ACTIVATE, new ActivateSSOActionHandler());
        put(ACTION_TOKEN, new TokenSSOActionHandler());
        put(ACTION_LOGIN, new LoginSSOActionHandler());
        put(ACTION_INFO, new InfoSSOActionHandler());
        put(ACTION_SEND_RESPONSE, new SendResponseSSOActionHandler());
    }};

    private static final Set<String> actionsForAnalytics = new HashSet() {{ add(ACTION_ACTIVATE); add(ACTION_TOKEN); add(ACTION_LOGIN); add(ACTION_INFO); }};

    public static void start(Context context, Uri data) {
        //parse request params
        String action = data.getHost();
        String appId = data.getQueryParameter(QP_APP_ID);
        String state = data.getQueryParameter(QP_STATE);
        if (state == null) {
            state = "";
        }
        String callback = data.getQueryParameter(QP_CALLBACK);

        //create start intent
        Intent starter = new Intent(context, SSOService.class);

        starter.putExtra(EXTRA_ACTION, action);
        starter.putExtra(EXTRA_APP_ID, appId);
        starter.putExtra(EXTRA_STATE, state);
        starter.putExtra(EXTRA_CALLBACK, callback);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(starter);
        } else {
            context.startService(starter);
        }
    }

    public static PendingIntent createSendResponsePendingAction(Context context, String appId, String state,
                                                                String callback, boolean refreshToken) {
        Intent intent = new Intent(context, SSOService.class);
        intent.putExtra(EXTRA_ACTION, ACTION_SEND_RESPONSE);
        intent.putExtra(EXTRA_APP_ID, appId);
        intent.putExtra(EXTRA_STATE, state);
        intent.putExtra(EXTRA_CALLBACK, callback);
        intent.putExtra(EXTRA_PARAMS, SendResponseSSOActionHandler.createParams(refreshToken));

        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
    }

    public SSOService() {
        super("SSOService");
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
    protected void onHandleIntent(@Nullable Intent intent) {
        String action = intent.getStringExtra(EXTRA_ACTION);
        String appId = intent.getStringExtra(EXTRA_APP_ID);
        String state = intent.getStringExtra(EXTRA_STATE);
        String callback = intent.getStringExtra(EXTRA_CALLBACK);
        Bundle params = intent.getBundleExtra(EXTRA_PARAMS);

        if (actionsForAnalytics.contains(action)) {
            Analytics.sendDmacApiEvent(action, appId);
        }

        Utils.log(TAG, "SSOService called with params[" + action + "," + appId + "," + state + "," + callback + "]");

        SSOActionHandler handler = handlers.get(action);
        if (handler != null) {
            handler.onAction(this, appId, state, callback, params);
        } else {
            Utils.logE(TAG, "Handler for the action " + action + " was not found");
            showToast("Unknown action \"" + action + "\"", Toast.LENGTH_LONG);
        }
    }

    private void showToast(final String text, final int duration) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), text, duration).show();
            }
        });
    }
}
