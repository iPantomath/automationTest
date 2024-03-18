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
import androidx.annotation.StringRes;

import com.dhl.demp.dmac.ui.MainActivity;
import com.dhl.demp.mydmac.utils.Constants;
import com.google.android.material.textfield.TextInputEditText;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dhl.demp.mydmac.LauncherPreference;
import com.dhl.demp.mydmac.ui.SimpleTextWatcher;
import com.dhl.demp.mydmac.utils.UIUtils;
import com.dhl.demp.mydmac.utils.Utils;

import mydmac.R;

/**
 * Created by petlebed on 10/08/17.
 */
public class ChoosePasscodeActivity extends AppCompatActivity {
    private static final String TAG = ChoosePasscodeActivity.class.getCanonicalName();
    private static final String EXTRA_PENDING_ACTION = "pending_action";
    private static final int DIALOG_ID_HINT = 1;
    private static final int PASSCODE_MIN_LENGTH = 4;
    private static final int PASSCODE_MAX_LENGTH = 6;
    private ObjectAnimator imageViewObjectAnimator = null;
    private TextInputEditText passcodeET, passcodeETR;
    private TextView passcodeError;
    private Button submit;
    private TextView info,info2;
    private ImageView infoIcon;

    public static void start(Context context) {
        Intent starter = new Intent(context, ChoosePasscodeActivity.class);
        context.startActivity(starter);
    }

    public static void start(Context context, @Nullable PendingIntent pendingAction) {
        Intent starter = new Intent(context, ChoosePasscodeActivity.class);
        if (pendingAction != null) {
            starter.putExtra(EXTRA_PENDING_ACTION, pendingAction);
        }
        context.startActivity(starter);
    }

    public static void start(Context context, @Nullable PendingIntent pendingAction, int flags) {
        Intent starter = new Intent(context, ChoosePasscodeActivity.class);
        if (pendingAction != null) {
            starter.putExtra(EXTRA_PENDING_ACTION, pendingAction);
        }
        starter.addFlags(flags);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_passcode);

        Typeface typeFaceNew=Typeface.createFromAsset(getAssets(), Constants.MAIN_FONT_PATH);
        passcodeET = (TextInputEditText) findViewById(R.id.passcodeET);
        passcodeET.setTypeface(typeFaceNew);
        passcodeET.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                passcodeET.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            }
        });


        passcodeETR = (TextInputEditText) findViewById(R.id.passcodeETR);
        passcodeETR.setTypeface(typeFaceNew);
        passcodeETR.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                passcodeETR.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            }
        });

        infoIcon= (ImageView) findViewById(R.id.infoIcon);
        infoIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(DIALOG_ID_HINT);
            }
        });

        info = (TextView) findViewById(R.id.passcodeInfo);
        info.setTypeface(typeFaceNew);
        info2 = (TextView) findViewById(R.id.passcodeInfo2);
        info2.setTypeface(typeFaceNew);



        passcodeError = (TextView) findViewById(R.id.passcodeError);
        passcodeError.setTypeface(typeFaceNew);

        submit = (Button) findViewById(R.id.submit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passcodeError.setVisibility(View.GONE);

                if (validateUserInput()){
                    passcodeET.setClickable(false);
                    imageViewObjectAnimator = UIUtils.createViewRotateXAnimation(submit);
                    imageViewObjectAnimator.start();

                    String passcode = passcodeET.getText().toString();
                    String passcodeHash = Utils.sha256(passcode);
                    LauncherPreference.addHASHPasscode(ChoosePasscodeActivity.this, null, passcodeHash, true, 0); //appid can be null because we use shared pin
                    LauncherPreference.savePasscodeForFingerprint(ChoosePasscodeActivity.this, passcode);

                    //Unset flag that we need to change passcode
                    LauncherPreference.setChangePasscode(ChoosePasscodeActivity.this, false);

                    //if app was locked - unlock it
                    LauncherPreference.setLocked(ChoosePasscodeActivity.this, false);

                    UIUtils.hideKeyboard(ChoosePasscodeActivity.this, passcodeET);

                    if (getIntent().hasExtra(EXTRA_PENDING_ACTION)) {
                        PendingIntent pendingAction = getIntent().getParcelableExtra(EXTRA_PENDING_ACTION);
                        try {
                            pendingAction.send();
                        } catch (PendingIntent.CanceledException e) {
                        }
                    } else {
                        MainActivity.start(ChoosePasscodeActivity.this);
                    }
                    finish();
                }
            }
        });
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_ID_HINT) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ChoosePasscodeActivity.this, R.style.MyAlertDialogStyle);
            builder.setMessage(getString(R.string.corporate_apps_may_require_dialog));
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

    /**
     * Method validates user's input. In case of an invalid entered data - the appropriate error message will be shown
     * @return True if the entered data is valid, false otherwise
     */
    private boolean validateUserInput() {
        String passcodeETValue = passcodeET.getText().toString();
        if (passcodeETValue.equals("")) {
            showError(R.string.passcode_not_set_error);
            return false;
        }

        if (passcodeETValue.length() < PASSCODE_MIN_LENGTH || passcodeETValue.length() > PASSCODE_MAX_LENGTH) {
            showError(R.string.passcode_is_invalid_error);
            return false;
        }

        if (!passcodeETR.getText().toString().equals(passcodeETValue)) {
            showError(R.string.passcode_mismatch_error);
            return false;
        }

        return true;
    }

    private void showError(@StringRes int errorMessageRes) {
        passcodeError.setText(errorMessageRes);
        passcodeError.setVisibility(View.VISIBLE);

        if (passcodeET != null) {
            passcodeET.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
        }

        if (imageViewObjectAnimator != null && imageViewObjectAnimator.isRunning()) {
            Utils.logI(TAG, "animEnd: onDestroy() ");
            imageViewObjectAnimator.end();
        }

        if (submit != null){
            UIUtils.shakeView(submit);
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
}
