package com.dhl.demp.mydmac.obj;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by robielok on 11/6/2017.
 */

public class ApproveRequestData {
    @SerializedName("requestId")
    @Expose
    public final String requestId;

    @SerializedName("parameters")
    @Expose
    public final AppIdWrapper parameters;

    public ApproveRequestData(String appId, String requestId) {
        this.requestId = requestId;
        this.parameters = new AppIdWrapper(appId);
    }

    private static class AppIdWrapper {
        @SerializedName("appId")
        @Expose
        public final String appId;

        public AppIdWrapper(String appId) {
            this.appId = appId;
        }
    }
}
