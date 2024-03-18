package com.dhl.demp.mydmac;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.dhl.demp.dmac.model.AppCategory;
import com.dhl.demp.dmac.model.DeviceOwner;
import com.dhl.demp.dmac.model.RegistrationType;
import com.dhl.demp.dmac.model.SimOwner;
import com.dhl.demp.mydmac.api.ApiFactory;
import com.dhl.demp.mydmac.obj.BiometricSensorState;
import com.dhl.demp.mydmac.obj.DeviceInfo;
import com.dhl.demp.mydmac.obj.PasscodeModel;
import com.dhl.demp.mydmac.utils.Constants;
import com.dhl.demp.mydmac.utils.Utils;
import com.dhl.demp.mydmac.utils.fingerprint.CryptoUtils;
import com.dhl.demp.mydmac.utils.fingerprint.FingerprintUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mydmac.BuildConfig;
import mydmac.R;

public class LauncherPreference {
    private static final String PREF_WALLPAPER = "pref_wallpaper";
    private static final String PREF_WALLPAPER_TEXT_COLOR = "pref_wallpaper_text_color";
    private static final String PREF_STANDARD_APPS = "standard_apps";
    private static final String PREF_DESCRIPTION = "pref_description";
    private static final String PREF_CODE = "pref_code";
    private static final String PREF_ID = "pref_id";
    private static final String PREF_TOKEN = "pref_token";
    private static final String PREF_REF_TOKEN = "pref_ref_token";
    private static final String EXPIRES_IN = "expires_in";
    private static final String PREF_TOKEN_EXPIRY = "token_expiry";
    private static final String PREF_AUTO_REG = "is_auto_reg";
    private static final String PREF_AZURE_REG = "is_azure_reg";
    private static final String PREF_TOKEN_EXPIRED = "pref_token_expired";
    private static final String PREF_HASH_TOKEN = "pref_hash_token";

    public static final int refreshTokenAfterDefinedDays = 288; // 288 hours= 12 days
    private static final String PREF_FCM_TOKEN = "pref_FCM_token";
    private static final String PREF_CLIENT_EMAIL = "pref_client_email";
    private static final String PREF_HASH_PASSCODE = "pref_hash_passcode";
    private static final String PREF_FINGERPRINT_PASSCODE = "pref_fingerprint_passcode";
    private static final String PREF_CLIENT_ACTIVATED = "pref_client_activated";
    private static final String PREF_SHARED_APP= "com.dhl.dmacauth";
    private static final String PREF_LOCKED = "locked";
    private static final String PREF_CHANGE_PASSCODE = "change_passcode";
    private static final String PREF_PIN_STATE = "pin_state";
    private static final String PREF_PIN_EXPIRATION_TIME = "pin_expiration_time";
    private static final String PREF_UPGRADE_POSTPONED_TO = "upgrade_postponed_to";

    private static final String PREF_URL_TYPE = "url_type";
    private static final String PREF_CUSTOM_URL = "custom_url";
    private static final String PREF_CUSTOM_API_KEY = "custom_api_key";
    private static final String PREF_CUSTOM_SCOPE = "custom_scope";
    private static final String PREF_LAUNCHER_SWITCHED_MANUALLY = "launcher_switched_manually";
    private static final String PREF_SN_MANUAL = "sn_manual";
    private static final String PREF_IMEI_MANUAL = "imei_manual";
    private static final String PREF_REPORTED_DEVICE_INFO = "reported_device_info";

    private static final String PREF_DEVICE_OWNER = "device_owner";
    private static final String PREF_SIM_OWNER = "sim_owner";

    private static final String PREF_APP_CATEGORY_FILTER = "app_category_filter";

    private static final String PREF_LATEST_APPLIST_REFRESH_TIME = "latest_refreshed_time";

    private static final String PREF_SHORTCUT_NAME_LIST = "shortcut_name_list";
    private static final String PREF_SHORTCUT_URL_LIST = "shortcut_url_list";
    private static final String PREF_SHORTCUT_ICON_LIST = "shortcut_icon_list";
    private static final String PREF_EDIT_SHORTCUT_APP_DETAILS = "edit_shortcut_app_details";
    private static final String PREF_CHANGE_SHORTCUT_APP_ICON = "change_shortcut_app_icon";

    private static final String PREF_TEMP_SHORTCUT_NAME = "temp_shortcut_name";
    private static final String PREF_TEMP_SHORTCUT_URL = "temp_shortcut_url";
    private static final String PREF_CURRENT_ICON = "current_icon";
    private static final String PREF_NEW_REGISTRATION = "new_registration";
    private static final String PREF_UNIQUE_ID = "new_unique_id";
    private static final String PREF_APP_PACKAGE_LIST = "app_package_list";
    private static final String PREF_APP_NAME_LIST = "app_name_list";
    private static final String PREF_MARKETPLACE_ICON_LIST = "app_marketplace_icon_list";


    private static SharedPreferences sPreferences;

    private static final Set<AppCategory> DEFAULT_APP_CATEGORY_FILTER = new HashSet<AppCategory>(){{
            add(AppCategory.ALL);
            add(AppCategory.DPDHL_GROUP);
            add(AppCategory.DGFF);
            add(AppCategory.DSC);
            add(AppCategory.ECS);
            add(AppCategory.EXP);
            add(AppCategory.GBS);
            add(AppCategory.CSI);
            add(AppCategory.CC);
            add(AppCategory.PNP);
    }};

    protected static SharedPreferences getPreferences(Context context) {
        if (sPreferences == null) {
            sPreferences = PreferenceManager
                    .getDefaultSharedPreferences(context.getApplicationContext());
        }

        return sPreferences;
    }

    protected static SharedPreferences.Editor getPreferencesEditor(Context context) {
        return getPreferences(context)
                .edit();
    }
    public static String getId(Context context){
        return getPreferences(context)
                .getString(PREF_ID, "");
    }
    public static String getCode(Context context){
        return getPreferences(context)
                .getString(PREF_CODE, "");
    }
    public static void setCode(Context context,String code){
        getPreferencesEditor(context)
                .putString(PREF_CODE, code)
                .commit();
    }
    public static void setId(Context context,String id){
        getPreferencesEditor(context)
                .putString(PREF_ID, id)
                .commit();
    }
    public static String getToken(Context context){
        return getPreferences(context)
                .getString(PREF_TOKEN, "");
    }
    public static void setToken(Context context,String token){
        getPreferencesEditor(context)
                .putString(PREF_TOKEN, token)
                .commit();
    }

    public static void setExpiresIn(Context context, int expiresIn){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND , expiresIn);
        getPreferencesEditor(context)
                .putLong(EXPIRES_IN, cal.getTimeInMillis())
                .commit();
    }

    public static Long getExpiresIn(Context context){
        return getPreferences(context).getLong(EXPIRES_IN, 0);
    }

    public static String getRefToken(Context context){
        return getPreferences(context)
                .getString(PREF_REF_TOKEN, "");
    }

    public static void setTokenExpired(Context context, boolean tokenExpired){
        getPreferencesEditor(context)
                .putBoolean(PREF_TOKEN_EXPIRED, tokenExpired)
                .commit();
    }

    public static boolean getTokenExpired(Context context){
        return getPreferences(context)
                .getBoolean(PREF_TOKEN_EXPIRED, false);
    }

    public static void setHashToken(Context context, boolean hashTokenValid){
        getPreferencesEditor(context)
                .putBoolean(PREF_HASH_TOKEN, hashTokenValid)
                .commit();
    }

    public static boolean getHashToken(Context context){
        return getPreferences(context)
                .getBoolean(PREF_HASH_TOKEN, false);
    }

    public static void setRefToken(Context context,String token,int expires_in){
        getPreferencesEditor(context)
                .putString(PREF_REF_TOKEN, token)
                .commit();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND , expires_in);
        getPreferencesEditor(context)
                .putLong(EXPIRES_IN, cal.getTimeInMillis())
                .commit();
    }

    public static String getWallpaper(Context context) {
        return getPreferences(context).getString(PREF_WALLPAPER, null);
    }

    public static void setWallpaper(Context context, String path) {
        getPreferencesEditor(context)
                .putString(PREF_WALLPAPER, path)
                .commit();
    }

    public static int getWallpaperTextColor(Context context) {
        return getPreferences(context).getInt(PREF_WALLPAPER_TEXT_COLOR, Color.DKGRAY);
    }

    public static void setWallpaperTextColor(Context context, int color) {
        getPreferencesEditor(context)
                .putInt(PREF_WALLPAPER_TEXT_COLOR, color)
                .commit();
    }

    public static int getStandardApps(Context context) {
        return getPreferences(context).getInt(PREF_STANDARD_APPS, 0);
    }

    public static void setStandardApps(Context context, int apps) {
        getPreferencesEditor(context)
                .putInt(PREF_STANDARD_APPS, apps)
                .commit();
    }

    public static String getDescription(Context context) {
        return getPreferences(context).getString(PREF_DESCRIPTION, context.getString(R.string.app_name));
    }

    public static void setDescription(Context context, String path) {
        getPreferencesEditor(context)
                .putString(PREF_DESCRIPTION, path)
                .commit();
    }

    public static void setTokenExpiry(Context context, long timeInMillis) {
        getPreferencesEditor(context)
                .putLong(PREF_TOKEN_EXPIRY , timeInMillis)
                .commit();
    }

    public static long getPrefTokenExpiry(Context context) {
        return getPreferences(context).getLong(PREF_TOKEN_EXPIRY, 0);
    }

    public static void setFCMToken(Context context,  String token) {
        getPreferencesEditor(context)
                .putString(PREF_FCM_TOKEN , token)
                .commit();
    }
    public static String getFCMToken(Context context){
        return getPreferences(context).getString(PREF_FCM_TOKEN, "");
    }

    public static Boolean getUserActivated(Context context){
        return getPreferences(context).getBoolean(PREF_CLIENT_ACTIVATED, true);
    }

    public static void setUserActivated(Context context, Boolean activated){
        getPreferencesEditor(context)
                .putBoolean(PREF_CLIENT_ACTIVATED, activated)
                .commit();
    }

    public static void setClientEmail(Context context, String mEmail) {
        getPreferencesEditor(context)
                .putString(PREF_CLIENT_EMAIL, mEmail)
                .commit();
        setUserActivated(context, true);
    }
    public static String getClientEmail(Context context){
        return getPreferences(context).getString(PREF_CLIENT_EMAIL, "");
    }

    public static void deletePasscodeForAppId(Context context,String appid, Boolean sharedPin){
        if (sharedPin)
            appid = LauncherPreference.PREF_SHARED_APP;

        Gson gson = new Gson();
        ArrayList<PasscodeModel> pINModels = null;
        String pINModelsJSON  = getPreferences(context).getString(PREF_HASH_PASSCODE, "");
        if (pINModelsJSON!=null && !pINModelsJSON.isEmpty() && !pINModelsJSON.equals("null")){
            Type listType = new TypeToken<ArrayList<PasscodeModel>>(){}.getType();
            pINModels = gson.fromJson(pINModelsJSON.toString(),listType);
            PasscodeModel itemForRemove = null;
            for (PasscodeModel it:pINModels) {
                if(it.appId != null && it.appId.equals(appid)){
                    itemForRemove = it;
                }
            }
            if(itemForRemove!=null){
                pINModels.remove(itemForRemove);
            }
        }
        getPreferencesEditor(context).putString(PREF_HASH_PASSCODE, gson.toJson(pINModels)).apply();
    }


    public static void setLastLoginForAppId(Context context,String appid, Boolean sharedPin){
        if (sharedPin) {
            appid = LauncherPreference.PREF_SHARED_APP;
        }

        Gson gson = new Gson();
        ArrayList<PasscodeModel> pINModels = null;
        String pINModelsJSON  = getPreferences(context).getString(PREF_HASH_PASSCODE, "");
        if (!pINModelsJSON.isEmpty()){
            Type listType = new TypeToken<ArrayList<PasscodeModel>>(){}.getType();
            pINModels = gson.fromJson(pINModelsJSON.toString(),listType);
            if (pINModels != null) {
                for (PasscodeModel it:pINModels) {
                    if(it.appId != null && it.appId.equals(appid)){
                        it.time = System.currentTimeMillis();
                    }
                }
            }
        }
        getPreferencesEditor(context).putString(PREF_HASH_PASSCODE, gson.toJson(pINModels)).apply();
    }


    public static Long getLastPasscodeForAppId(Context context,String appid, Boolean sharedPin){
        if (sharedPin) {
            appid = LauncherPreference.PREF_SHARED_APP;
        }

        Gson gson = new Gson();
        ArrayList<PasscodeModel> pINModels = null;
        String pINModelsJSON  = getPreferences(context).getString(PREF_HASH_PASSCODE, "");
        if (pINModelsJSON!=null && !pINModelsJSON.isEmpty() && !pINModelsJSON.equals("null")){
            Type listType = new TypeToken<ArrayList<PasscodeModel>>(){}.getType();
            pINModels = gson.fromJson(pINModelsJSON.toString(),listType);
            for (PasscodeModel it:pINModels) {
                if(it.appId != null && it.appId.equals(appid)){
                    return it.time;
                }
            }
        }
        return 0L;
    }


    public static PasscodeModel getPasscodeModel(Context context, String appid, Boolean sharedPin) {
        if (sharedPin) {
            appid = LauncherPreference.PREF_SHARED_APP;
        }

        Gson gson = new Gson();
        String pINModelsJSON  = getPreferences(context).getString(PREF_HASH_PASSCODE, "");
        if (!pINModelsJSON.isEmpty()){
            Type listType = new TypeToken<ArrayList<PasscodeModel>>(){}.getType();
            List<PasscodeModel> pINModels = gson.fromJson(pINModelsJSON.toString(),listType);
            if (pINModels == null) {
                return null;
            }

            for (PasscodeModel it : pINModels) {
                if(it.appId != null && it.appId.equals(appid)){
                    return it;
                }
            }
        }

        return null;
    }

    public static PasscodeModel getHashPasscodeandTryPasscodeForAppId(Context context, String appid, String pinHash, Boolean sharedPin){
        if (sharedPin) {
            appid = LauncherPreference.PREF_SHARED_APP;
        }

        Gson gson = new Gson();
        ArrayList<PasscodeModel> pINModels = null;
        String pINModelsJSON  = getPreferences(context).getString(PREF_HASH_PASSCODE, "");
        if (!pINModelsJSON.isEmpty()){
            Type listType = new TypeToken<ArrayList<PasscodeModel>>(){}.getType();
            pINModels = gson.fromJson(pINModelsJSON.toString(),listType);
            if (pINModels == null) {
                return null;
            }
            for (PasscodeModel it : pINModels) {
                if(it.appId != null && it.appId.equals(appid)){
                    if(it.tryTime ==null || it.tryTime >= System.currentTimeMillis() - PasscodeModel.TRY_TIME_PASSCODE_EXPIRATION_MIN*60*1000)
                        it.tryPasscode++;
                    else
                        it.tryPasscode = 0;
                    if(pinHash.isEmpty())
                        it.tryPasscode = 0;
                    if(pinHash.equals(it.hash) && (it.tryTime <= System.currentTimeMillis() - PasscodeModel.TRY_TIME_PASSCODE_EXPIRATION_MIN*60*1000 || it.tryPasscode<=3))
                        it.tryPasscode = 0;
                    it.tryTime = System.currentTimeMillis();
                    getPreferencesEditor(context).putString(PREF_HASH_PASSCODE, gson.toJson(pINModels)).apply();
                    return it;
                }
            }
        }
        return null;
    }


    public static void addHASHPasscode(Context context, String appid, String hashPasscode, Boolean sharedPin,Integer tryPIN) {
        if (sharedPin) {
            appid = LauncherPreference.PREF_SHARED_APP;
        }

        PasscodeModel pinModel = new PasscodeModel(appid, hashPasscode, System.currentTimeMillis(), tryPIN);
        ArrayList<PasscodeModel> pINModels = null;
        Gson gson = new Gson();

        String pINModelsJSON  = getPreferences(context).getString(PREF_HASH_PASSCODE, "");
        if (pINModelsJSON== "" || pINModelsJSON.equals("null")){
            pINModels = new ArrayList<PasscodeModel>();
            pINModels.add(pinModel);
        }
        else{
            Type listType = new TypeToken<ArrayList<PasscodeModel>>(){}.getType();
            pINModels = gson.fromJson(pINModelsJSON.toString(),listType);
            boolean needNew = true;
            for (PasscodeModel it:pINModels) {
                if(it.appId != null && it.appId.equals(appid)){
                    it.hash = hashPasscode;
                    needNew = false;
                }
            }
            if(needNew)
                pINModels.add(pinModel);
        }
        getPreferencesEditor(context).putString(PREF_HASH_PASSCODE, gson.toJson(pINModels)).apply();
    }

    public static ArrayList<String> getShortcutAppNameList(Context context){

        ArrayList<String> appNameList;
        Type type = new TypeToken<ArrayList<String>>(){}.getType();
        SharedPreferences prefs = context.getSharedPreferences(PREF_SHORTCUT_NAME_LIST, 0);
        Gson gson = new Gson();
        String getAppName = prefs.getString(PREF_SHORTCUT_NAME_LIST, null);
        if(getAppName==null){
            appNameList = new ArrayList<>();
        }else {
            appNameList = gson.fromJson(getAppName, type);
        }
        return appNameList;
    }
    public static void setAppNameList(Context context, ArrayList<String> shortcutAppNameList){

        SharedPreferences prefs = context.getSharedPreferences(PREF_SHORTCUT_NAME_LIST, 0);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String appNameList = gson.toJson(shortcutAppNameList);
        editor.putString(PREF_SHORTCUT_NAME_LIST, appNameList);
        editor.apply();
    }
    public static ArrayList<String> getShortcutAppURLList(Context context){

        ArrayList<String> appURLList;
        Type type = new TypeToken<ArrayList<String>>(){}.getType();
        SharedPreferences prefs = context.getSharedPreferences(PREF_SHORTCUT_URL_LIST, 0);
        Gson gson = new Gson();
        String getAppUrl = prefs.getString(PREF_SHORTCUT_URL_LIST, null);
        if(getAppUrl==null){
            appURLList = new ArrayList<>();
        }else {
            appURLList = gson.fromJson(getAppUrl, type);
        }
        return appURLList;
    }

    public static void setAppURLList(Context context, ArrayList<String> shortcutAppURLList){
        SharedPreferences prefs = context.getSharedPreferences(PREF_SHORTCUT_URL_LIST, 0);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String appUrlList = gson.toJson(shortcutAppURLList);
        editor.putString(PREF_SHORTCUT_URL_LIST, appUrlList);
        editor.apply();
    }

    public static ArrayList<String> getIconPath(Context context){

        ArrayList<String> appNameList;
        Type type = new TypeToken<ArrayList<String>>(){}.getType();
        SharedPreferences prefs = context.getSharedPreferences(PREF_SHORTCUT_ICON_LIST, 0);
        Gson gson = new Gson();
        String getAppName = prefs.getString(PREF_SHORTCUT_ICON_LIST, null);
        if(getAppName==null){
            appNameList = new ArrayList<>();
        }else {
            appNameList = gson.fromJson(getAppName, type);
        }
        return appNameList;
    }
    public static void setIconPath(Context context, ArrayList<String> shortcutAppNameList){

        SharedPreferences prefs = context.getSharedPreferences(PREF_SHORTCUT_ICON_LIST, 0);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String appNameList = gson.toJson(shortcutAppNameList);
        editor.putString(PREF_SHORTCUT_ICON_LIST, appNameList);
        editor.apply();
    }


    public static void setLocked(Context context, boolean b) {
        getPreferencesEditor(context)
                .putBoolean(PREF_LOCKED, b)
                .commit();

    }

    public static boolean getLocked(Context context) {
        return getPreferences(context).getBoolean(PREF_LOCKED, false);
    }

    public static void setChangePasscode(Context context, boolean value) {
        getPreferencesEditor(context)
                .putBoolean(PREF_CHANGE_PASSCODE, value)
                .commit();

        //clear app data if user wants to reset pin
        if (value) {
            //Note: this is workaround. Tokens must not be deleted if relogin will be done by Azure
            if (!isAzureLogin(context)) {
                Utils.deleteAllTokens(context);
            }

            deletePasscodeForAppId(context, "", true);
            deletePasscodeForFingerprint(context);
        }
    }

    public static boolean isChangePasscode(Context context) {
        return getPreferences(context).getBoolean(PREF_CHANGE_PASSCODE, false);
    }

    public static void savePasscodeForFingerprint(Context context, String passcode) {
        if (FingerprintUtils.getFingerprintSensorState(context) == BiometricSensorState.READY) {
            String encoded = CryptoUtils.encode(passcode);

            getPreferencesEditor(context)
                    .putString(PREF_FINGERPRINT_PASSCODE, encoded)
                    .commit();
        }
    }

    public static void deletePasscodeForFingerprint(Context context) {
        getPreferencesEditor(context)
                .remove(PREF_FINGERPRINT_PASSCODE)
                .commit();
    }

    public static String getPasscodeForFingerprint(Context context) {
        return getPreferences(context).getString(PREF_FINGERPRINT_PASSCODE, null);
    }

    public static void setPinState(Context context, String state, long expiration) {
        getPreferencesEditor(context)
                .putString(PREF_PIN_STATE, state)
                .putLong(PREF_PIN_EXPIRATION_TIME, expiration)
                .commit();
    }

    public static void deletePinState(Context context) {
        getPreferencesEditor(context)
                .remove(PREF_PIN_STATE)
                .remove(PREF_PIN_EXPIRATION_TIME)
                .commit();
    }

    @Nullable
    public static String getPinState(Context context) {
        return getPreferences(context).getString(PREF_PIN_STATE, null);
    }

    public static long getPinExpirationTime(Context context) {
        return getPreferences(context).getLong(PREF_PIN_EXPIRATION_TIME, 0L);
    }

    public static boolean isAutoreg(Context context) {
        return getPreferences(context).getBoolean(PREF_AUTO_REG, false);
    }

    public static void setRegistrationType(Context context, RegistrationType regType) {
        getPreferencesEditor(context)
                .putBoolean(PREF_AUTO_REG, regType == RegistrationType.AUTO)
                .putBoolean(PREF_AZURE_REG, regType == RegistrationType.AZURE)
                .commit();
    }

    public static long getUpgradePostponedTo(Context context) {
        return getPreferences(context).getLong(PREF_UPGRADE_POSTPONED_TO, 0);
    }

    public static void setUpgradePostponedTo(Context context, long value) {
        getPreferencesEditor(context)
                .putLong(PREF_UPGRADE_POSTPONED_TO, value)
                .commit();
    }

    public static int getUrlType(Context context) {
        if(BuildConfig.FLAVOR.equals(Utils.FLAVOR_NAME_DMAC_TIP) || BuildConfig.FLAVOR.equals(Utils.FLAVOR_NAME_TIP_EXPRESS)){
            return getPreferences(context).getInt(PREF_URL_TYPE, ApiFactory.URL_TYPE_PROD_TIP);
        } else if (BuildConfig.BUILD_TYPE.equals(Constants.BuildType.UAT)) {
            return getPreferences(context).getInt(PREF_URL_TYPE, ApiFactory.URL_TYPE_UAT);
        }else {
            return getPreferences(context).getInt(PREF_URL_TYPE, ApiFactory.URL_TYPE_TEST);
        }
    }

    public static void setUrlType(Context context, int value) {
        getPreferencesEditor(context)
                .putInt(PREF_URL_TYPE, value)
                .commit();
    }

    public static String getCustomUrl(Context context) {
        return getPreferences(context).getString(PREF_CUSTOM_URL, "");
    }

    public static void setCustomUrl(Context context, String value) {
        getPreferencesEditor(context)
                .putString(PREF_CUSTOM_URL, value)
                .commit();
    }

    public static String getCustomApiKey(Context context) {
        return getPreferences(context).getString(PREF_CUSTOM_API_KEY, "");
    }

    public static void setCustomApiKey(Context context, String value) {
        getPreferencesEditor(context)
                .putString(PREF_CUSTOM_API_KEY, value)
                .commit();
    }

    public static String getCustomScope(Context context) {
        return getPreferences(context).getString(PREF_CUSTOM_SCOPE, "");
    }

    public static void setCustomScope(Context context, String value) {
        getPreferencesEditor(context)
                .putString(PREF_CUSTOM_SCOPE, value)
                .commit();
    }

    public static boolean isLauncherSwitchedManually(Context context) {
        return getPreferences(context).getBoolean(PREF_LAUNCHER_SWITCHED_MANUALLY, false);
    }

    public static void setLauncherSwitchedManually(Context context, boolean value) {
        getPreferencesEditor(context)
                .putBoolean(PREF_LAUNCHER_SWITCHED_MANUALLY, value)
                .commit();
    }

    public static String getSnManual(Context context) {
        return getPreferences(context).getString(PREF_SN_MANUAL, "");
    }

    public static void setSnManual(Context context, String value) {
        getPreferencesEditor(context)
                .putString(PREF_SN_MANUAL, value)
                .commit();
    }

    public static String getImeiManual(Context context) {
        return getPreferences(context).getString(PREF_IMEI_MANUAL, "");
    }

    public static void setImeiManual(Context context, String value) {
        getPreferencesEditor(context)
                .putString(PREF_IMEI_MANUAL, value)
                .commit();
    }

    public static void saveReportedDeviceInfo(Context context, DeviceInfo deviceInfo) {
        String jsonString = deviceInfo.toJsonString();

        getPreferencesEditor(context)
                .putString(PREF_REPORTED_DEVICE_INFO, jsonString)
                .commit();

    }

    public static DeviceInfo loadReportedDeviceInfo(Context context) {
        String jsonString = getPreferences(context).getString(PREF_REPORTED_DEVICE_INFO, "");

        if (!TextUtils.isEmpty(jsonString)) {
            Gson gson = new Gson();
            return gson.fromJson(jsonString, DeviceInfo.class);
        } else {
            return null;
        }
    }

    public static DeviceOwner loadDeviceOwner(Context context) {
        String ownerString = getPreferences(context).getString(PREF_DEVICE_OWNER, DeviceOwner.CORPORATE.getValue());

        return DeviceOwner.from(ownerString);
    }

    public static void saveDeviceOwner(Context context, DeviceOwner deviceOwner) {
        getPreferencesEditor(context)
                .putString(PREF_DEVICE_OWNER, deviceOwner.getValue())
                .commit();
    }

    public static SimOwner loadSimOwner(Context context) {
        String ownerString = getPreferences(context).getString(PREF_SIM_OWNER, SimOwner.CORPORATE.getValue());

        return SimOwner.from(ownerString);
    }

    public static void saveSimOwner(Context context, SimOwner simOwner) {
        getPreferencesEditor(context)
                .putString(PREF_SIM_OWNER, simOwner.getValue())
                .commit();
    }

    public static boolean isAzureLogin(Context context) {
        return getPreferences(context).getBoolean(PREF_AZURE_REG, false);
    }

    public static Set<AppCategory> loadAppCategoryFilter(Context context) {
        Set<String> data = getPreferences(context).getStringSet(PREF_APP_CATEGORY_FILTER, null);

        if (data != null) {
            Set<AppCategory> result = new HashSet<>();

            for (String rawCategory : data) {
                AppCategory category = AppCategory.from(rawCategory);
                if (category != null) {
                    result.add(category);
                }
            }

            return result;
        } else {
            return DEFAULT_APP_CATEGORY_FILTER;
        }
    }

    public static void saveAppCategoryFilter(Context context,Set<AppCategory> appCategories) {
        Set<String> data = new HashSet<>();
        for (AppCategory category : appCategories) {
            data.add(category.getValue());
        }

        getPreferencesEditor(context)
                .putStringSet(PREF_APP_CATEGORY_FILTER, data)
                .commit();
    }

    public static long getLatestApplistRefreshTime(Context context){
        return getPreferences(context)
                .getLong(PREF_LATEST_APPLIST_REFRESH_TIME, 0);
    }
    public static void setLatestApplistRefreshTime(Context context,long refreshedTime){
        getPreferencesEditor(context)
                .putLong(PREF_LATEST_APPLIST_REFRESH_TIME, refreshedTime)
                .commit();
    }

    public static Boolean getEditApp(Context context){
        return getPreferences(context).getBoolean(PREF_EDIT_SHORTCUT_APP_DETAILS, false);
    }

    public static void setEditApp(Context context, Boolean activated){
        getPreferencesEditor(context)
                .putBoolean(PREF_EDIT_SHORTCUT_APP_DETAILS, activated)
                .commit();
    }

    public static Boolean getIsChangeIcon(Context context){
        return getPreferences(context).getBoolean(PREF_CHANGE_SHORTCUT_APP_ICON, false);
    }

    public static void setIsChangeIcon(Context context, Boolean changeIcon){
        getPreferencesEditor(context)
                .putBoolean(PREF_CHANGE_SHORTCUT_APP_ICON, changeIcon)
                .commit();
    }

    public static String getTempName(Context context){
        return getPreferences(context).getString(PREF_TEMP_SHORTCUT_NAME, "");
    }

    public static void setTempName(Context context, String tempName){
        getPreferencesEditor(context)
                .putString(PREF_TEMP_SHORTCUT_NAME, tempName)
                .commit();
    }

    public static String getTempUrl(Context context){
        return getPreferences(context).getString(PREF_TEMP_SHORTCUT_URL, "");
    }

    public static void setTempUrl(Context context, String tempUrl){
        getPreferencesEditor(context)
                .putString(PREF_TEMP_SHORTCUT_URL, tempUrl)
                .commit();
    }

    public static String getCurrentIcon(Context context){
        return getPreferences(context).getString(PREF_CURRENT_ICON, "");
    }

    public static void setCurrentIcon(Context context, String currentIcon){
        getPreferencesEditor(context)
                .putString(PREF_CURRENT_ICON, currentIcon)
                .commit();
    }

    public static Boolean getIsNewRegistration(Context context){
        return getPreferences(context).getBoolean(PREF_NEW_REGISTRATION, false);
    }

    public static void setIsNewRegistration(Context context, Boolean changeIcon){
        getPreferencesEditor(context)
                .putBoolean(PREF_NEW_REGISTRATION, changeIcon)
                .commit();
    }

    public static String getUniqueId(Context context){
        return getPreferences(context).getString(PREF_UNIQUE_ID, "");
    }

    public static void setUniqueId(Context context, String uniqueId){
        getPreferencesEditor(context)
                .putString(PREF_UNIQUE_ID, uniqueId)
                .commit();
    }

    public static void setDMACPackageIds(Context context, ArrayList<String> appsPackageArray){
        SharedPreferences prefs = context.getSharedPreferences(PREF_APP_PACKAGE_LIST, 0);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String appsPackageString = gson.toJson(appsPackageArray);
        editor.putString(PREF_APP_PACKAGE_LIST, appsPackageString);
        editor.apply();
    }

    public static ArrayList<String> getDMACPackageIds(Context context){

        ArrayList<String> appPackageArray;
        Type type = new TypeToken<ArrayList<String>>(){}.getType();
        SharedPreferences prefs = context.getSharedPreferences(PREF_APP_PACKAGE_LIST, 0);
        Gson gson = new Gson();
        String getPackageString = prefs.getString(PREF_APP_PACKAGE_LIST, null);
        if(getPackageString==null){
            appPackageArray = new ArrayList<>();
        }else {
            appPackageArray = gson.fromJson(getPackageString, type);
        }
        return appPackageArray;
    }

    public static void setDMACAppNames(Context context, ArrayList<String> appsNameArray){
        SharedPreferences prefs = context.getSharedPreferences(PREF_APP_NAME_LIST, 0);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String appsNameString = gson.toJson(appsNameArray);
        editor.putString(PREF_APP_NAME_LIST, appsNameString);
        editor.apply();
    }

    public static ArrayList<String> getDMACAppNames(Context context){

        ArrayList<String> appNameArray;
        Type type = new TypeToken<ArrayList<String>>(){}.getType();
        SharedPreferences prefs = context.getSharedPreferences(PREF_APP_NAME_LIST, 0);
        Gson gson = new Gson();
        String getNameString = prefs.getString(PREF_APP_NAME_LIST, null);
        if(getNameString==null){
            appNameArray = new ArrayList<>();
        }else {
            appNameArray = gson.fromJson(getNameString, type);
        }
        return appNameArray;
    }

    public static void setDMACAppMarketplaceIcon(Context context, ArrayList<String> appsMarketplaceIconArray){
        SharedPreferences prefs = context.getSharedPreferences(PREF_MARKETPLACE_ICON_LIST, 0);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String appsMarketplaceIconString = gson.toJson(appsMarketplaceIconArray);
        editor.putString(PREF_MARKETPLACE_ICON_LIST, appsMarketplaceIconString);
        editor.apply();
    }

    public static ArrayList<String> getDMACAppMarketplaceIcon(Context context){

        ArrayList<String> appMarketplaceIconArray;
        Type type = new TypeToken<ArrayList<String>>(){}.getType();
        SharedPreferences prefs = context.getSharedPreferences(PREF_MARKETPLACE_ICON_LIST, 0);
        Gson gson = new Gson();
        String getMarketplaceIconString = prefs.getString(PREF_MARKETPLACE_ICON_LIST, null);
        if(getMarketplaceIconString==null){
            appMarketplaceIconArray = new ArrayList<>();
        }else {
            appMarketplaceIconArray = gson.fromJson(getMarketplaceIconString, type);
        }
        return appMarketplaceIconArray;
    }

}
