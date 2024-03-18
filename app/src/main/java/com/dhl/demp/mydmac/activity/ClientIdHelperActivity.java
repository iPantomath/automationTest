package com.dhl.demp.mydmac.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import mydmac.R;

public class ClientIdHelperActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_id_helper);
        Intent intent = getIntent();
        getSharedPreferences("mam",MODE_PRIVATE).edit().putString("id",intent.getStringExtra("client_id")).commit();
    }

    public void close(View view) {
        finishAffinity();
    }
}
