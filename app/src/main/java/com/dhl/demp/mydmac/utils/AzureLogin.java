package com.dhl.demp.mydmac.utils;

import android.app.Activity;

import androidx.annotation.Nullable;

import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.IAccount;
import com.microsoft.identity.client.IAuthenticationResult;
import com.microsoft.identity.client.IPublicClientApplication;
import com.microsoft.identity.client.ISingleAccountPublicClientApplication;
import com.microsoft.identity.client.Prompt;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.exception.MsalException;

import mydmac.R;

public class AzureLogin implements IPublicClientApplication.ISingleAccountApplicationCreatedListener,
        ISingleAccountPublicClientApplication.CurrentAccountCallback, AuthenticationCallback {
    private static final String TAG = "AzureLogin";
    private final static String[] SCOPES = { "user.read.all", /* "email", "openid", "profile" */ };

    private ISingleAccountPublicClientApplication singleAccountApp;
    private Activity activity;
    private AzureLoginCallback azureLoginCallback;
    private Boolean signInStarted = false;

    public AzureLogin(Activity activity, AzureLoginCallback azureLoginCallback) {
        this.activity = activity;
        this.azureLoginCallback = azureLoginCallback;
    }

    public void login() {
        if (singleAccountApp != null) {
            loadAccount();
            return;
        }else {
            PublicClientApplication
                    .createSingleAccountPublicClientApplication(activity.getApplicationContext(),
                            R.raw.auth_config_single_account,
                            this);
        }
    }

    @Override
    public void onCreated(ISingleAccountPublicClientApplication application) {
        singleAccountApp = application;

        IAccount account = null;
        try {
            application.getCurrentAccountAsync(this);
        } catch (Exception ex) {
            ex.getMessage();
        }

//        if (account != null) {
//            azureLoginCallback.onSuccess(account.getIdToken());
//        } else {
//            loadAccount();
//        }
    }

    @Override
    public void onError(MsalException exception) {
        Utils.logI(TAG, exception.getMessage());

        azureLoginCallback.onError();
    }

    private void loadAccount() {
        if (singleAccountApp == null) {
            azureLoginCallback.onError();

            return;
        }

        singleAccountApp.getCurrentAccountAsync(this);
    }

    @Override
    public void onAccountLoaded(@Nullable IAccount activeAccount) {
        if(!signInStarted) {
            signInStarted=true;
            signIn(activeAccount != null);
        }
    }

    @Override
    public void onAccountChanged(@Nullable IAccount priorAccount, @Nullable IAccount currentAccount) {
        if(!signInStarted){
            signInStarted=true;
            signIn(false);
        }
    }

    private void signIn(boolean isSignedIn) {
        if (isSignedIn) {
            singleAccountApp.signInAgain(activity, SCOPES, Prompt.LOGIN, this);
        } else {
            singleAccountApp.signIn(activity, null, SCOPES, Prompt.LOGIN, this);
        }
    }

    @Override
    public void onSuccess(IAuthenticationResult authenticationResult) {
        IAccount account = authenticationResult.getAccount();

        if (account != null) {
            azureLoginCallback.onSuccess(account.getIdToken());
        } else {
            azureLoginCallback.onError();
        }
    }

    @Override
    public void onCancel() {
        azureLoginCallback.onError();
    }

    public interface AzureLoginCallback {
        void onSuccess(String token);

        void onError();
    }
}
