package com.dhl.demp.mydmac.activity;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.Nullable;

import com.dhl.demp.dmac.model.RegistrationType;
import com.dhl.demp.dmac.ui.MainActivity;
import com.dhl.demp.mydmac.utils.AzureLogin;
import com.dhl.demp.mydmac.utils.Constants;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.dhl.demp.mydmac.LauncherPreference;
import com.dhl.demp.mydmac.api.ApiFactory;
import com.dhl.demp.mydmac.api.listener.RegistrationCallback;
import com.dhl.demp.mydmac.api.request.GetPinRequest;
import com.dhl.demp.mydmac.api.request.GetTokenRequest;
import com.dhl.demp.mydmac.api.response.GetPinResponse;
import com.dhl.demp.mydmac.api.response.GetTokenResponse;
import com.dhl.demp.mydmac.api.response.RegistrationErrorResponse;
import com.dhl.demp.mydmac.api.response.RegistrationResponse;
import com.dhl.demp.mydmac.api.UnifiedApi;
import com.dhl.demp.mydmac.api.request.RegistrationRequest;
import com.dhl.demp.mydmac.notifications.DeleteFCMtokenService;
import com.dhl.demp.mydmac.obj.DeviceInfo;
import com.dhl.demp.mydmac.utils.RootUtil;
import com.dhl.demp.mydmac.utils.UIUtils;
import com.dhl.demp.mydmac.utils.Utils;

import java.util.concurrent.TimeUnit;

import mydmac.BuildConfig;
import mydmac.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 *
 * Created by rd on 09/05/16.
 */
public class LoginActivity extends AppCompatActivity {
    static final String TAG = LoginActivity.class.getCanonicalName();
    private static final int DIALOG_ID_INFO = 1;
    private static final int DIALOG_ID_ENVIRONMENT = 2;
    public static final String EXTRA_RESEND = "resend";
    private ObjectAnimator imageViewObjectAnimator = null;
    private TextInputEditText loginET;
    private String login = "";
    private Button submit;
    private View rootView;
    private UnifiedApi api;

    //views for selecting environment
    private RadioGroup environmentSelector;
    private TextInputEditText url;
    private TextInputEditText apiKey;
    private TextInputEditText scope;

    private BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkFCMToken();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        rootView = findViewById(R.id.login_rootview);

        //if "resend" - delete previous pin info
        if (getIntent().getBooleanExtra(EXTRA_RESEND, false)) {
            LauncherPreference.deletePinState(this);
        }

        //if we still have valid PIN - don't ask user to enter email again
        String storedState = LauncherPreference.getPinState(this);
        long storedStateExpirationTime = LauncherPreference.getPinExpirationTime(this);
        if (storedState != null && System.currentTimeMillis() < storedStateExpirationTime) {
            String email = LauncherPreference.getClientEmail(this);
            PinActivity.start(LoginActivity.this, email);

            finish();
            return;
        } else {
            LauncherPreference.deletePinState(this);
        }

        Typeface typeFaceNew = Typeface.createFromAsset(getAssets(), Constants.MAIN_FONT_PATH);

        loginET = (TextInputEditText) findViewById(R.id.loginET);
        loginET.setTypeface(typeFaceNew);
        loginET.clearFocus();
        if (getIntent().hasExtra("emailContainer")) loginET.setText(getIntent().getStringExtra("emailContainer"));
        if (getIntent().hasExtra("oldEmail")) loginET.setText(getIntent().getStringExtra("oldEmail"));
        if (!LauncherPreference.getClientEmail(this).equals("")) loginET.setText(LauncherPreference.getClientEmail(this));

        loginET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loginET.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        TextView info = (TextView) findViewById(R.id.info);
        info.setTypeface(typeFaceNew);

        TextInputLayout loginTIL = (TextInputLayout) findViewById(R.id.loginTIL);
        loginTIL.clearFocus();

        submit = (Button) findViewById(R.id.loginBTN);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                login = loginET.getText().toString();
                submit.setClickable(false);
                imageViewObjectAnimator = UIUtils.createViewRotateXAnimation(submit);
                imageViewObjectAnimator.start();

                if (Utils.isLoginValid(login)) {
                    login(login);
                }else {
                    Utils.logE(TAG , "emailContainer: " + login + " is not valid");
                    submit.setClickable(true);
                    shakeError();
                    Snackbar
                            .make(rootView, R.string.wrong_email, Snackbar.LENGTH_LONG)
                            .show();
                }
            }
        });

        setupInfoIcon();
        checkIsDeviceRooted();
    }

    private void checkIsDeviceRooted() {
        if (RootUtil.isDeviceRooted()) {
            findViewById(R.id.rooted_device_message).setVisibility(View.VISIBLE);
        }
    }

    private void setupInfoIcon() {
        findViewById(R.id.infoIcon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_ID_INFO);
            }
        });
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_ID_INFO: return createInfoDialog();
            case DIALOG_ID_ENVIRONMENT: return createEnvironmentDialog();
        }

        return super.onCreateDialog(id);
    }

    private Dialog createInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        builder.setTitle(getString(R.string.device_physical_info));
        builder.setMessage(Utils.getDevicePhysicalInfo(this));
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        if (BuildConfig.DEBUG) {
            builder.setNeutralButton(R.string.environment, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showDialog(DIALOG_ID_ENVIRONMENT);
                }
            });
        }
        return builder.create();
    }

    private Dialog createEnvironmentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        builder.setTitle(getString(R.string.environment));
        builder.setView(R.layout.dialog_environment);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setNeutralButton(android.R.string.cancel, null);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                url = null;
                apiKey = null;
                scope = null;
                environmentSelector = null;
            }
        });
        builder.setCancelable(false);

        //setup view handlers
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                url = (TextInputEditText)alertDialog.findViewById(R.id.url);
                apiKey = (TextInputEditText)alertDialog.findViewById(R.id.api_key);
                scope = (TextInputEditText)alertDialog.findViewById(R.id.scope);
                environmentSelector = (RadioGroup)alertDialog.findViewById(R.id.environment_selector);
                environmentSelector.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        if (url != null) {
                            boolean isCustom = (checkedId == R.id.environment_custom);
                            url.setEnabled(isCustom);
                            apiKey.setEnabled(isCustom);
                            scope.setEnabled(isCustom);
                        }
                    }
                });

                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onEnvironmentSelected()) {
                            alertDialog.dismiss();
                        }
                    }
                });

                //restore saved values
                switch (LauncherPreference.getUrlType(LoginActivity.this)) {
                    case ApiFactory.URL_TYPE_TEST:
                        environmentSelector.check(R.id.environment_test);
                        break;
                    case ApiFactory.URL_TYPE_DEV:
                        environmentSelector.check(R.id.environment_dev);
                        break;
                    case ApiFactory.URL_TYPE_CUSTOM:
                        environmentSelector.check(R.id.environment_custom);
                        break;
                    case ApiFactory.URL_TYPE_UAT:
                        environmentSelector.check(R.id.environment_uat);
                        break;
                    case ApiFactory.URL_TYPE_UAT_PRAGUE:
                        environmentSelector.check(R.id.environment_uat_prague);
                        break;
                    case ApiFactory.URL_TYPE_UAT_CYBERJAYA:
                        environmentSelector.check(R.id.environment_uat_cyberjaya);
                        break;
                    case ApiFactory.URL_TYPE_UAT_ASHBURN:
                        environmentSelector.check(R.id.environment_uat_ashburn);
                        break;
                }
                url.setText(LauncherPreference.getCustomUrl(LoginActivity.this));
                apiKey.setText(LauncherPreference.getCustomApiKey(LoginActivity.this));
                scope.setText(LauncherPreference.getCustomScope(LoginActivity.this));
            }
        });

        return alertDialog;
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

    private boolean onEnvironmentSelected() {
        if (environmentSelector != null) {
            int checkedId = environmentSelector.getCheckedRadioButtonId();
            if (checkedId == R.id.environment_custom) {
                String urlVal = url.getText().toString();
                String apiKeyVal = apiKey.getText().toString();
                String scopeVal = scope.getText().toString();

                //validate
                ((TextInputLayout) environmentSelector.findViewById(R.id.url_wrapper)).setError(null);
                ((TextInputLayout) environmentSelector.findViewById(R.id.api_key_wrapper)).setError(null);
                ((TextInputLayout) environmentSelector.findViewById(R.id.scope_wrapper)).setError(null);
                if (!Patterns.WEB_URL.matcher(urlVal).matches()) {
                    ((TextInputLayout) environmentSelector.findViewById(R.id.url_wrapper)).setError("Invalid URL");
                    return false;
                } else
                if (apiKeyVal.isEmpty()) {
                    ((TextInputLayout) environmentSelector.findViewById(R.id.api_key_wrapper)).setError("Must not be empty");
                    return false;
                }
                if (scopeVal.isEmpty()) {
                    ((TextInputLayout) environmentSelector.findViewById(R.id.scope_wrapper)).setError("Must not be empty");
                    return false;
                }

                LauncherPreference.setUrlType(LoginActivity.this, ApiFactory.URL_TYPE_CUSTOM);
                LauncherPreference.setCustomUrl(LoginActivity.this, urlVal);
                LauncherPreference.setCustomApiKey(LoginActivity.this, apiKeyVal);
                LauncherPreference.setCustomScope(LoginActivity.this, scopeVal);
            } else {
                switch (checkedId) {
                    case R.id.environment_test:
                        LauncherPreference.setUrlType(LoginActivity.this, ApiFactory.URL_TYPE_TEST);
                        break;
                    case R.id.environment_dev:
                        LauncherPreference.setUrlType(LoginActivity.this, ApiFactory.URL_TYPE_DEV);
                        break;
                    case R.id.environment_uat:
                        LauncherPreference.setUrlType(LoginActivity.this, ApiFactory.URL_TYPE_UAT);
                        break;
                    case R.id.environment_uat_prague:
                        LauncherPreference.setUrlType(LoginActivity.this, ApiFactory.URL_TYPE_UAT_PRAGUE);
                        break;
                    case R.id.environment_uat_cyberjaya:
                        LauncherPreference.setUrlType(LoginActivity.this, ApiFactory.URL_TYPE_UAT_CYBERJAYA);
                        break;
                    case R.id.environment_uat_ashburn:
                        LauncherPreference.setUrlType(LoginActivity.this, ApiFactory.URL_TYPE_UAT_ASHBURN);
                        break;
                }
            }

            //need to reset api
            api = null;

            return true;
        }

        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        checkFCMToken();
    }

    private void checkFCMToken() {
        if (LauncherPreference.getFCMToken(getApplicationContext()).length() == 0) {
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

        cancelLoginAnimation();
    }

    private void requestPin(final String mEmail) {
        submit.setClickable(false);
        imageViewObjectAnimator = UIUtils.createViewRotateXAnimation(submit);
        imageViewObjectAnimator.start();

        final int state =  Utils.generateCode();
        DeviceInfo deviceInfo = Utils.buildDeviceInfo(this);

        if (api == null) {
            api = ApiFactory.buildUnifiedApi();
        }
        Call<GetPinResponse> requestPin = api.getPin(new GetPinRequest(mEmail, state, deviceInfo), ApiFactory.getApiKey());
        requestPin.enqueue(new RegistrationCallback<GetPinResponse>(state) {
            @Override
            protected void onSuccess(GetPinResponse pinResponse) {
                submit.setClickable(true);

                if (pinResponse.isPinSent()) {
                    LauncherPreference.setRegistrationType(getApplicationContext(), RegistrationType.EMAIL);
                    LauncherPreference.setPinState(LoginActivity.this, String.valueOf(pinResponse.state), System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(pinResponse.expiresIn));
                    LauncherPreference.setClientEmail(LoginActivity.this, mEmail);

                    PinActivity.start(LoginActivity.this, mEmail);

                    cancelLoginAnimation();
                    finish();
                } else {
                    showError(R.string.request_pin_error);
                }
            }

            @Override
            protected void onError(@Nullable RegistrationErrorResponse error) {
                submit.setClickable(true);
                showRegistrationError(error, mEmail);
            }

            @Override
            public void onFailure(Call<GetPinResponse> call, Throwable t) {
                Utils.logE(TAG , "onFailure request pin" + t.getMessage());
                submit.setClickable(true);
                showError(R.string.unable_to_connect);
            }
        });
    }

    private void login(final String mEmail) {
        UIUtils.hideKeyboard(this, loginET);

        final int state =  Utils.generateCode();
        DeviceInfo deviceInfo = Utils.buildDeviceInfo(this);

        if (api == null) {
            api = ApiFactory.buildUnifiedApi();
        }
        Call<RegistrationResponse> request = api.registration(new RegistrationRequest(mEmail, state, deviceInfo), ApiFactory.getApiKey());
        request.enqueue(new RegistrationCallback<RegistrationResponse>(state) {
            @Override
            protected void onSuccess(RegistrationResponse resp) {
                boolean enableSubmitButton = true;

                if (resp.isAzureLogin()) {
                    enableSubmitButton = false;

                    azureLogin(mEmail, resp.passwordLoginAllowed());
                } else if (resp.isPasswordLogin()) {
                    LauncherPreference.setRegistrationType(getApplicationContext(), RegistrationType.EMAIL);
                    LauncherPreference.setPinState(LoginActivity.this, String.valueOf(resp.state), System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(resp.expiresIn));
                    LauncherPreference.setClientEmail(LoginActivity.this, mEmail);

                    PinActivity.start(LoginActivity.this, mEmail);

                    cancelLoginAnimation();
                    finish();
                } else {
                    showError(R.string.request_pin_error);
                }

                submit.setClickable(enableSubmitButton);
            }

            @Override
            protected void onError(RegistrationErrorResponse error) {
                submit.setClickable(true);
                showRegistrationError(error, mEmail);
            }

            @Override
            public void onFailure(Call<RegistrationResponse> call, Throwable t) {
                Utils.logE(TAG , "onFailure " + t.getMessage());
                submit.setClickable(true);
                showError(R.string.unable_to_connect);
            }
        });
    }

    private void showRegistrationError(RegistrationErrorResponse error, String mEmail) {
        if (error != null) {
            if (error.isPinSentError()) {
                PinActivity.start(LoginActivity.this, mEmail);

                cancelLoginAnimation();
                LauncherPreference.setClientEmail(getApplicationContext(), mEmail);
                finish();
            } else if (error.isNotRegisteredError()) {
                showError(R.string.email_not_registered);
            } else if (error.isRejectedEmailError()) {
                showError(R.string.rejected_email);
            } else if (error.isDeactivatedUserError()) {
                showError(R.string.deactivated_user);
                LauncherPreference.setUserActivated(getApplicationContext(), false);
            } else if (!TextUtils.isEmpty(error.errorDescription)){
                showError(error.errorDescription);
            } else {
                showError(R.string.server_not_responding_error);
            }
        } else {
            showError(R.string.request_pin_error);
        }
    }

    private void azureLogin(final String email, final boolean passwordLoginAllowed) {
        new AzureLogin(this, new AzureLogin.AzureLoginCallback() {
            @Override
            public void onSuccess(String token) {
                loginByAzureToken(email, token);
            }

            @Override
            public void onError() {
                Utils.logI(TAG, "Azure login failed");

                submit.setClickable(true);
                cancelLoginAnimation();

                showAADFailDialog(getString(R.string.aad_login_failed), email, passwordLoginAllowed);
            }
        }).login();
    }

    private void loginByAzureToken(String email, String azureToken) {
        DeviceInfo deviceInfo = Utils.buildDeviceInfo(this);

        Call<GetTokenResponse> getTokenRequest = api.getToken(new GetTokenRequest(GetTokenRequest.GRANT_TYPE_AAD, email, azureToken, deviceInfo), ApiFactory.getApiKey());
        getTokenRequest.enqueue(new Callback<GetTokenResponse>() {
            @Override
            public void onResponse(Call<GetTokenResponse> call, Response<GetTokenResponse> response) {
                if (response.isSuccessful()) {
                    GetTokenResponse getTokenResponse = response.body();
                    Utils.log(TAG, "newToken: " + getTokenResponse.accessToken);
                    Utils.updateTokens(LoginActivity.this, getTokenResponse);
                    LauncherPreference.setRegistrationType(getApplicationContext(), RegistrationType.AZURE);
                    LauncherPreference.setClientEmail(LoginActivity.this, login);

                    //+ mark not azure in all other cases(mabe rename and reuse setAutoreg???or just serach by it usage)

                    startMainActivity();
                } else {
                    showError(R.string.aad_login_failed);
                }

                submit.setClickable(true);
            }

            @Override
            public void onFailure(Call<GetTokenResponse> call, Throwable t) {
                Utils.logE(TAG, "onFailure" + t.getMessage());
                showError(R.string.aad_login_failed);

                submit.setClickable(true);
            }
        });
    }

    private void startMainActivity() {
        MainActivity.start(this);

        finish();
    }

    private void showError(int stringResId) {
        Snackbar.make(rootView, stringResId, Snackbar.LENGTH_LONG).show();
        shakeError();
    }

    private void showError(String error) {
        Snackbar.make(rootView, error, Snackbar.LENGTH_LONG).show();
        shakeError();
    }

    private void shakeError() {
        if (loginET != null && submit != null){
            cancelLoginAnimation();
            loginET.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
            UIUtils.shakeView(submit);
        }
    }

    private void cancelLoginAnimation() {
        if (imageViewObjectAnimator != null && imageViewObjectAnimator.isRunning()) {
            imageViewObjectAnimator.end();
        }
    }
}
