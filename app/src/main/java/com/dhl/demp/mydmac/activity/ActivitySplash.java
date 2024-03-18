package com.dhl.demp.mydmac.activity;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dhl.demp.dmac.ui.MainActivity;
import com.dhl.demp.mydmac.LauncherPreference;
import com.dhl.demp.mydmac.utils.Utils;

/**
 * Created by rd on 23/06/16.
 */

public class ActivitySplash extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utils.startGuardService(this);

        String token = LauncherPreference.getToken(this);
        if (TextUtils.isEmpty(token)) {
            Utils.startLoginActivity(this);
        } else {
            MainActivity.start(this);
        }

        //WORKAROUND, don't delete this
        LauncherPreference.setLauncherSwitchedManually(this, true);

        finish();
    }
}
