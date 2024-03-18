package com.dhl.demp.mydmac;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import com.dhl.demp.mydmac.sso.SSOService;
import com.dhl.demp.mydmac.utils.Utils;

public class DMACAuthReceiver extends BroadcastReceiver {
    private static String TAG = DMACAuthReceiver.class.getCanonicalName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Uri data = intent.getData();
        Utils.log(TAG, "DMACAuthReceiver called with params[" + data+ "]");

        SSOService.start(context, data);
    }
}
