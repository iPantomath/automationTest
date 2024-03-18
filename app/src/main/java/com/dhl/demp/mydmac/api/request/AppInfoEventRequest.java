package com.dhl.demp.mydmac.api.request;

import android.os.Build;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import mydmac.BuildConfig;

public class AppInfoEventRequest extends AnalyticsEventRequest {
    @SerializedName("app_version")
    @Expose
    public final String appVersion;
    @SerializedName("os")
    @Expose
    public final String os;
    @SerializedName("os_vesrion")
    @Expose
    public final String osVesrion;
    @SerializedName("app_id")
    @Expose
    public final String appId;

    public AppInfoEventRequest() {
        super("app_info");

        appVersion = BuildConfig.VERSION_NAME;
        os = "Android";
        osVesrion = Build.VERSION.RELEASE;
        appId = BuildConfig.DMAC_APP_ID;
    }
}
