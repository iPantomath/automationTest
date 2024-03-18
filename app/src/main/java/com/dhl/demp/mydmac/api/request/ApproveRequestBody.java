package com.dhl.demp.mydmac.api.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ApproveRequestBody {
    @SerializedName("user")
    @Expose
    public final String email;
    @SerializedName("appId")
    @Expose
    public final String appId;
    @SerializedName("requestId")
    @Expose
    public final String requestId;
    @SerializedName("deviceId")
    @Expose
    public final String clientId;

    public ApproveRequestBody(String email, String appId, String requestId, String clientId) {
        this.email = email;
        this.appId = appId;
        this.requestId = requestId;
        this.clientId = clientId;
    }
}
