package com.dhl.demp.mydmac.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dhl.demp.dmac.data.network.TipInterceptor;
import com.dhl.demp.mydmac.LauncherPreference;
import com.dhl.demp.mydmac.api.ApiFactory;
import com.dhl.demp.mydmac.api.UnifiedApi;
import com.dhl.demp.mydmac.api.request.GetTokenRequest;
import com.dhl.demp.mydmac.api.request.RefreshTokenRequest;
import com.dhl.demp.mydmac.api.response.GetTokenResponse;
import com.dhl.demp.mydmac.api.response.RefreshTokenResponse;
import com.dhl.demp.mydmac.obj.DeviceInfo;
import com.dhl.demp.mydmac.obj.DeviceInfoExpress;
import com.dhl.demp.mydmac.utils.Constants;
import com.dhl.demp.mydmac.utils.RefreshAccessTokenCallback;
import com.dhl.demp.mydmac.utils.Utils;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import mydmac.R;
import retrofit2.Call;

/**
 * Created by robielok on 3/22/2018.
 */
@AndroidEntryPoint
public class TestingActivity extends AppCompatActivity {

    private DeviceInfo deviceInfoModel;
    private EditText osVersion;
    private EditText kernelVersion;
    private EditText patchVersion;
    private EditText uniqueId;
    private String deviceUniqueId;
    private String editUniqueId;

    private SwitchCompat networkSwitch;
    private SwitchCompat accessTokenSwitch;
    private SwitchCompat hashTokenSwitch;
    @Inject
    TipInterceptor tipInterceptor;


    public static void start(Context context) {
        Intent intent = new Intent(context, TestingActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setBackgroundColor(getColor(R.color.build_type_color));
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        setupViews();
    }

    private void setupViews() {
        deviceInfoModel = Utils.buildDeviceInfo(this);
        //deviceUniqueId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID) + getString(R.string.uniqueIdSuffix) + "_debug";

        ((TextView) findViewById(R.id.device_id)).setText("• Device unique ID: " + deviceInfoModel.deviceUniqueId);
        ((TextView) findViewById(R.id.manufacturer)).setText("• Manufacturer: " + deviceInfoModel.manufacture);
        ((TextView) findViewById(R.id.device_model)).setText("• Device model: " + deviceInfoModel.devModel);
        ((TextView) findViewById(R.id.environment)).setText("• URL : " + getUrl());

        TextView.OnEditorActionListener onEditorActionListener = new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendDeviceInfo();
                    return true;
                }

                return false;
            }
        };

        osVersion = (EditText) findViewById(R.id.os_version);
        osVersion.setText(deviceInfoModel.osVersion);
        osVersion.setOnEditorActionListener(onEditorActionListener);
        kernelVersion = (EditText) findViewById(R.id.kernel_version);
        kernelVersion.setText(deviceInfoModel.kernel);
        kernelVersion.setOnEditorActionListener(onEditorActionListener);
        patchVersion = (EditText) findViewById(R.id.patch_version);
        patchVersion.setText(deviceInfoModel.patchLevel);
        patchVersion.setOnEditorActionListener(onEditorActionListener);
        uniqueId = (EditText) findViewById(R.id.unique_id);
        uniqueId.setText(deviceInfoModel.deviceUniqueId);
        uniqueId.setOnEditorActionListener(onEditorActionListener);
        networkSwitch = (SwitchCompat) findViewById(R.id.network_switch);
        accessTokenSwitch = (SwitchCompat) findViewById(R.id.access_token_switch);
        hashTokenSwitch = (SwitchCompat) findViewById(R.id.hash_token_switch);


        findViewById(R.id.send_device_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDeviceInfo();
            }
        });

        findViewById(R.id.clear_upgrade_postpone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LauncherPreference.setUpgradePostponedTo(TestingActivity.this, 0);
            }
        });
        findViewById(R.id.triggerRegistration).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editUniqueId = uniqueId.getText().toString();
                if(!editUniqueId.equals(deviceUniqueId)) {
                    LauncherPreference.setIsNewRegistration(TestingActivity.this, true);
                    LauncherPreference.setUniqueId(TestingActivity.this, editUniqueId);
                }
                AutoRegActivity autoRegActivity = new AutoRegActivity();
                UnifiedApi api = ApiFactory.buildUnifiedApi();
                String imei = LauncherPreference.getImeiManual(TestingActivity.this);
                String sn = LauncherPreference.getSnManual(TestingActivity.this);
                DeviceInfoExpress deviceInfoExpress = new DeviceInfoExpress(TestingActivity.this, sn, imei);

                //hashed-IMEI+S/N
                String hashed = Utils.getChecksum(deviceInfoExpress.IMEI + deviceInfoExpress.serialNum);
                Call<GetTokenResponse> autoReg = api.autoReg(new GetTokenRequest(Constants.AUTO_REG_SCOPE, deviceInfoExpress.IMEI, hashed, deviceInfoExpress), ApiFactory.getApiKey());

                autoRegActivity.triggerRegistration(autoReg, imei, sn);
                Toast.makeText(getApplicationContext(), "Registration Triggered!", Toast.LENGTH_SHORT).show();
//                TestActivityInterface testActivityInterface = (TestActivityInterface) TestingActivity.this;
//                testActivityInterface.triggerRegistration();
            }
        });
        findViewById(R.id.resetUniqueId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LauncherPreference.setIsNewRegistration(TestingActivity.this, false);
                //uniqueId.getText().clear();
                uniqueId.setText(deviceUniqueId);
                LauncherPreference.setUniqueId(TestingActivity.this, editUniqueId);
                Toast.makeText(getApplicationContext(), "Unique ID reset!", Toast.LENGTH_SHORT).show();
            }
        });

        tipInterceptor.getConnected();
        networkSwitch.setChecked(tipInterceptor.getConnected());
        networkSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                if(isChecked){
                    networkSwitch.setChecked(true);
                    tipInterceptor.setConnected(true);
                }
                else{
                    networkSwitch.setChecked(false);
                    tipInterceptor.setConnected(false);
                }
            }
        });

        accessTokenSwitch.setChecked(LauncherPreference.getTokenExpired(getApplicationContext()));
        accessTokenSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){
                    accessTokenSwitch.setChecked(true);
                    LauncherPreference.setTokenExpired(getApplicationContext(), true);

                }else{
                    accessTokenSwitch.setChecked(false);
                    LauncherPreference.setTokenExpired(getApplicationContext(),false);

                }

            }
        });

        hashTokenSwitch.setChecked(LauncherPreference.getHashToken(getApplicationContext()));
        hashTokenSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){
                    hashTokenSwitch.setChecked(true);
                    LauncherPreference.setHashToken(getApplicationContext(), true);

                }else{
                    hashTokenSwitch.setChecked(false);
                    LauncherPreference.setHashToken(getApplicationContext(), false);

                }
            }
        });
    }

    private String getUrl() {
        int type = LauncherPreference.getUrlType(this);
        switch (type) {
            case ApiFactory.URL_TYPE_TEST:
                return ApiFactory.BASE_URL_TEST;
            case ApiFactory.URL_TYPE_DEV:
                return ApiFactory.BASE_URL_DEV;
            case ApiFactory.URL_TYPE_CUSTOM:
                return LauncherPreference.getCustomUrl(this);
            case ApiFactory.URL_TYPE_UAT:
                return ApiFactory.BASE_URL_UAT;
            case ApiFactory.URL_TYPE_UAT_PRAGUE:
                return ApiFactory.BASE_URL_UAT_PRAGUE;
            case ApiFactory.URL_TYPE_UAT_CYBERJAYA:
                return ApiFactory.BASE_URL_UAT_CYBERJAYA;
            case ApiFactory.URL_TYPE_UAT_ASHBURN:
                return ApiFactory.BASE_URL_UAT_ASHBURN;
            default:
                return "Unknown type " + type;
        }
    }

    private void sendDeviceInfo() {
        UnifiedApi api = ApiFactory.buildUnifiedApi();

        deviceInfoModel.osVersion = osVersion.getText().toString();
        deviceInfoModel.kernel = kernelVersion.getText().toString();
        deviceInfoModel.patchLevel = patchVersion.getText().toString();
        deviceInfoModel.deviceUniqueId = uniqueId.getText().toString();

        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest(
                LauncherPreference.getRefToken(this),
                ApiFactory.getScope(),
                deviceInfoModel);
        Call<RefreshTokenResponse> refreshToken = api.refreshToken(refreshTokenRequest, ApiFactory.getApiKey());
        refreshToken.enqueue(new RefreshAccessTokenCallback(this) {
            @Override
            protected void onRequestDone() {
                showToast("Device info sent");
            }

            @Override
            protected void onError(String error, boolean logoutUser) {
                showToast("Fail to send device info");
            }

            @Override
            protected void onFailure(String message) {
                showToast("Fail to send device info");
            }

            @Override
            protected void onTokenRefreshed() {
                //nothing to do
            }
        });
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
