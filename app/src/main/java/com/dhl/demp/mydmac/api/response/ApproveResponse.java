package com.dhl.demp.mydmac.api.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ApproveResponse {
    @SerializedName("state")
    @Expose
    public String approveState;
    @SerializedName("message")
    @Expose
    public String message;
}
