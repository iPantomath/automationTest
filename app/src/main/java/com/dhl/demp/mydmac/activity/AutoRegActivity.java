package com.dhl.demp.mydmac.activity;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.Nullable;

import com.dhl.demp.dmac.model.RegistrationType;
import com.dhl.demp.dmac.ui.MainActivity;
import com.dhl.demp.mydmac.utils.AzureLogin;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dhl.demp.mydmac.LauncherPreference;
import com.dhl.demp.mydmac.api.ApiFactory;
import com.dhl.demp.mydmac.api.UnifiedApi;
import com.dhl.demp.mydmac.api.listener.RegistrationCallback;
import com.dhl.demp.mydmac.api.request.GetPinRequest;
import com.dhl.demp.mydmac.api.request.GetTokenRequest;
import com.dhl.demp.mydmac.api.request.RegistrationRequest;
import com.dhl.demp.mydmac.api.response.GetPinResponse;
import com.dhl.demp.mydmac.api.response.GetTokenResponse;
import com.dhl.demp.mydmac.api.response.RegistrationErrorResponse;
import com.dhl.demp.mydmac.api.response.RegistrationResponse;
import com.dhl.demp.mydmac.notifications.DeleteFCMtokenService;
import com.dhl.demp.mydmac.obj.DeviceInfo;
import com.dhl.demp.mydmac.obj.DeviceInfoExpress;
import com.dhl.demp.mydmac.obj.ErrorResponse;
import com.dhl.demp.mydmac.ui.SimpleTextWatcher;
import com.dhl.demp.mydmac.utils.Constants;
import com.dhl.demp.mydmac.utils.UIUtils;
import com.dhl.demp.mydmac.utils.Utils;

import java.util.concurrent.TimeUnit;

import mydmac.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by rd on 09/05/16.
 */
public class AutoRegActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PERMISSION_READ_PHONE_STATE = 1;
    private static final int DIALOG_ID_INFO = 1;

    static final String TAG = AutoRegActivity.class.getCanonicalName();
    private ObjectAnimator imageViewObjectAnimator = null;
    private TextInputEditText loginET;

    private View emailContainer, autoContainer;
    private Button submit, submitAuto;
    private View selectedButton;
    private TextView autoRegError;

    private TextView infoTitle;

    private View rootView;
    private UnifiedApi api;

    private BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkFCMToken();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autoreg);

        rootView = findViewById(R.id.login_rootview);
        api = ApiFactory.buildUnifiedApi();

        Typeface typeFaceNew = Typeface.createFromAsset(getAssets(), Constants.MAIN_FONT_PATH);

        infoTitle = (TextView) findViewById(R.id.info_title);
        infoTitle.setTypeface(typeFaceNew);

        autoContainer = findViewById(R.id.auto_container);
        autoRegError = (TextView) findViewById(R.id.auto_reg_error);
        if (autoRegError != null) {
            autoRegError.setTypeface(typeFaceNew);
        }

        loginET = (TextInputEditText) findViewById(R.id.loginET);
        loginET.setTypeface(typeFaceNew);
        loginET.clearFocus();
        if (getIntent().hasExtra("email")) {
            loginET.setText(getIntent().getStringExtra("email"));
        }
        if (getIntent().hasExtra("oldEmail")) {
            loginET.setText(getIntent().getStringExtra("oldEmail"));
        }
        if (!LauncherPreference.getClientEmail(this).equals("")) {
            loginET.setText(LauncherPreference.getClientEmail(this));
        }

        loginET.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loginET.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            }
        });

        emailContainer = findViewById(R.id.email_container);
        submitAuto = (Button) findViewById(R.id.autologinBTN);
        submit = (Button) findViewById(R.id.loginBTN);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedButton.setSelected(false);
                selectedButton = v;
                selectedButton.setSelected(true);

                onSubmitEmail();
            }
        });
        submitAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedButton.setSelected(false);
                selectedButton = v;
                selectedButton.setSelected(true);

                if (Utils.beforeAndroid10()) {
                    autoRegistrationWithPermission();
                } else {
                    onSubmitAuto();
                }
            }
        });

        findViewById(R.id.infoIcon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(DIALOG_ID_INFO);
            }
        });

        selectedButton = submitAuto;
        selectedButton.setSelected(true);

        if (Utils.isAndroid10()) {
            autoContainer.setVisibility(View.VISIBLE);
        } else {
            if (!getIntent().getBooleanExtra("unregister", false)) {
                autoRegistrationWithPermission();
            }
        }
    }

    private void onSubmitEmail() {
        autoContainer.setVisibility(View.GONE);
        if (emailContainer.getVisibility() == View.GONE) {
            emailContainer.setVisibility(View.VISIBLE);
            infoTitle.setText(R.string.login_screen_info);
            return;
        }

        String login = loginET.getText().toString();

        if (Utils.isLoginValid(login)) {
            submit.setClickable(false);
            animateButton(submit);

            login(login);
        } else {
            shakeError(submit);
            Snackbar.make(rootView, R.string.wrong_email, Snackbar.LENGTH_LONG).show();
        }
    }

    private void onSubmitAuto() {
        emailContainer.setVisibility(View.GONE);
        if (autoContainer.getVisibility() == View.GONE) {
            autoContainer.setVisibility(View.VISIBLE);
            infoTitle.setText(R.string.auto_login_screen_info);
            return;
        }

        boolean validInput = true;

        EditText imeiET = (EditText) findViewById(R.id.imeiET);
        String imei = imeiET.getText().toString();
        if (imei.isEmpty()) {
            validInput = false;

            ((TextInputLayout) findViewById(R.id.imei_container)).setError(getString(R.string.empty_field_error));
        }

        EditText snET = (EditText) findViewById(R.id.snET);
        String sn = snET.getText().toString();
        if (sn.isEmpty()) {
            validInput = false;

            ((TextInputLayout) findViewById(R.id.sn_container)).setError(getString(R.string.empty_field_error));
        }

        if (validInput) {
            DeviceInfoExpress deviceInfoExpress = new DeviceInfoExpress(this, sn, imei);

            //hashed-IMEI+S/N
            String hashed = Utils.getChecksum(deviceInfoExpress.IMEI + deviceInfoExpress.serialNum);
            Call<GetTokenResponse> autoReg = api.autoReg(new GetTokenRequest(Constants.AUTO_REG_SCOPE, deviceInfoExpress.IMEI, hashed, deviceInfoExpress), ApiFactory.getApiKey());

            performAutoReg(autoReg, imei, sn);
        }
    }

    private void autoRegistrationWithPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(AutoRegActivity.this, Manifest.permission.READ_PHONE_STATE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            autoRegistration();
        } else {
            ActivityCompat.requestPermissions(AutoRegActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE_PERMISSION_READ_PHONE_STATE);
        }
    }

    private void autoRegistration() {
        emailContainer.setVisibility(View.GONE);
        infoTitle.setText(R.string.login_auto_screen_info);
        autoContainer.setVisibility(View.VISIBLE);
        if (autoRegError != null) {
            autoRegError.setVisibility(View.INVISIBLE);
        }

        DeviceInfoExpress deviceInfoExpress = (DeviceInfoExpress) Utils.buildDeviceInfo(this);

        //hashed-IMEI+S/N
        String hashed = Utils.getChecksum(deviceInfoExpress.IMEI + deviceInfoExpress.serialNum);
        Call<GetTokenResponse> autoReg = api.autoReg(new GetTokenRequest(Constants.AUTO_REG_SCOPE, deviceInfoExpress.IMEI, hashed, deviceInfoExpress), ApiFactory.getApiKey());

        performAutoReg(autoReg, deviceInfoExpress.IMEI, deviceInfoExpress.serialNum);
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        checkFCMToken();
    }

    private void checkFCMToken() {
        if (LauncherPreference.getFCMToken(this).length() == 0) {
            DeleteFCMtokenService.start(this);
        }
    }

    @Override
    public void onPause() {
        unregisterReceiver(networkStateReceiver);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        cancelAnimation();
    }

    private void requestPin(final String email) {
        submit.setClickable(false);
        animateButton(submit);

        final int state = Utils.generateCode();
        DeviceInfo deviceInfo = Utils.buildDeviceInfo(this);

        Call<GetPinResponse> requestPin = api.getPin(new GetPinRequest(email, state, deviceInfo), ApiFactory.getApiKey());
        requestPin.enqueue(new RegistrationCallback<GetPinResponse>(state) {
            @Override
            protected void onSuccess(GetPinResponse pinResponse) {
                submit.setClickable(true);

                if (pinResponse.isPinSent()) {
                    LauncherPreference.setRegistrationType(getApplicationContext(), RegistrationType.EMAIL);
                    LauncherPreference.setPinState(AutoRegActivity.this, String.valueOf(pinResponse.state), System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(pinResponse.expiresIn));
                    LauncherPreference.setClientEmail(AutoRegActivity.this, email);

                    PinActivity.start(AutoRegActivity.this, email);

                    cancelAnimation();
                    finish();
                } else {
                    showError(R.string.request_pin_error, submit);
                }
            }

            @Override
            protected void onError(@Nullable RegistrationErrorResponse error) {
                submit.setClickable(true);
                showRegistrationError(error, email);
            }

            @Override
            public void onFailure(Call<GetPinResponse> call, Throwable t) {
                Utils.logE(TAG, "onFailure request pin" + t.getMessage());
                submit.setClickable(true);
                showError(R.string.unable_to_connect, submit);
            }
        });
    }

    void login(final String email) {
        UIUtils.hideKeyboard(this, loginET);

        final int state = Utils.generateCode();
        DeviceInfo deviceInfo = Utils.buildDeviceInfo(this);

        Call<RegistrationResponse> request = api.registration(new RegistrationRequest(email, state, deviceInfo), ApiFactory.getApiKey());
        request.enqueue(new RegistrationCallback<RegistrationResponse>(state) {
            @Override
            protected void onSuccess(RegistrationResponse resp) {
                boolean enableSubmitButton = true;

                if (resp.isAzureLogin()) {
                    enableSubmitButton = false;

                    azureLogin(email, resp.passwordLoginAllowed());
                } else if (resp.isPasswordLogin()) {
                    LauncherPreference.setRegistrationType(getApplicationContext(), RegistrationType.EMAIL);
                    LauncherPreference.setPinState(AutoRegActivity.this, String.valueOf(resp.state), System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(resp.expiresIn));
                    LauncherPreference.setClientEmail(getApplicationContext(), email);

                    PinActivity.start(AutoRegActivity.this, email);

                    cancelAnimation();
                    finish();
                } else {
                    showError(R.string.request_pin_error, submit);
                }

                submit.setClickable(enableSubmitButton);
            }

            @Override
            protected void onError(@Nullable RegistrationErrorResponse error) {
                submit.setClickable(true);
                showRegistrationError(error, email);
            }

            @Override
            public void onFailure(Call<RegistrationResponse> call, Throwable t) {
                Utils.logE(TAG, "onFailure " + t.getMessage());
                submit.setClickable(true);
                showError(R.string.unable_to_connect, submit);
            }
        });
    }

    private void showRegistrationError(RegistrationErrorResponse error, String email) {
        if (error != null) {
            if (error.isPinSentError()) {
                PinActivity.start(AutoRegActivity.this, email);

                cancelAnimation();
                LauncherPreference.setClientEmail(getApplicationContext(), email);
                finish();
            } else if (error.isNotRegisteredError()) {
                showError(R.string.email_not_registered, submit);
            } else if (error.isRejectedEmailError()) {
                showError(R.string.rejected_email, submit);
            } else if (error.isDeactivatedUserError()) {
                showError(R.string.deactivated_user, submit);
                LauncherPreference.setUserActivated(getApplicationContext(), false);
            } else {
                showError(error.errorDescription, submit);
            }
        } else {
            showError(R.string.request_pin_error, submit);
        }
    }


    private void azureLogin(final String email, final boolean passwordLoginAllowed) {
        Utils.logI(TAG, "azureLogin " + email);

        new AzureLogin(this, new AzureLogin.AzureLoginCallback() {
            @Override
            public void onSuccess(String token) {
                loginByAzureToken(email, token);
            }

            @Override
            public void onError() {
                Utils.logI(TAG, "Azure login failed");

                submit.setClickable(true);
                cancelAnimation();

                showAADFailDialog(getString(R.string.aad_login_failed), email, passwordLoginAllowed);
            }
        }).login();
    }

    private void loginByAzureToken(final String email, String azureToken) {
        DeviceInfo deviceInfo = Utils.buildDeviceInfo(this);

        Call<GetTokenResponse> getTokenRequest = api.getToken(new GetTokenRequest(GetTokenRequest.GRANT_TYPE_AAD, email, azureToken, deviceInfo), ApiFactory.getApiKey());
        getTokenRequest.enqueue(new Callback<GetTokenResponse>() {
            @Override
            public void onResponse(Call<GetTokenResponse> call, Response<GetTokenResponse> response) {
                if (response.isSuccessful()) {
                    GetTokenResponse getTokenResponse = response.body();
                    Utils.log(TAG, "newToken: " + getTokenResponse.accessToken);
                    Utils.updateTokens(AutoRegActivity.this, getTokenResponse);
                    LauncherPreference.setRegistrationType(getApplicationContext(), RegistrationType.AZURE);
                    LauncherPreference.setClientEmail(AutoRegActivity.this, email);

                    startMainActivity();
                } else {
                    showError(R.string.aad_login_failed, submit);
                }

                submit.setClickable(true);
            }

            @Override
            public void onFailure(Call<GetTokenResponse> call, Throwable t) {
                Utils.logE(TAG, "onFailure" + t.getMessage());
                showError(R.string.aad_login_failed, submit);

                submit.setClickable(true);
            }
        });
    }

    private void startMainActivity() {
        MainActivity.start(this);

        finish();
    }

    private void showError(int stringResId, Button button) {
        Snackbar.make(rootView, stringResId, Snackbar.LENGTH_LONG).show();
        shakeError(button);
    }

    private void showError(String error, Button button) {
        Snackbar.make(rootView, error, Snackbar.LENGTH_LONG).show();
        shakeError(button);
    }

    void shakeError(Button b) {
        if (b != null) {
            cancelAnimation();

            UIUtils.shakeView(b);
        }
    }

    private void cancelAnimation() {
        if (imageViewObjectAnimator != null && imageViewObjectAnimator.isRunning()) {
            imageViewObjectAnimator.end();
        }
    }

    private void performAutoReg(Call<GetTokenResponse> request, final String imei, final String sn) {
        submitAuto.setClickable(false);
        animateButton(submitAuto);

        request.enqueue(new Callback<GetTokenResponse>() {
            @Override
            public void onResponse(Call<GetTokenResponse> call, Response<GetTokenResponse> response) {
                submitAuto.setClickable(true);

                if (response.isSuccessful()) {
                    GetTokenResponse getTokenResponse = response.body();

                    LauncherPreference.setRegistrationType(getApplicationContext(), RegistrationType.AUTO);
                    if (Utils.isAndroid10()) {
                        LauncherPreference.setImeiManual(AutoRegActivity.this, imei);
                        LauncherPreference.setSnManual(AutoRegActivity.this, sn);
                    }
                    Utils.updateTokens(getApplicationContext(), getTokenResponse);

                    startMainActivity();
                } else {
                    String error;
                    try {
                        ErrorResponse errorResponse = ApiFactory.gson.fromJson(response.errorBody().string(), ErrorResponse.class);
                        error = errorResponse.errorDescription;
                    } catch (Exception e) {
                        error = e.getMessage();
                    }

                    Utils.logE(TAG, "error: " + error);
                    if (TextUtils.isEmpty(error)) {
                        error = getString(R.string.failed_login);
                    }

                    if (autoRegError != null) {
                        autoRegError.setVisibility(View.VISIBLE);
                    }
                    showError(error, submitAuto);
                }
            }

            @Override
            public void onFailure(Call<GetTokenResponse> call, Throwable t) {
                Utils.logE(TAG, "onFailure " + t.getMessage());
                Snackbar
                        .make(rootView, R.string.unable_to_connect, Snackbar.LENGTH_LONG)
                        .show();

                submitAuto.setClickable(true);
                if (autoRegError != null) {
                    autoRegError.setVisibility(View.VISIBLE);
                }
                shakeError(submitAuto);
            }
        });
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_ID_INFO) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
            builder.setTitle(getString(R.string.device_physical_info));
            builder.setMessage(generateDeviceInfo());
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return builder.create();
        }

        return super.onCreateDialog(id);
    }

    private void showAADFailDialog(String errorMsg, final String email, boolean passwordLoginAllowed) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        builder.setTitle(getString(R.string.auth_failed));
        builder.setMessage(errorMsg);
        builder.setPositiveButton(android.R.string.ok, null);
        if (passwordLoginAllowed) {
            builder.setNegativeButton(R.string.send_me_pin, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    requestPin(email);
                }
            });
        }

        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_READ_PHONE_STATE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    autoRegistration();
                } else {
                    shakeError(submitAuto);
                    Snackbar.make(rootView, R.string.read_phone_state_permission_required, Snackbar.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private String generateDeviceInfo() {
        String deviceInfo = Utils.getDevicePhysicalInfo(this);

        if (Utils.beforeAndroid10()) {
            String sn = Utils.getSerialNumber();
            String imei = Utils.getIMEI(this);

            deviceInfo += "\nS\\N: " + sn + "\nIMEI: " + imei;
        }

        return deviceInfo;
    }

    private void animateButton(Button button) {
        imageViewObjectAnimator = UIUtils.createViewRotateXAnimation(button);
        imageViewObjectAnimator.start();
    }

    public void triggerRegistration(Call<GetTokenResponse> request, final String imei, final String sn) {
        //AutoRegActivityInterface autoRegActivityInterface = (TestingActivity) getApplicationContext();
        request.enqueue(new Callback<GetTokenResponse>() {
            @Override
            public void onResponse(Call<GetTokenResponse> call, Response<GetTokenResponse> response) {


                if (response.isSuccessful()) {
                    GetTokenResponse getTokenResponse = response.body();

                    //Utils.updateTokens(getApplicationContext(), getTokenResponse);
                    ////autoRegActivityInterface.showToastMessage("Registration successful!");
                    //Toast.makeText(getApplicationContext(), "Registration successful!", Toast.LENGTH_SHORT).show();

                } else {
                    String error;
                    try {
                        ErrorResponse errorResponse = ApiFactory.gson.fromJson(response.errorBody().string(), ErrorResponse.class);
                        error = errorResponse.errorDescription;
                    } catch (Exception e) {
                        error = e.getMessage();
                    }

                    Utils.logE(TAG, "error: " + error);
                    if (TextUtils.isEmpty(error)) {
                        error = getString(R.string.failed_login);
                    }

                    //autoRegActivityInterface.showToastMessage( error);
                    //Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GetTokenResponse> call, Throwable t) {
                Utils.logE(TAG, "onFailure " + t.getMessage());
//                Snackbar
//                        .make(rootView, R.string.unable_to_connect, Snackbar.LENGTH_LONG)
//                        .show();

////                submitAuto.setClickable(true);
//                if (autoRegError != null) {
//                    autoRegError.setVisibility(View.VISIBLE);
//                }
//                shakeError(submitAuto);
            }
        });
    }
}
