package com.dhl.demp.mydmac.api.request;

import com.dhl.demp.mydmac.obj.DeviceInfo;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetTokenRequest extends UnifiedRequest<GetTokenRequest.Data> {
    public static final String GRANT_TYPE_AAD = "aad";
    public static final String GRANT_TYPE_PASSWORD = "password";

    public GetTokenRequest(String grantType, String username, String password, DeviceInfo deviceInfo) {
        super("registration", "device_gettoken", new Data(grantType, username, password, deviceInfo));
    }

    static class Data {
        @SerializedName("grant_type")
        @Expose
        public final String grantType;
        @SerializedName("username")
        @Expose
        public final String username;
        @SerializedName("password")
        @Expose
        public final String password;
        @SerializedName("device_info")
        @Expose
        public final DeviceInfo deviceInfo;

        public Data(String grantType, String username, String password, DeviceInfo deviceInfo) {
            this.grantType = grantType;
            this.username = username;
            this.password = password;
            this.deviceInfo = deviceInfo;
        }
    }
}
