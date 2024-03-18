package com.dhl.demp.mydmac.api.request;

import com.dhl.demp.mydmac.obj.DeviceInfo;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RefreshTokenRequest extends UnifiedRequest<RefreshTokenRequest.Data> {
    public RefreshTokenRequest(String refreshToken, String scope, DeviceInfo deviceInfo) {
        super("registration", "device_refreshtoken", new Data("refresh_token", scope, refreshToken, deviceInfo));
    }

    static class Data {
        @SerializedName("grant_type")
        @Expose
        public final String grantType;
        @SerializedName("scope")
        @Expose
        public final String scope;
        @SerializedName("refresh_token")
        @Expose
        public final String refreshToken;
        @SerializedName("device_info")
        @Expose
        public final DeviceInfo deviceInfo;

        public Data(String grantType, String scope, String refreshToken, DeviceInfo deviceInfo) {
            this.grantType = grantType;
            this.scope = scope;
            this.refreshToken = refreshToken;
            this.deviceInfo = deviceInfo;
        }
    }
}
