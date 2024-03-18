package com.dhl.demp.mydmac.obj;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import com.dhl.demp.mydmac.LauncherPreference;
import com.dhl.demp.mydmac.utils.RootUtil;
import com.dhl.demp.mydmac.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Locale;
import java.util.TimeZone;

import mydmac.BuildConfig;
import mydmac.R;

/**
 * Created by jahofman on 3/7/2016.
 */
public class DeviceInfo {
    private static Gson gson = new Gson();

    @SerializedName("device_language")
    @Expose
    public String  deviceLanguage;
    @SerializedName("device_unique_id")
    @Expose
    public String  deviceUniqueId;
    @SerializedName("dmac_id")
    @Expose
    public String  dmacFlavorId;
    @SerializedName("dmac_version")
    @Expose
    public String  dmacVersion;
    @SerializedName("pushToken")
    @Expose
    public String  firebaseToken;

    @SerializedName("os_version")
    @Expose
    public String  osVersion;
    @SerializedName("rooted")
    @Expose
    public boolean rooted;
    @SerializedName("time_zone")
    @Expose
    public String  timeZone;
    @SerializedName("app_id")
    @Expose
    public String applicationID;
    @SerializedName("dev_model")
    @Expose
    public String devModel;
    @SerializedName("kernel")
    @Expose
    public String kernel;
    @SerializedName("manufacture")
    @Expose
    public String manufacture;
    @SerializedName("patch_level")
    @Expose
    public String patchLevel;

    public DeviceInfo(Context context) {
        deviceLanguage  = Locale.getDefault().getLanguage();
        deviceUniqueId = Utils.getDeviceUniqueId(context);
        if(LauncherPreference.getIsNewRegistration(context)){
            deviceUniqueId = BuildConfig.DEBUG ? LauncherPreference.getUniqueId(context): Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }else{
            if(BuildConfig.FLAVOR.equals("dmac_e")){
                deviceUniqueId = BuildConfig.DEBUG ? Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID) + "eDMAC" + "_debug" : Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            }else {
                deviceUniqueId = BuildConfig.DEBUG ? Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID) + context.getString(R.string.app_name).replaceAll(" ", "") + "_debug" : Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            }
        }
        dmacFlavorId = Utils.getDmacFlavorIdForDeviceInfo();
        applicationID = BuildConfig.APPLICATION_ID;
        dmacVersion = BuildConfig.VERSION_NAME;
        firebaseToken = LauncherPreference.getFCMToken(context);
        osVersion = "Android " + Build.VERSION.RELEASE;
        rooted = RootUtil.isDeviceRooted();
        timeZone = TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT, Locale.US);
        devModel = Build.MODEL;
        kernel = System.getProperty("os.version");
        manufacture = Build.MANUFACTURER;
        patchLevel = Utils.getSecurityPatchLevel();
    }

    public String toJsonString() {
        return gson.toJson(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeviceInfo that = (DeviceInfo) o;

        if (rooted != that.rooted) return false;
        if (deviceLanguage != null ? !deviceLanguage.equals(that.deviceLanguage) : that.deviceLanguage != null)
            return false;
        if (deviceUniqueId != null ? !deviceUniqueId.equals(that.deviceUniqueId) : that.deviceUniqueId != null)
            return false;
        if (dmacFlavorId != null ? !dmacFlavorId.equals(that.dmacFlavorId) : that.dmacFlavorId != null)
            return false;
        if (dmacVersion != null ? !dmacVersion.equals(that.dmacVersion) : that.dmacVersion != null)
            return false;
        if (firebaseToken != null ? !firebaseToken.equals(that.firebaseToken) : that.firebaseToken != null)
            return false;
        if (osVersion != null ? !osVersion.equals(that.osVersion) : that.osVersion != null)
            return false;
        if (timeZone != null ? !timeZone.equals(that.timeZone) : that.timeZone != null)
            return false;
        if (applicationID != null ? !applicationID.equals(that.applicationID) : that.applicationID != null)
            return false;
        if (devModel != null ? !devModel.equals(that.devModel) : that.devModel != null)
            return false;
        if (kernel != null ? !kernel.equals(that.kernel) : that.kernel != null) return false;
        if (manufacture != null ? !manufacture.equals(that.manufacture) : that.manufacture != null)
            return false;
        return patchLevel != null ? patchLevel.equals(that.patchLevel) : that.patchLevel == null;
    }

    @Override
    public int hashCode() {
        int result = deviceLanguage != null ? deviceLanguage.hashCode() : 0;
        result = 31 * result + (deviceUniqueId != null ? deviceUniqueId.hashCode() : 0);
        result = 31 * result + (dmacFlavorId != null ? dmacFlavorId.hashCode() : 0);
        result = 31 * result + (dmacVersion != null ? dmacVersion.hashCode() : 0);
        result = 31 * result + (firebaseToken != null ? firebaseToken.hashCode() : 0);
        result = 31 * result + (osVersion != null ? osVersion.hashCode() : 0);
        result = 31 * result + (rooted ? 1 : 0);
        result = 31 * result + (timeZone != null ? timeZone.hashCode() : 0);
        result = 31 * result + (applicationID != null ? applicationID.hashCode() : 0);
        result = 31 * result + (devModel != null ? devModel.hashCode() : 0);
        result = 31 * result + (kernel != null ? kernel.hashCode() : 0);
        result = 31 * result + (manufacture != null ? manufacture.hashCode() : 0);
        result = 31 * result + (patchLevel != null ? patchLevel.hashCode() : 0);
        return result;
    }
}
