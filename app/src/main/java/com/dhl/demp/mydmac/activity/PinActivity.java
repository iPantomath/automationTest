package com.dhl.demp.mydmac.activity;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.Nullable;

import com.dhl.demp.dmac.ui.MainActivity;
import com.dhl.demp.mydmac.utils.Constants;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dhl.demp.mydmac.LauncherPreference;
import com.dhl.demp.mydmac.api.ApiFactory;
import com.dhl.demp.mydmac.api.UnifiedApi;
import com.dhl.demp.mydmac.api.request.GetTokenRequest;
import com.dhl.demp.mydmac.api.response.GetTokenResponse;
import com.dhl.demp.mydmac.obj.DeviceInfo;
import com.dhl.demp.mydmac.obj.ErrorResponse;
import com.dhl.demp.mydmac.utils.UIUtils;
import com.dhl.demp.mydmac.utils.Utils;

import java.util.Calendar;

import mydmac.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by rd on 23/05/16.
 */
public class PinActivity extends AppCompatActivity {
    private static final String TAG = PinActivity.class.getCanonicalName();
    private static final String MAX_ATTEMPTS_REACHED_ERROR = "max_attempts_reached";
    private static final String INVALID_GRANT_ERROR = "invalid_grant";
    private static final String PIN_EXPIRED_ERROR = "pin_expired";
    private static final int DIALOG_ID_INFO = 1;
    private static final String KEY_MAX_ATTEMPTS_REACHED = "max_attempts_reached";
    private static final String EXTRA_EMAIL = "email";
    private static final String EXTRA_CHANGE_PASSCODE = "change_passcode";
    private static final String EXTRA_PENDING_ACTION = "pending_action";

    private ObjectAnimator imageViewObjectAnimator = null;
    private String pin;
    private TextInputEditText pinET;
    private Button submit, resend;
    private View rootView;

    private UnifiedApi api;
    private TextView info;
    private String mEmail = "";
    private boolean maxAttemptsReached = false;

    public static void start(Context context, String email) {
        Intent intent = new Intent(context, PinActivity.class);
        intent.putExtra(EXTRA_EMAIL, email);
        context.startActivity(intent);
    }

    public static void start(Context context, String email, boolean changePasscode, @Nullable PendingIntent pendingAction) {
        Intent intent = new Intent(context, PinActivity.class);

        intent.putExtra(EXTRA_EMAIL, email);
        intent.putExtra(EXTRA_CHANGE_PASSCODE, changePasscode);
        if (pendingAction != null) {
            intent.putExtra(EXTRA_PENDING_ACTION, pendingAction);
        }

        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);
        rootView = findViewById(R.id.pin_rootview);

        api = ApiFactory.buildUnifiedApi();

        Typeface typeFace = Typeface.createFromAsset(getAssets(), Constants.MAIN_FONT_PATH);
        pinET = (TextInputEditText) findViewById(R.id.pinET);
        pinET.setTypeface(typeFace);
        pinET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                pinET.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        if (savedInstanceState != null) {
            maxAttemptsReached = savedInstanceState.getBoolean(KEY_MAX_ATTEMPTS_REACHED);
        } else {
            maxAttemptsReached = false;
        }

        final Intent intent = getIntent();
        mEmail = intent.getStringExtra(EXTRA_EMAIL);

        info = (TextView) findViewById(R.id.pinInfo);
        info.setText(String.format(getString(R.string.pin_screen_info), mEmail));
        info.setTypeface(typeFace);

        submit = (Button) findViewById(R.id.submit);
        if (maxAttemptsReached) {
            blockSubmitButton();
        } else {
            setupSubmitButton();
        }

        resend = (Button) findViewById(R.id.resend);
        resend.setTypeface(typeFace);
        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoginActivity();
            }
        });

        setupInfoIcon();
    }

    private void setupSubmitButton() {
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!pinET.getText().toString().equals("") && pinET.getText().toString().length() >= 4 && pinET.getText().toString().length() <= 6) {
                    pinET.setClickable(false);
                    imageViewObjectAnimator = UIUtils.createViewRotateXAnimation(submit);
                    imageViewObjectAnimator.start();

                    pin = pinET.getText().toString();
                    checkPin(pin);

                    UIUtils.hideKeyboard(PinActivity.this, pinET);
                } else {
                    shakeError();
                }
            }
        });
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
        if (id == DIALOG_ID_INFO) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
            builder.setTitle(getString(R.string.device_physical_info));
            builder.setMessage(Utils.getDevicePhysicalInfo(this));
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

    private void openLoginActivity() {
        Intent resendIntent = new Intent(PinActivity.this, LoginActivity.class);
        resendIntent.putExtra("emailContainer", mEmail);
        resendIntent.putExtra(LoginActivity.EXTRA_RESEND, true);
        startActivity(resendIntent);
        stopSubmitButtonAnimation();
        finish();
    }

    void checkPin(final String pin) {
        submit.setClickable(false);

        DeviceInfo deviceInfo = Utils.buildDeviceInfo(this);

        Call<GetTokenResponse> getTokensFromPin = api.getToken(new GetTokenRequest(GetTokenRequest.GRANT_TYPE_PASSWORD, mEmail, pin, deviceInfo), ApiFactory.getApiKey());
        getTokensFromPin.enqueue(new Callback<GetTokenResponse>() {
            @Override
            public void onResponse(Call<GetTokenResponse> call, Response<GetTokenResponse> response) {
                if (response.isSuccessful()) {
                    GetTokenResponse getTokenResponse = response.body();
                    Utils.log(TAG, "newToken: " + getTokenResponse.accessToken);
                    Utils.updateTokens(PinActivity.this, getTokenResponse);
                    LauncherPreference.setClientEmail(PinActivity.this, mEmail);
                    LauncherPreference.deletePinState(PinActivity.this);

                    onPinAccepted();

                    stopSubmitButtonAnimation();
                } else {
                    String error;
                    try {
                        ErrorResponse errorResponse = ApiFactory.gson.fromJson(response.errorBody().string(), ErrorResponse.class);
                        //set human understandable text
                        switch (errorResponse.error) {
                            case MAX_ATTEMPTS_REACHED_ERROR:
                                error = getString(R.string.max_attempts_reached_message);

                                //don't allow to submit anymore. User MUST re-send PIN to the emailContainer
                                maxAttemptsReached = true;
                                blockSubmitButton();
                                break;
                            case INVALID_GRANT_ERROR:
                                error = getString(R.string.invalid_grant_message);
                                break;
                            case PIN_EXPIRED_ERROR:
                                error = getString(R.string.pin_expired_message);
                                break;
                            default:
                                error = errorResponse.errorDescription;
                                break;
                        }
                    } catch (Exception e) {
                        error = "";
                    }

                    Utils.logE(TAG, "error: " + error);

                    showError(error);
                }

                submit.setClickable(true);
            }

            @Override
            public void onFailure(Call<GetTokenResponse> call, Throwable t) {
                Utils.logE(TAG, "onFailure" + t.getMessage());
                shakeError();

                submit.setClickable(true);
            }
        });
    }

    private void blockSubmitButton() {
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showError(getString(R.string.max_attempts_reached_message));
            }
        });
    }

    private void showError(String error) {
        stopSubmitButtonAnimation();
        shakeError();
        Snackbar.make(rootView, error, Snackbar.LENGTH_LONG).show();
    }

    private void stopSubmitButtonAnimation() {
        if (imageViewObjectAnimator != null && imageViewObjectAnimator.isRunning()) {
            Utils.logI(TAG, "animEnd: onDestroy() ");
            imageViewObjectAnimator.end();
        }
    }


    void shakeError() {
        stopSubmitButtonAnimation();

        if (pinET != null && submit != null) {

            pinET.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
            UIUtils.shakeView(submit);
        }
    }

    private void onPinAccepted() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, LauncherPreference.refreshTokenAfterDefinedDays);
        LauncherPreference.setTokenExpiry(getApplicationContext(), cal.getTimeInMillis());

        Intent callIntent = getIntent();
        boolean isChangePasscode = LauncherPreference.isChangePasscode(PinActivity.this);
        if (getIntent().hasExtra(EXTRA_PENDING_ACTION)
                || callIntent.hasExtra(EXTRA_CHANGE_PASSCODE)
                || isChangePasscode) {

            PendingIntent pendingAction = getIntent().getParcelableExtra(EXTRA_PENDING_ACTION);
            startChoosePasscodeActivity(pendingAction);
        } else {
            startMainActivity();
        }
    }

    private void startChoosePasscodeActivity(PendingIntent pendingAction) {
        ChoosePasscodeActivity.start(PinActivity.this, pendingAction);
        stopSubmitButtonAnimation();
        finish();
    }

    private void startMainActivity() {
        MainActivity.start(this);

        stopSubmitButtonAnimation();
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(KEY_MAX_ATTEMPTS_REACHED, maxAttemptsReached);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopSubmitButtonAnimation();
    }
}
