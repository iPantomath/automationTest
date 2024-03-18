package com.example.authtestdmac;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final String TAG = "sso_test";

    final static String uriActivateTemplate = "://activate?appid=" + BuildConfig.APPLICATION_ID + "&callBackURL=" + "authtestdmac://activate";
    final static String uriLoginTemplate = "://login?appid=" + BuildConfig.APPLICATION_ID + "&callBackURL=" + "authtestdmac://login";
    final static String uriInfoTemplate = "://info?appid=" + BuildConfig.APPLICATION_ID + "&callBackURL=" + "authtestdmac://info";
    final static String uriTokenTemplate = "://token?appid=" + BuildConfig.APPLICATION_ID + "&callBackURL=" + "authtestdmac://token";

    private RadioGroup rg;
    private TextView tv;
    private EditText state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rg = findViewById(R.id.rg);
        state = findViewById(R.id.state);

        tv = findViewById(R.id.tv);
        if (getIntent().getData() != null) {
            tv.setText(getIntent().getData().toString());
        }

        Button activateButton = findViewById(R.id.activate);
        activateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv.setText("");
                sendAction(uriActivateTemplate);
            }
        });
        Button loginButton = findViewById(R.id.login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv.setText("");
                sendAction(uriLoginTemplate);
            }
        });
        Button infoButton = findViewById(R.id.info);
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv.setText("");
                sendAction(uriInfoTemplate);
            }
        });
        Button tokenButton = findViewById(R.id.token);
        tokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv.setText("");
                sendAction(uriTokenTemplate);
            }
        });
    }

    private void sendAction(String template) {
        Log.d(TAG, "sendAction() called with: template = [" + template + "]");

        Intent intent = new Intent();
        intent.setData(Uri.parse(getSchema() + template));

        //add state
        if (!TextUtils.isEmpty(state.getText())) {
            intent.setData(Uri.parse(intent.getData().toString() + "&state=" + state.getText().toString()));
        }

        try {
            startActivity(intent);
        } catch (Exception ex) {
            Log.d(TAG, "failed to sendAction ", ex);
        }
    }

    private boolean isProd() {
        return rg.getCheckedRadioButtonId() == R.id.mode_prod;
    }

    private String getSchema() {
        return isProd() ? "dmacauth" : "dmactestauth";
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null && intent.getData() != null) {
            tv.setText(intent.getData().toString());
        }
    }
}
