package com.dhl.demp.mydmac.installer;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;

import mydmac.BuildConfig;

/**
 * Created by robielok on 11/9/2017.
 */

public class AppInstallerActivity extends AppCompatActivity {
    public static final String ACTION_INSTALLATION_RESULT = "INSTALLATION_RESULT";
    public static final String EXTRA_APP_ID = "app_id";
    public static final String EXTRA_APK_PATH = "apk_path";
    public static final String EXTRA_SUCCESS = "success";
    private static final int REQUEST_CODE_INSTALL = 1;

    private String appId;
    private String apkPath;

    public static void start(Context context, String appId, String apkPath) {
        Intent starter = new Intent(context, AppInstallerActivity.class);
        starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        starter.putExtra(EXTRA_APP_ID, appId);
        starter.putExtra(EXTRA_APK_PATH, apkPath);
        context.startActivity(starter);
    }

    public static PendingIntent createPendingIntent(Context context, int requestCode, String appId, String apkPath) {
        Intent intent = new Intent(context, AppInstallerActivity.class);
        intent.putExtra(EXTRA_APP_ID, appId);
        intent.putExtra(EXTRA_APK_PATH, apkPath);

        return PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appId = getIntent().getStringExtra(EXTRA_APP_ID);
        apkPath = getIntent().getStringExtra(EXTRA_APK_PATH);

        //start installation
        Intent installIntent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        installIntent.setData(fileNameToUri(apkPath));
        installIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        installIntent.putExtra(Intent.EXTRA_RETURN_RESULT, true);

        startActivityForResult(installIntent, REQUEST_CODE_INSTALL);
    }

    private Uri fileNameToUri(String fileName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".files", new File(fileName));
        } else {
            return Uri.parse("file://" + fileName);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_INSTALL) {
            boolean isInstalled = (resultCode == RESULT_OK);

            onInstallationFinished(isInstalled);
        }
    }

    private void onInstallationFinished(boolean isInstalled) {
        Intent intent = new Intent(ACTION_INSTALLATION_RESULT);
        intent.putExtra(EXTRA_APP_ID, appId);
        intent.putExtra(EXTRA_APK_PATH, apkPath);
        intent.putExtra(EXTRA_SUCCESS, isInstalled);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        finish();
    }
}
