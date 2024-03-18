package com.dhl.demp.mydmac.installer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.collection.LongSparseArray;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.dhl.demp.dmac.data.repository.AppRepositoryLegacy;
import com.dhl.demp.dmac.data.repository.GetAppLatestMD5callback;
import com.dhl.demp.mydmac.LauncherApplication;
import com.dhl.demp.mydmac.LauncherPreference;
import com.dhl.demp.mydmac.analytics.Analytics;
import com.dhl.demp.mydmac.obj.InstallationAppItem;
import com.dhl.demp.mydmac.utils.Constants;
import com.dhl.demp.mydmac.utils.FileUtils;
import com.dhl.demp.mydmac.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mydmac.R;

/**
 * Created by robielok on 6/26/2017.
 */

public class AppInstallerService extends Service implements ApkDownloadManagerListener {
    private static final String TAG = "AppInstallerService";
    private static final String APPLICATION_MIME_TYPE = "application/vnd.android.package-archive";
    private static final String KEY_URL = "url";
    private static final String KEY_MD5 = "md5";
    private static final String KEY_APP_NAME = "appName";
    private static final String KEY_APP_ID = "appID";
    private static final String KEY_DEPENDENCIES = "dependencies";
    private static final String KEY_LAUNCH_AFTER_INSTALLATION= "launchAfterInstallation";
    private static final String KEY_EVENT_TYPE = "event_type";

    public static final String ACTION_APP_INSTALL_EVENT = "com.dhl.demp.mydmac.action.INSTALL_EVENT";

    private static Set<String> installingApps;
    private LongSparseArray<AppInfo> downloadingAppsInfo = new LongSparseArray<>();
    private Map<String, Boolean> launchAfterInstallationContainer = new HashMap<>();

    private ApkDownloadManager apkDownloadManager;

    private BroadcastReceiver installCompleteReceiver = new InstallCompleteReceiver();

    public static void installApp(
            Context context,
            String appID,
            String appName,
            String appVersion,
            String source,
            String md5,
            @Nullable InstallationAppItem[] dependencies,
            boolean launchAfterInstallation
    ) {
        Intent intent = new Intent(context, AppInstallerService.class);
        intent.putExtra(KEY_APP_ID, appID);
        intent.putExtra(KEY_APP_NAME, appName);
        intent.putExtra(KEY_URL, source);
        intent.putExtra(KEY_MD5, md5);
        if (dependencies != null) {
            intent.putExtra(KEY_DEPENDENCIES, dependencies);
        }
        intent.putExtra(KEY_LAUNCH_AFTER_INSTALLATION, launchAfterInstallation);

        context.startService(intent);

        Analytics.sendAppDownload(appVersion, appID);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        installingApps = new HashSet<>();

        apkDownloadManager = createApkDownloadManager();
        apkDownloadManager.onAttach(this);

        IntentFilter packageActionsFilter = new IntentFilter(AppInstallerActivity.ACTION_INSTALLATION_RESULT);
        LocalBroadcastManager.getInstance(this).registerReceiver(installCompleteReceiver, packageActionsFilter);
    }

    @Override
    public void onDestroy() {
        apkDownloadManager.onDetach();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(installCompleteReceiver);

        installingApps = null;

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String url = intent.getStringExtra(KEY_URL);
        String appName = intent.getStringExtra(KEY_APP_NAME);
        String appID = intent.getStringExtra(KEY_APP_ID);
        String md5 = intent.getStringExtra(KEY_MD5);
        List<AppInfo> dependencies = extractDependencies(intent);
        boolean launchAfterInstallation = intent.getBooleanExtra(KEY_LAUNCH_AFTER_INSTALLATION, false);

        installApp(url, appName, appID, md5, dependencies, launchAfterInstallation);

        return START_NOT_STICKY;
    }

    private AppRepositoryLegacy getAppRepositoryLegacy() {
        return ((LauncherApplication)getApplication()).appRepositoryLegacy.get();
    }

    private List<AppInfo> extractDependencies(Intent intent) {
        if (intent.hasExtra(KEY_DEPENDENCIES)) {
            Parcelable[] items = intent.getParcelableArrayExtra(KEY_DEPENDENCIES);
            List<AppInfo> result = new ArrayList<>(items.length);

            List<AppInfo> emptyDedependencies = Collections.emptyList();
            for (int i = 0; i < items.length; i++) {
                InstallationAppItem item = (InstallationAppItem)items[i];
                result.add(new AppInfo(item.appId, item.appName, item.source, item.md5, emptyDedependencies));
            }

            return result;
        } else {
            return Collections.emptyList();
        }
    }

    public void installApp(String url, String appName, String appID, String md5, List<AppInfo> dependencies, boolean launchAfterInstallation) {
        //don't load the same app twice
        if (isAppInDownloadQueue(appID)) {
            Utils.logI(TAG, "app is already in download queue");
            return;
        }

        emitEvent(Event.TYPE_STARTING_DOWNLOAD, appID);
        installingApps.add(appID);
        launchAfterInstallationContainer.put(appID, launchAfterInstallation);

        downloadNext(new AppInfo(appID, appName, url, md5, dependencies));
    }

    private void downloadNext(AppInfo appInfo) {
        AppInfo targetAppInfo = appInfo;
        //if the app has dependencies - first install dependencies
        if (!appInfo.dependencies.isEmpty()) {
            targetAppInfo = appInfo.dependencies.get(0);
        }

        //create request data
        Uri uri = Uri.parse(targetAppInfo.url);
        Map<String, String> headers = new HashMap(1);
        headers.put("Authorization", "Bearer " + LauncherPreference.getToken(getApplicationContext()));

        targetAppInfo.downloadId = apkDownloadManager.download(uri, headers, targetAppInfo.appId, targetAppInfo.appName, targetAppInfo.appId);
        downloadingAppsInfo.put(targetAppInfo.downloadId, appInfo);

        Utils.logI(TAG, "installApp: downID: " + targetAppInfo.downloadId + " " + targetAppInfo.url + " " + targetAppInfo.appId + " " + targetAppInfo.md5);
    }

    @Override
    public void onDownloadComplete(final long downloadId) {
        AppInfo appInfo = downloadingAppsInfo.get(downloadId);
        if (appInfo == null) {
            Utils.logE(TAG, "App info not found");

            return;
        }

        String appId = appInfo.appId;

        if (appInfo.downloadId == downloadId) {
            //app itself was downloaded
            downloadingAppsInfo.delete(downloadId);
            installingApps.remove(appId);
        } else {
            //dependency was downloaded, find it and don't delete original appInfo from the list. But delete loaded app form the dependencies
            for (int i = 0; i < appInfo.dependencies.size(); i++) {
                if (appInfo.dependencies.get(i).downloadId == downloadId) {
                    appInfo = appInfo.dependencies.remove(i);
                    break;
                }
            }
        }

        String mimeTypeForDownloadedFile = apkDownloadManager.getMimeType(downloadId);
        Utils.logI(TAG, "MimeType: " + mimeTypeForDownloadedFile);

        if (!APPLICATION_MIME_TYPE.equals(mimeTypeForDownloadedFile)) {
            //clean up
            downloadingAppsInfo.delete(downloadId);
            installingApps.remove(appId);

            emitEvent(Event.TYPE_FILE_CORRUPTED, appInfo.appId);

            return;
        }

        File file = apkDownloadManager.getFile(downloadId, appInfo.appId);

        if (file == null) {
            Utils.logI(TAG, "apk file wasn't copied");

            //clean up
            downloadingAppsInfo.delete(downloadId);
            installingApps.remove(appId);

            return;
        }

        String md5 = FileUtils.getMD5(file);
        Utils.logI(TAG, "md5check: " + md5 + " " + appInfo.md5);

        if (md5 != null && md5.equals(appInfo.md5)) {
            startInstallation(appId, appInfo.appName, file.getAbsolutePath());
        } else {
            String appName = appInfo.appName;
            getAppRepositoryLegacy().getAppLatestMD5(appId, new GetAppLatestMD5callback() {
                @Override
                public void onLatestMD5(@Nullable String latestMD5) {
                    if (md5 != null && md5.equals(latestMD5)) {
                        startInstallation(appId, appName, file.getAbsolutePath());
                    } else {
                        //clean up
                        downloadingAppsInfo.delete(downloadId);
                        installingApps.remove(appId);

                        file.delete();

                        emitEvent(Event.TYPE_MD5_DONOT_MATCH, appId);
                    }
                }
            });
        }
    }

    private void startInstallation(String appId, String appName, String apkPath) {
        if (canShowActivity()) {
            AppInstallerActivity.start(this, appId, apkPath);
        } else {
            showInstallNotification(appId, appName, apkPath);
        }
    }

    private boolean canShowActivity() {
        return Utils.beforeAndroid10() || Utils.isAppInForeground();
    }

    private void showInstallNotification(String appId, String appName, String apkPath) {
        int notificationId = (int)(System.currentTimeMillis() % Integer.MAX_VALUE);

        Utils.createImportantNotificationChannel(this);

        PendingIntent action = AppInstallerActivity.createPendingIntent(this, notificationId, appId, apkPath);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_IMPORTANT);
        builder.setAutoCancel(true)
                .setOngoing(false)
                .setContentIntent(action)
                .setSmallIcon(R.drawable.ic_notification_wrapper)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentTitle(appName)
                .setContentText(getString(R.string.tap_to_install));

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        notificationManager.notify(notificationId, builder.build());
    }


    private void onInstallFinished(String appId, String apkPath, boolean success) {
        //remove downloaded file
        File file = new File(apkPath);
        file.delete();

        AppInfo appInfo = findAppInfo(appId);
        deleteAppInfoByAppId(appId);

        if (!success) {
            //clean up
            installingApps.remove(appId);
        }

        if (appInfo != null && success) {
            //This app is the app with dependencies, continue the installation
            downloadNext(appInfo);
        } else {
            emitEvent(Event.TYPE_INSTALL_FINISHED, appId);
        }

        if (success
                && launchAfterInstallationContainer.containsKey(appId)
                && launchAfterInstallationContainer.get(appId)) {
            //Do 1 second delay because not always installed app is available right in this moment of time
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    launchApp(appId);
                }
            }, 1000);
        }
    }

    private void launchApp(String appId) {
        String packageName = getAppRepositoryLegacy().getPackageName(appId);
        if (packageName == null) {
            packageName = appId;
        }

        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(launchIntent);
        }
    }

    private void emitEvent(int type, String appId) {
        Intent intent = new Intent(ACTION_APP_INSTALL_EVENT);
        intent.putExtra(KEY_EVENT_TYPE, type);
        intent.putExtra(KEY_APP_ID, appId);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private boolean isAppInDownloadQueue(String appId) {
        for (int i = 0; i < downloadingAppsInfo.size(); i++) {
            if (appId.equals(downloadingAppsInfo.valueAt(i).appId)) {
                return true;
            }
        }

        return false;
    }

    @Nullable
    private AppInfo findAppInfo(String appId) {
        for (int i = 0; i < downloadingAppsInfo.size(); i++) {
            if (appId.equals(downloadingAppsInfo.valueAt(i).appId)) {
                return downloadingAppsInfo.valueAt(i);
            }
        }

        return null;
    }

    private void deleteAppInfoByAppId(String appId) {
        for (int i = downloadingAppsInfo.size() - 1; i >= 0; i--) {
            if (appId.equals(downloadingAppsInfo.valueAt(i).appId)) {
                downloadingAppsInfo.removeAt(i);
            }
        }
    }

    public static boolean isAppInstalling(String appId) {
        return (installingApps != null && installingApps.contains(appId));
    }

    private ApkDownloadManager createApkDownloadManager() {
        return new ApkDownloadManagerImpl(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class InstallCompleteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String appId = intent.getStringExtra(AppInstallerActivity.EXTRA_APP_ID);
            String apkPath = intent.getStringExtra(AppInstallerActivity.EXTRA_APK_PATH);
            boolean success = intent.getBooleanExtra(AppInstallerActivity.EXTRA_SUCCESS, false);

            onInstallFinished(appId, apkPath, success);
        }
    }

    private static class AppInfo {
        private final String appId;
        private final String appName;
        private final String url;
        private final String md5;
        private final List<AppInfo> dependencies;
        public long downloadId = -1;

        public AppInfo(String appId, String appName, String url, String md5, List<AppInfo> dependencies) {
            if (dependencies == null) {
                throw new IllegalArgumentException("dependencies can't be null");
            }

            this.appId = appId;
            this.appName = appName;
            this.url = url;
            this.md5 = md5;
            this.dependencies = dependencies;
        }

        @Override
        public String toString() {
            return "AppInfo{" +
                    "appId='" + appId + '\'' +
                    ", appName='" + appName + '\'' +
                    ", md5='" + md5 + '\'' +
                    '}';
        }
    }

    public static class Event {
        public static final int TYPE_STARTING_DOWNLOAD = 0;
        public static final int TYPE_FILE_CORRUPTED = 1;
        public static final int TYPE_MD5_DONOT_MATCH = 2;
        public static final int TYPE_INSTALL_FINISHED = 3;

        public final int type;
        public final String appId;

        public Event(Bundle extras) {
            type = extras.getInt(KEY_EVENT_TYPE);
            appId = extras.getString(KEY_APP_ID);
        }
    }
}
