package com.dhl.demp.mydmac.api.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AppDownloadEventRequest extends AnalyticsEventRequest {
    @SerializedName("app_version")
    @Expose
    public final String targetAppVersion;
    @SerializedName("app_id")
    @Expose
    public final String targetAppId;

    public AppDownloadEventRequest(String targetAppVersion, String targetAppId) {
        super("app_download");
        this.targetAppVersion = targetAppVersion;
        this.targetAppId = targetAppId;
    }
}
