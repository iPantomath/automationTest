package com.dhl.demp.mydmac.activity;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dhl.demp.dmac.model.RegistrationType;
import com.dhl.demp.dmac.ui.MainActivity;
import com.dhl.demp.mydmac.utils.AzureLogin;
import com.dhl.demp.mydmac.utils.Constants;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dhl.demp.mydmac.LauncherPreference;
import com.dhl.demp.mydmac.api.ApiFactory;
import com.dhl.demp.mydmac.api.UnifiedApi;
import com.dhl.demp.mydmac.api.listener.RegistrationCallback;
import com.dhl.demp.mydmac.api.request.GetPinRequest;
import com.dhl.demp.mydmac.api.response.GetPinResponse;
import com.dhl.demp.mydmac.api.response.RegistrationErrorResponse;
import com.dhl.demp.mydmac.obj.DeviceInfo;
import com.dhl.demp.mydmac.obj.BiometricSensorState;
import com.dhl.demp.mydmac.obj.PasscodeModel;
import com.dhl.demp.mydmac.utils.UIUtils;
import com.dhl.demp.mydmac.utils.Utils;
import com.dhl.demp.mydmac.utils.fingerprint.CryptoUtils;
import com.dhl.demp.mydmac.utils.fingerprint.FingerprintUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import mydmac.R;
import retrofit2.Call;

/**
 * Created by petlebed on 10/08/17.
 */
public class EnterPasscodeActivity extends AppCompatActivity {
    private static final String TAG = EnterPasscodeActivity.class.getCanonicalName();
    private static final int DIALOG_ID_CHANGE_PASSCODE = 1;
    private static final int MAX_ATTEMPTS_COUNT = 3;
    private static final String EXTRA_RESET_PIN = "resetPIN";
    private static final String EXTRA_PENDING_ACTION = "pending_action";
    private static final String EXTRA_SKIP_BIOMETRIC_PROMPT = "skip_biometric_prompt";
    private ObjectAnimator imageViewObjectAnimator = null;
    private TextInputEditText passcodeET;
    private ImageView fingerprintMarker;
    private TextView fingerprintHint;
    private TextView passcodeError,reset;
    private TextView passcodeError2;
    private Button submit;
    private TextView info;
    private View rootView;
    private UnifiedApi api;
    private BiometricAuthHelper mBiometricAuthHelper;
    private PendingIntent pendingAction;
    private boolean skipBiometricPromptOnInit = false;

    public static void start(boolean explicit, Context context) {
        Intent starter = new Intent(context, EnterPasscodeActivity.class);
        if (explicit) {
            starter.putExtra(EXTRA_SKIP_BIOMETRIC_PROMPT, true);
        }
        context.startActivity(starter);
    }

    public static void start(Context context, boolean resetPin) {
        Intent starter = new Intent(context, EnterPasscodeActivity.class);
        starter.putExtra(EXTRA_RESET_PIN, resetPin);
        context.startActivity(starter);
    }

    public static void start(Context context, @Nullable PendingIntent pendingAction, int flags) {
        Intent starter = new Intent(context, EnterPasscodeActivity.class);

        if (pendingAction != null) {
            starter.putExtra(EXTRA_PENDING_ACTION, pendingAction);
        }
        starter.addFlags(flags);

        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_passcode);

        //check if passcode is set
        final Intent intent = getIntent();

        if (savedInstanceState != null) {
            pendingAction = savedInstanceState.getParcelable(EXTRA_PENDING_ACTION);
            skipBiometricPromptOnInit = savedInstanceState.getBoolean(EXTRA_SKIP_BIOMETRIC_PROMPT);
        } else {
            pendingAction = intent.getParcelableExtra(EXTRA_PENDING_ACTION);
            skipBiometricPromptOnInit = intent.getBooleanExtra(EXTRA_SKIP_BIOMETRIC_PROMPT, false);
        }

        if(!intent.hasExtra(EXTRA_RESET_PIN)){
            //appid can be null because we use shared pin
            Long time = LauncherPreference.getLastPasscodeForAppId(this, null, true);
            if(time == 0L){
                ChoosePasscodeActivity.start(this, pendingAction);
                finish();

                return;
            }
        }
        initRootView();

        api = ApiFactory.buildUnifiedApi();

        Typeface typeFaceNew=Typeface.createFromAsset(getAssets(), Constants.MAIN_FONT_PATH);
        passcodeET = findViewById(R.id.passcodeET);
        passcodeET.setTypeface(typeFaceNew);
        passcodeET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                passcodeET.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        passcodeET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    //do stuff
                    submitAction();
                }
                return false;
            }
        });

        info = findViewById(R.id.pinInfo);
        info.setTypeface(typeFaceNew);

        fingerprintMarker = findViewById(R.id.fingerprint_marker);
        fingerprintHint = findViewById(R.id.fingerprint_hint);
        fingerprintHint.setTypeface(typeFaceNew);

        passcodeError = findViewById(R.id.passcodeError);
        passcodeError.setTypeface(typeFaceNew);

        passcodeError2 = findViewById(R.id.passcodeError2);
        passcodeError2.setTypeface(typeFaceNew);

        submit = findViewById(R.id.submit);
        reset = findViewById(R.id.reset);

        SpannableString spanString = new SpannableString(reset.getText());
        spanString.setSpan(new UnderlineSpan(), 0, spanString.length(), 0);
        reset.setText(spanString);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickReset();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitAction();
            }
        });

        if(intent.getBooleanExtra(EXTRA_RESET_PIN, false)){
            changePassCode();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        pendingAction = intent.getParcelableExtra(EXTRA_PENDING_ACTION);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //in case the pending action exists - this means the request for passcode was made from DMACAuthReceiver(or similar)
        //and the getLocked will be false, but we still need to receive the passcode from the user
        boolean canSkip = (pendingAction == null);

        boolean resetPin = getIntent().getBooleanExtra(EXTRA_RESET_PIN, false);

        if (canSkip && !LauncherPreference.getLocked(this) && !resetPin) {
            onUnlocked();
        }

        initBiometricPrompt();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mBiometricAuthHelper != null) {
            mBiometricAuthHelper.cancel();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (pendingAction != null) {
            outState.putParcelable(EXTRA_PENDING_ACTION, pendingAction);
        }
        outState.putBoolean(EXTRA_SKIP_BIOMETRIC_PROMPT, skipBiometricPromptOnInit);
    }

    private void initBiometricPrompt() {
        BiometricSensorState biometricSensorState = FingerprintUtils.getFingerprintSensorState(this);

        String passcodeForFingerprint = LauncherPreference.getPasscodeForFingerprint(this);
        boolean fingerprintsAvailable = false;

        if (!TextUtils.isEmpty(passcodeForFingerprint)) {
            if (biometricSensorState == BiometricSensorState.READY) {
                BiometricPrompt.CryptoObject cryptoObject = CryptoUtils.getCryptoObject();
                if (cryptoObject != null) {
                    fingerprintsAvailable = true;

                    mBiometricAuthHelper = new BiometricAuthHelper(cryptoObject, passcodeForFingerprint);
                    if (skipBiometricPromptOnInit) {
                        skipBiometricPromptOnInit = false;
                    } else {
                        mBiometricAuthHelper.startAuth();
                    }
                } else {
                    LauncherPreference.deletePasscodeForFingerprint(this);
                }
            }
        }

        adjustFingerprintHints(biometricSensorState, fingerprintsAvailable);
    }


    private void adjustFingerprintHints(BiometricSensorState biometricSensorState, boolean fingerprintsAvailable) {
        boolean needFingerprintSetup = false;
        if (biometricSensorState == BiometricSensorState.NO_BIOMETRIC_ENROLMENT) {
            fingerprintHint.setText(R.string.fingerprint_hint_no_fingerprints);
            fingerprintHint.setVisibility(View.VISIBLE);

            needFingerprintSetup = true;
            setFingerprintMarkerColor(ColorStateList.valueOf(Color.RED));
        } else if (biometricSensorState == BiometricSensorState.NOT_BLOCKED) {
            fingerprintHint.setText(R.string.fingerprint_hint_not_blocked);
            fingerprintHint.setVisibility(View.VISIBLE);

            needFingerprintSetup = true;
            setFingerprintMarkerColor(ColorStateList.valueOf(Color.RED));
        } else {
            fingerprintHint.setVisibility(View.GONE);

            setFingerprintMarkerColor(null);
        }


        fingerprintMarker.setVisibility((fingerprintsAvailable || needFingerprintSetup) ? View.VISIBLE : View.GONE);
        //adjust icon with available biometric methods
        if (fingerprintMarker.getVisibility() == View.VISIBLE) {
            switch (FingerprintUtils.getAvailableFeatures(this)) {
                case FACE_ID:
                    fingerprintMarker.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_faceid, null));
                    break;
                case MULTIPLE:
                    fingerprintMarker.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_faceid_and_fingerprint, null));
                    break;
                case FINGERPRINT:
                default:
                    fingerprintMarker.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_fingerprint_white_32dp, null));
            }
        }

        if (needFingerprintSetup) {
            fingerprintMarker.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS));
                }
            });
        } else {
            fingerprintMarker.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBiometricAuthHelper.startAuth();
                }
            });
        }
    }

    private void setFingerprintMarkerColor(ColorStateList color) {
        fingerprintMarker.setImageTintList(color);
    }

    private void initRootView() {
        rootView = findViewById(R.id.pin_rootview);
    }

    private void submitAction(){
        passcodeError.setVisibility(View.GONE);
        passcodeError2.setVisibility(View.GONE);

        if(passcodeET.getText().length() == 0){
            Toast.makeText(getApplicationContext() , getText(R.string.please_enter_your_passcode) ,Toast.LENGTH_LONG).show();
            return;
        }

        String passcode = passcodeET.getText().toString();
        onPasscodeEntered(passcode, false);
    }

    private void onPasscodeEntered(String passcode, boolean fromFingerprint) {
        String passcodeHash = Utils.sha256(passcode);
        PasscodeModel pinModel = LauncherPreference.getHashPasscodeandTryPasscodeForAppId(EnterPasscodeActivity.this, null, passcodeHash, true);//appid can be null because we use shared pin
        if(pinModel == null){
            Intent i = new Intent(EnterPasscodeActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
            return;
        }
        String pinHashPrefs = pinModel.hash;
        if (passcodeHash.equals(pinHashPrefs) && pinModel.tryPasscode < MAX_ATTEMPTS_COUNT){
            passcodeET.setClickable(false);
            imageViewObjectAnimator = UIUtils.createViewRotateXAnimation(submit);
            imageViewObjectAnimator.start();

            LauncherPreference.savePasscodeForFingerprint(EnterPasscodeActivity.this, passcode);
            LauncherPreference.setLastLoginForAppId(EnterPasscodeActivity.this, null, true); //appid can be null because we use shared pin

            UIUtils.hideKeyboard(this, passcodeET);

            LauncherPreference.setLocked(EnterPasscodeActivity.this, false);
            onUnlocked();
        }else {
            if (fromFingerprint) {
                onFingerprintPasscodeError();
            }

            shakeError(pinModel);
        }
    }

    private void onFingerprintPasscodeError() {
        LauncherPreference.deletePasscodeForFingerprint(this);
        CryptoUtils.deleteInvalidKey();

        BiometricSensorState biometricSensorState = FingerprintUtils.getFingerprintSensorState(this);
        adjustFingerprintHints(biometricSensorState, false);
    }

    private void onUnlocked() {
        if (pendingAction != null) {
            try {
                pendingAction.send();
            } catch (PendingIntent.CanceledException e) {
            }
        }else{
            //show Market
            startMainActivity();
        }
        finish();
    }

    private void onClickReset() {
        //delete old PIN now
        showDialog(DIALOG_ID_CHANGE_PASSCODE);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_ID_CHANGE_PASSCODE) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
            builder.setTitle(getString(R.string.change_passcode));
            builder.setMessage(getString(R.string.to_change_your_passcode));
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    changePassCode();
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            return builder.create();
        }

        return super.onCreateDialog(id);
    }

    private void changePassCode() {
        //Set flag that we need to change passcode
        LauncherPreference.setChangePasscode(this, true);

        String email = LauncherPreference.getClientEmail(EnterPasscodeActivity.this);
        if(email.isEmpty()){
            Intent i = new Intent(EnterPasscodeActivity.this, LoginActivity.class);
            startActivity(i);
        } else {
            boolean azureLogin = LauncherPreference.isAzureLogin(this);
            if (azureLogin) {
                azureAuthorization(email);
            } else {
                requestPin(email);
            }
        }
    }

    void shakeError(PasscodeModel pinModel) {

        passcodeError.setVisibility(View.VISIBLE);

        if (imageViewObjectAnimator != null && imageViewObjectAnimator.isRunning()) {
            Utils.logI(TAG, "animEnd: onDestroy() ");
            imageViewObjectAnimator.end();
        }

        if (passcodeET != null && submit != null){
            passcodeET.setText("");
            UIUtils.shakeView(submit);
        }

        if(pinModel!=null && pinModel.tryPasscode > MAX_ATTEMPTS_COUNT){
            passcodeError.setVisibility(View.GONE);
            passcodeError2.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (imageViewObjectAnimator != null && imageViewObjectAnimator.isRunning()) {
            Utils.logI(TAG, "animEnd: onDestroy() ");
            imageViewObjectAnimator.end();
        }
    }

    private void azureAuthorization(final String email) {
        Utils.logI(TAG, "azureAuthorization " + email);

        new AzureLogin(this, new AzureLogin.AzureLoginCallback() {
            @Override
            public void onSuccess(String token) {
                ChoosePasscodeActivity.start(EnterPasscodeActivity.this);
                finish();
            }

            @Override
            public void onError() {
                Utils.logI(TAG, "azureAuthorization failed");
                showError(R.string.aad_login_failed);
            }
        }).login();
    }

    private void requestPin(final String email) {
        final int state =  Utils.generateCode();

        DeviceInfo deviceInfo = Utils.buildDeviceInfo(this);

        Call<GetPinResponse> requestPin = api.getPin(new GetPinRequest(email, state, deviceInfo), ApiFactory.getApiKey());
        requestPin.enqueue(new RegistrationCallback<GetPinResponse>(state) {
            @Override
            protected void onSuccess(GetPinResponse pinResponse) {
                submit.setClickable(true);

                if (pinResponse.isPinSent()) {
                    LauncherPreference.setRegistrationType(getApplicationContext(), RegistrationType.EMAIL);
                    LauncherPreference.setPinState(EnterPasscodeActivity.this, String.valueOf(pinResponse.state), System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(pinResponse.expiresIn));
                    LauncherPreference.setClientEmail(getApplicationContext(), email);

                    PinActivity.start(EnterPasscodeActivity.this, email, true, pendingAction);

                    if (imageViewObjectAnimator != null && imageViewObjectAnimator.isRunning()) {
                        imageViewObjectAnimator.end();
                    }
                    finish();
                } else {
                    showError(R.string.request_pin_error);
                }
            }

            @Override
            protected void onError(@Nullable RegistrationErrorResponse error) {
                submit.setClickable(true);

                if (error != null) {
                    if (error.isPinSentError()) {
                        PinActivity.start(EnterPasscodeActivity.this, email, true, pendingAction);

                        if (imageViewObjectAnimator != null && imageViewObjectAnimator.isRunning()) {
                            imageViewObjectAnimator.end();
                        }
                        LauncherPreference.setClientEmail(getApplicationContext(), email);
                        finish();
                    } else if (error.isNotRegisteredError()) {
                        showError(R.string.email_not_registered);
                    } else {
                        showError(R.string.request_pin_error);
                    }
                } else {
                    showError(R.string.request_pin_error);
                }
            }

            @Override
            public void onFailure(Call<GetPinResponse> call, Throwable t) {
                Utils.logE(TAG , "onFailure " + t.getMessage());
                if(rootView == null) {
                    initRootView();
                }
                if (rootView != null) {
                    Snackbar
                            .make(rootView, R.string.unable_to_connect, Snackbar.LENGTH_LONG)
                            .show();
                }

                submit.setClickable(true);
                shakeError(null);
            }
        });
    }

    private void showError(int stringResId) {
        Snackbar.make(rootView, stringResId, Snackbar.LENGTH_LONG).show();
        shakeError(null);
    }

    private void showError(String error) {
        Snackbar.make(rootView, error, Snackbar.LENGTH_LONG).show();
        shakeError(null);
    }

    private void startMainActivity() {
        MainActivity.start(this);

        if (imageViewObjectAnimator != null && imageViewObjectAnimator.isRunning()) {
            Utils.logI(TAG, "animEnd: onDestroy() ");
            imageViewObjectAnimator.end();
        }
    }

    private class BiometricAuthHelper extends BiometricPrompt.AuthenticationCallback {
        private BiometricPrompt.CryptoObject cryptoObject;
        private final String cryptedPasscode;

        private Executor executor;
        private BiometricPrompt biometricPrompt;
        private BiometricPrompt.PromptInfo promptInfo;

        public BiometricAuthHelper(BiometricPrompt.CryptoObject cryptoObject, String cryptedPasscode) {
            this.cryptoObject = cryptoObject;
            this.cryptedPasscode = cryptedPasscode;

            executor = ContextCompat.getMainExecutor(EnterPasscodeActivity.this);
            biometricPrompt = new BiometricPrompt(EnterPasscodeActivity.this, executor, this);

            promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle(getString(R.string.biometric_passcode, getString(R.string.app_name)))
                    .setNegativeButtonText(getString(R.string.enter_passcode_manually))
                    .setConfirmationRequired(false)
                    .build();
        }

        @Override
        public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
            super.onAuthenticationError(errorCode, errString);

            if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                fingerprintMarker.setVisibility(View.GONE);
            }
        }

        @Override
        public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);

            Cipher cipher = result.getCryptoObject().getCipher();
            String decodedPasscode = CryptoUtils.decode(cryptedPasscode, cipher);

            onPasscodeEntered(decodedPasscode, true);
        }

        public void startAuth() {
            biometricPrompt.authenticate(promptInfo, cryptoObject);
        }

        public void cancel() {
            biometricPrompt.cancelAuthentication();
        }
    }
}
