package com.dhl.demp.mydmac.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;

import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.dhl.demp.dmac.model.RegistrationType;
import com.dhl.demp.mydmac.LauncherApplication;
import com.dhl.demp.mydmac.LauncherPreference;
import com.dhl.demp.mydmac.activity.AutoRegActivity;
import com.dhl.demp.mydmac.activity.LauncherActivity;
import com.dhl.demp.mydmac.activity.LoginActivity;
import com.dhl.demp.mydmac.api.ApiFactory;
import com.dhl.demp.mydmac.api.UnifiedApi;
import com.dhl.demp.mydmac.api.request.GetTokenRequest;
import com.dhl.demp.mydmac.api.response.GetTokenResponse;
import com.dhl.demp.mydmac.api.response.RefreshTokenResponse;
import com.dhl.demp.mydmac.notifications.DeleteFCMtokenService;
import com.dhl.demp.mydmac.obj.Apka;
import com.dhl.demp.mydmac.obj.AppCategory;
import com.dhl.demp.mydmac.obj.DeviceInfo;
import com.dhl.demp.mydmac.obj.DeviceInfoExpress;
import com.dhl.demp.mydmac.service.DMACJobsScheduler;
import com.dhl.demp.mydmac.service.GuardService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import mydmac.BuildConfig;
import mydmac.R;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by jahofman on 3/7/2016.
 */
public class Utils {
    private static final String FLAVOR_NAME_DMAC = "dmac";
    private static final String FLAVOR_NAME_EXPRESS = "express";
    private static final String FLAVOR_NAME_DP = "dp";
    private static final String FLAVOR_NAME_DHL = "dhl";
    private static final String FLAVOR_NAME_DMAC_E = "dmac_e";
    public static final String FLAVOR_NAME_DMAC_TIP = "dmacTip";
    public static final String FLAVOR_NAME_TIP_EXPRESS = "expressTip";
    private static final String FLAVOR_NAME_3CA = "tca";
    private static final String CHECKSUM_SALT = "98088ZTRRGUZR3QGGBSHGTWB?CRD5O7548082HBSH98RTZZHUHESWSJMNNHGV";
    public static final int HTTP_CODE_SERVER_UNAVAILABLE = 503;
    public static final Pattern PHONE_REGEX = Pattern.compile("(\\+|00)[0-9]{1,30}");

    public static boolean isLoginValid(String login) {
        return Patterns.EMAIL_ADDRESS.matcher(login).matches() || PHONE_REGEX.matcher(login).matches();
    }

    public static boolean isEmail(String login) {
        return Patterns.EMAIL_ADDRESS.matcher(login).matches();
    }

    public static String generateUniqueId(Context ctx) {
        return getDeviceUniqueId(ctx) + "_" + System.currentTimeMillis();
    }

    public static String sha256(String s) {
        if (TextUtils.isEmpty(s)) {
            return "";
        }

        try {
            // Create SHA 256 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String hex = Integer.toHexString(0xFF & messageDigest[i]);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
                //hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getChecksum(String src) {
        String st = CHECKSUM_SALT + src;
        return Utils.sha256(st);
    }

    public static String generateHashToken(String token, String clientId, String appId) {
        String hashToken = Utils.sha256(token + clientId + appId + "sdhsfdhfdshdfgh65465458487461yttuoi");

        return hashToken;
    }

    public static boolean sendDMACCallback(Context context, Uri data, String targetPackageId) {
        Intent intent = new Intent();
        if (!TextUtils.isEmpty(targetPackageId)) {
            intent.setPackage(targetPackageId);
        }
        intent.setData(data);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            return false;
        }

        return true;
    }

    public static void doAppAction(Context ctx, String appId, Apka.Action action) {
        doAppAction(ctx, appId, action.targetPackageId, action.url);
    }

    public static void doAppAction(Context ctx, String appId, String targetPackageId, String actionUrl) {
        String clientId = LauncherPreference.getId(ctx);
        String token = LauncherPreference.getRefToken(ctx);
        //in case action.targetPackageId is not empty - we have to use it for generating hashToken
        String hashToken = Utils.generateHashToken(token, clientId,
                TextUtils.isEmpty(targetPackageId) ? appId : targetPackageId);

        String url = actionUrl.replace("$client_id", clientId).replace("$hash_token", hashToken);

        sendDMACCallback(ctx, Uri.parse(url), targetPackageId);
    }

    public static int generateCode() {
        return (int)(System.currentTimeMillis() % Integer.MAX_VALUE);
    }

    public static void log(String TAG, String body) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, body);
    }

    public static void logE(String TAG, String body) {
        if (BuildConfig.DEBUG)
            Log.e(TAG, body);
    }

    public static void logI(String TAG, String body) {
        if (BuildConfig.DEBUG)
            Log.i(TAG, body);
    }

    public static void updateTokens(Context context, RefreshTokenResponse response) {
        LauncherPreference.setToken(context, response.accessToken);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, LauncherPreference.refreshTokenAfterDefinedDays);
        LauncherPreference.setTokenExpiry(context, cal.getTimeInMillis());

        if (response.refreshToken != null) {
            LauncherPreference.setRefToken(context, response.refreshToken, response.expiresIn);
        } else {
            LauncherPreference.setExpiresIn(context, response.expiresIn);
        }

        DMACJobsScheduler.scheduleSendDeviceInfo(context, response.getDataCollectionPeriod());
    }

    public static void updateTokens(Context context, GetTokenResponse getTokenResponse) {
        LauncherPreference.setToken(context, getTokenResponse.accessToken);
        LauncherPreference.setRefToken(context, getTokenResponse.refreshToken, getTokenResponse.expiresIn);
        LauncherPreference.setId(context, getTokenResponse.clientId);

        DMACJobsScheduler.scheduleSendDeviceInfo(context, getTokenResponse.getDataCollectionPeriod());
    }

    public static void cleanUpBeforeLogout(Context context, boolean deleteFCMToken) {
        //delete FCM token
        if (deleteFCMToken) {
            LauncherPreference.setFCMToken(context, "");
            DeleteFCMtokenService.start(context);
        }

        deleteAllTokens(context);
    }

    public static void showLoginScreen(Context context, String oldEmail) {
        Intent newLogin = new Intent(context, useAutologin() ? AutoRegActivity.class : LoginActivity.class);
        newLogin.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        newLogin.putExtra("unregister", true);
        if (oldEmail != null) {
            logI("Utils", "logout: oldEmail: " + oldEmail);
            newLogin.putExtra("oldEmail", oldEmail);
        }
        context.startActivity(newLogin);
    }

    public static void deleteAllTokens(Context context) {
        DMACJobsScheduler.cancelAll(context);

        LauncherPreference.setToken(context, "");
        LauncherPreference.setRefToken(context, "", 0);

        LauncherApplication mApp = (LauncherApplication) context.getApplicationContext();
        mApp.appRepositoryLegacy.get().deleteAllApps();
    }

    public static String getDeviceUniqueId(Context ctx) {
        return Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID)
                + (BuildConfig.DEBUG ? (ctx.getString(R.string.app_name) + "_debug") : "");
    }

    public static String getDevicePhysicalInfo(Context context) {
        String buildVersion = BuildConfig.VERSION_NAME;
        String osVersion = "Android " + Build.VERSION.RELEASE;
        String deviceId = getDeviceUniqueId(context);
        String timeZone = TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT, Locale.US);
        String devModel = Build.MODEL;

        return context.getString(R.string.device_physical_info_template, buildVersion, timeZone, deviceId, osVersion,  devModel);
    }

    public static final int APP_NOT_INSTALLED = 0;
    public static final int APP_INSTALLED = 1;
    public static final int APP_INSTALLED_OTHER_VERSION = 2;

    public static int appInstalledOrNot(Context context, String packageName, String appVersion) {
        //0 = not installed;  app_insatll 1 = versions equal; 2 = ver not equal;
        PackageManager pm = context.getPackageManager();
        int app_installed;
        try {
            PackageInfo info = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            if (info.versionName.equals(appVersion)) {
                app_installed = APP_INSTALLED;
            } else {
                app_installed = APP_INSTALLED_OTHER_VERSION;
            }

        } catch (PackageManager.NameNotFoundException e) {
            app_installed = APP_NOT_INSTALLED;
        }
        return app_installed;
    }

    @Nullable
    public static String getInstalledAppVersion(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static boolean isAppInstalled(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(packageName, PackageManager.GET_META_DATA);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * Method compare two versions
     * @param v1 Version 1 to compare
     * @param v2 Version 2 to compare
     * @return negative int if  v1 < v2, 0 if v1 = v2 and positive int if v1 > v2
     */
    public static int compareVersions(String v1, String v2) {
        String[] segments1 = v1.split("\\.");
        String[] segments2 = v2.split("\\.");
        int cmp;
        for (int i = 0; i < Math.min(segments1.length, segments2.length); i++) {
            try {
                int a = Integer.parseInt(segments1[i]);
                int b = Integer.parseInt(segments2[i]);
                cmp = a - b;
            } catch (NumberFormatException nfe) {
                cmp = segments1[i].compareTo(segments2[i]);
            }

            if (cmp != 0) {
                return cmp;
            }
        }

        return 0;
    }

    public static byte[] bitmap2ByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public static Bitmap byteArray2Bitmap(byte[] raw) {
        return BitmapFactory.decodeByteArray(raw, 0, raw.length);
    }


    public static void startGuardService(Context ctx) {
        String manufacter = android.os.Build.MANUFACTURER.toLowerCase();
        if (manufacter.equals("asus") || manufacter.equals("lenovo")) {
            Intent intentService = new Intent(ctx, GuardService.class);
            ctx.startService(intentService);
        }
    }

    public static boolean useAutologin() {
        return BuildConfig.FLAVOR.equals(FLAVOR_NAME_EXPRESS)
                || BuildConfig.FLAVOR.equals(FLAVOR_NAME_DP)
                || BuildConfig.FLAVOR.equals(FLAVOR_NAME_DHL)
                || BuildConfig.FLAVOR.equals(FLAVOR_NAME_TIP_EXPRESS)
                || BuildConfig.FLAVOR.equals(FLAVOR_NAME_3CA);
    }

    public static void startLoginActivity(Context context) {
        if (Utils.useAutologin()) {
            context.startActivity(new Intent(context, AutoRegActivity.class));
        } else {
            context.startActivity(new Intent(context, LoginActivity.class));
        }
    }

    public static boolean equals(Object a, Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }

    public static DrawableTypeRequest createIconRequest(Context context, String iconUrl) {
        if (!TextUtils.isEmpty(iconUrl)) {
            GlideUrl glideUrl = new GlideUrl(iconUrl, new LazyHeaders.Builder()
                    .addHeader("Authorization", "Bearer " + LauncherPreference.getToken(context))
                    .build());
            return Glide.with(context).load(glideUrl);
        } else {
            return Glide.with(context).load(R.drawable.apps_list_default_icon);
        }
    }

    public static String getSerialNumber() {
        String serialnum;
        try {

            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);

            serialnum = (String) (get.invoke(c, "sys.serialnumber", Build.UNKNOWN));
            if(Build.UNKNOWN.equals(serialnum)) {
                serialnum = (String) get.invoke(c, "ril.serialnumber", Build.UNKNOWN);
            }
            if(Build.UNKNOWN.equals(serialnum)) {
                serialnum = Build.SERIAL;
            }
        } catch (Exception ignored) {
            serialnum = Build.SERIAL;
        }

        return serialnum;
    }

    public static String getIMEI(Context context) {
        TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            return tMgr.getDeviceId();
        } catch (SecurityException e) {
        }

        return "";
    }

    public static void sendEmail(Context context, String email, String subject) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        Uri uri = Uri.parse("mailto:" + email);
        intent.setData(uri);
        if (!TextUtils.isEmpty(subject)) {
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }

        context.startActivity(Intent.createChooser(intent, context.getString(R.string.send_email_chooser)));
    }

    public static boolean canAutoRelogin(Context context, int httpCode) {
        //user can relogin in case it was autoregistrated previously and the code is 4xx
        return  (LauncherPreference.isAutoreg(context) && (httpCode >= 400 && httpCode < 500));
    }

    public static DeviceInfo buildDeviceInfo(Context context) {
        if (useAutologin()) {
            String sn = beforeAndroid10() ? getSerialNumber() : LauncherPreference.getSnManual(context);
            String imei = beforeAndroid10() ? getIMEI(context) : LauncherPreference.getImeiManual(context);

            return new DeviceInfoExpress(context, sn, imei);
        }

        return new DeviceInfo(context);
    }

    public static boolean autoReloginBlocking(Context context) {
        boolean successful = false;
        UnifiedApi api = ApiFactory.buildUnifiedApi();

        String sn = beforeAndroid10() ? getSerialNumber() : LauncherPreference.getSnManual(context);
        String imei = beforeAndroid10() ? getIMEI(context) : LauncherPreference.getImeiManual(context);
        DeviceInfoExpress deviceInfoExpress = new DeviceInfoExpress(context, sn, imei);

        //hashed-IMEI+S/N
        String hashed = Utils.getChecksum(deviceInfoExpress.IMEI + deviceInfoExpress.serialNum);
        Call<GetTokenResponse> request = api.autoReg(new GetTokenRequest(Constants.AUTO_REG_SCOPE, deviceInfoExpress.IMEI, hashed, deviceInfoExpress), ApiFactory.getApiKey());

        try {
            Response<GetTokenResponse> response = request.execute();
            if (response.isSuccessful()) {
                GetTokenResponse getTokenResponse = response.body();

                LauncherPreference.setRegistrationType(context, RegistrationType.AUTO);
                updateTokens(context, getTokenResponse);

                successful = true;
            }
        } catch (IOException e) {
            //Nothing to do
        }

        return successful;
    }

    public static boolean isGooglePlayServicesAvailable(Context context){
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        return resultCode == ConnectionResult.SUCCESS;
    }

    public static boolean isLauncherEnabled(Context context) {
        ComponentName component = new ComponentName(context, LauncherActivity.class);
        int enabledSetting = context.getPackageManager().getComponentEnabledSetting(component);

        return enabledSetting == PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
    }

    public static void setLauncherEnabled(Context context, boolean enabled) {
        ComponentName component = new ComponentName(context, LauncherActivity.class);

        int newEnabledSetting = enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        context.getPackageManager().setComponentEnabledSetting(component, newEnabledSetting, PackageManager.DONT_KILL_APP);

        LauncherPreference.setLauncherSwitchedManually(context, true);
    }

    @Nullable
    public static String getLauncherAppPackageName(Context context) {
        Intent intent= new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo defaultLauncher = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);

        if (defaultLauncher != null) {
            return defaultLauncher.activityInfo.packageName;
        } else {
            return null;
        }
    }

    public static boolean isAppInForeground() {
        RunningAppProcessInfo myProcess = new RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(myProcess);

        return myProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
    }

    public static boolean isAndroid10() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }

    public static boolean beforeAndroid10() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q;
    }

    public static void createMinorNotificationChannel(Context context) {
        createNotificationChannel(context, Constants.NOTIFICATION_CHANNEL_MINOR, NotificationManager.IMPORTANCE_LOW,
                R.string.nc_minor_name, R.string.nc_minor_description);
    }

    public static void createImportantNotificationChannel(Context context) {
        createNotificationChannel(context, Constants.NOTIFICATION_CHANNEL_IMPORTANT, NotificationManager.IMPORTANCE_HIGH,
                R.string.nc_important_name, R.string.nc_important_description);
    }

    public static String createFGServiceNotificationChannel(Context context) {
        createNotificationChannel(context, Constants.NOTIFICATION_CHANNEL_FG_SERVICE, NotificationManager.IMPORTANCE_LOW,
                R.string.nc_fg_service_name, R.string.nc_fg_service_description);

        return Constants.NOTIFICATION_CHANNEL_FG_SERVICE;
    }

    private static void createNotificationChannel(Context context, String id, int importance, int nameResId, int descriptionResId) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = context.getString(nameResId);
            String description = context.getString(descriptionResId);
            NotificationChannel channel = new NotificationChannel(id, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static boolean searchInCategories(List<AppCategory> categories, String searchQuery) {
        if (categories == null || categories.isEmpty()) {
            return false;
        }

        for (AppCategory category : categories) {
            if (category.tags != null) {
                for (String tag : category.tags) {
                    if (tag.toLowerCase().contains(searchQuery)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    //this method should be used only in DeviceInfo class
    public static String getDmacFlavorIdForDeviceInfo() {
        //if flavor id is DMAC_E, use DMAC flavor id instead of it, because it must behave the same as DMAC
        return BuildConfig.FLAVOR.equals(FLAVOR_NAME_DMAC_E) ? FLAVOR_NAME_DMAC : BuildConfig.FLAVOR;
    }

    public static String getSecurityPatchLevel() {
        String securityPatch = getBuildExValue("SECURITY_PATCH");
        if (TextUtils.isEmpty(securityPatch) || "unknown".equals(securityPatch)) {
            securityPatch = Build.VERSION.SECURITY_PATCH;
        }
        return securityPatch;
    }

    private static String getBuildExValue(String buildExKey) {
        String buildExValue = "";
        try {
            Class<?> clazz = Class.forName("com.huawei.system.BuildEx");
            Field field = clazz.getDeclaredField(buildExKey);
            field.setAccessible(true);
            buildExValue = (String) field.get(null);
        } catch (Throwable th) {
        }

        return buildExValue;
    }

    public static boolean appListExpired(Context context) {
        long lastRefreshTime = LauncherPreference.getLatestApplistRefreshTime(context);
        long currentTime = Calendar.getInstance().getTimeInMillis();

        long interval = TimeUnit.MILLISECONDS.toMinutes(currentTime - lastRefreshTime);

        boolean isExpired;

        if (interval >= 5) {
            isExpired = true;
        }else{
            isExpired = false;
        }

        return isExpired;
    }
}