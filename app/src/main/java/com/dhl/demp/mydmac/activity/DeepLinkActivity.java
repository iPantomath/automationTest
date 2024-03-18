package com.dhl.demp.mydmac.activity;

import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.dhl.demp.mydmac.sso.SSOService;
import com.dhl.demp.mydmac.utils.Utils;

public class DeepLinkActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Uri data = intent.getData();

        Utils.log("DeepLinkActivity", "DeepLinkActivity is started[" + data + "]");

        SSOService.start(this, data);

        finish();
    }
}