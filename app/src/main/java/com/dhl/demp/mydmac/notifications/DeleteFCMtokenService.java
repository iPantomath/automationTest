package com.dhl.demp.mydmac.notifications;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.dhl.demp.mydmac.utils.Utils;
import com.google.firebase.iid.FirebaseInstanceId;
import java.io.IOException;

/**
 * Created by petlebed on 18/04/17.
 */

public class DeleteFCMtokenService extends IntentService {
    private static final String TAG = DeleteFCMtokenService.class.getCanonicalName();

    public static void start(Context context) {
        Intent intent = new Intent(context, DeleteFCMtokenService.class);
        context.startService(intent);
    }

    public DeleteFCMtokenService() {
        super("DeleteFCMtokenService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            // Resets Instance ID and revokes all tokens.
            FirebaseInstanceId.getInstance().deleteInstanceId();
            Utils.logI("DeleteFCMTokenService", "token deleted");
        } catch (IOException e) {
            Utils.logE("DeleteFCMTokenService", "error");
            e.printStackTrace();
        }
        FirebaseInstanceId.getInstance().getToken();
    }
}
