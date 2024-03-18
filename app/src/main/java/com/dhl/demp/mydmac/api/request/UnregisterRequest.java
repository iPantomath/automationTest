package com.dhl.demp.mydmac.api.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UnregisterRequest extends UnifiedRequest<UnregisterRequest.Data> {
    public UnregisterRequest(String clientId, String refreshToken) {
        super("registration", "device_unregister", new Data(clientId, refreshToken));
    }

    static class Data {
        @SerializedName("client_id")
        @Expose
        public final String clientId;
        @SerializedName("refresh_token")
        @Expose
        public final String refreshToken;


        public Data(String clientId, String refreshToken) {
            this.clientId = clientId;
            this.refreshToken = refreshToken;
        }
    }
}
