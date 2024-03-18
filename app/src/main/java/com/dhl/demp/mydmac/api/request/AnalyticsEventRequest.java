package com.dhl.demp.mydmac.api.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public abstract class AnalyticsEventRequest {
    @SerializedName("event")
    @Expose
    public final String eventName;

    public AnalyticsEventRequest(String eventName) {
        this.eventName = eventName;
    }
}
