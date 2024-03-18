package com.dhl.demp.mydmac.api.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DmacApiEventRequest extends AnalyticsEventRequest {
    @SerializedName("function")
    @Expose
    public final String function;
    @SerializedName("app_id")
    @Expose
    public final String targetAppId;

    public DmacApiEventRequest(String function, String targetAppId) {
        super("dmac_api");
        this.function = function;
        this.targetAppId = targetAppId;
    }
}