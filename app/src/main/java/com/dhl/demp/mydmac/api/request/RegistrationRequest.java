package com.dhl.demp.mydmac.api.request;

import com.dhl.demp.mydmac.obj.DeviceInfo;
import com.dhl.demp.mydmac.utils.Utils;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RegistrationRequest extends UnifiedRequest<RegistrationRequest.Data> {
    public RegistrationRequest(String login, int state, DeviceInfo deviceInfo) {
        super("registration", "device_registration", new Data(login, state, deviceInfo));
    }

    static class Data {
        @SerializedName("email")
        @Expose
        public final String email;
        @SerializedName("phone")
        @Expose
        public final String phone;
        @SerializedName("state")
        @Expose
        public final int state;
        @SerializedName("device_info")
        @Expose
        public final DeviceInfo deviceInfo;

        public Data(String login, int state, DeviceInfo deviceInfo) {
            if (Utils.isEmail(login)) {
                this.email = login;
                this.phone = null;
            } else {
                this.email = null;
                this.phone = login;
            }
            this.state = state;
            this.deviceInfo = deviceInfo;
        }
    }
}
