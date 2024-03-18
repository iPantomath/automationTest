package com.dhl.demp.mydmac.api.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.concurrent.TimeUnit;

public class GetTokenResponse {
    @SerializedName("token_type")
    @Expose
    public String tokenType;
    @SerializedName("access_token")
    @Expose
    public String accessToken;
    @SerializedName("expires_in")
    @Expose
    public int expiresIn;
    @SerializedName("refresh_token")
    @Expose
    public String refreshToken;
    @SerializedName("client_id")
    @Expose
    public String clientId;
    @SerializedName("data_collection_period")
    @Expose
    private int dataCollectionPeriod;

    /**
     * Data collection period
     * @return Period in milliseconds
     */
    public long getDataCollectionPeriod() {
        return TimeUnit.SECONDS.toMillis(dataCollectionPeriod);
    }
}
