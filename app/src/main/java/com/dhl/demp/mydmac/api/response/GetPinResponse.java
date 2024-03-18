package com.dhl.demp.mydmac.api.response;

import com.dhl.demp.mydmac.api.listener.Statefull;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetPinResponse implements Statefull {
    private static final String STATUS_PIN_SENT = "PIN_sent";

    @SerializedName("status")
    @Expose
    public String status;
    @SerializedName("state")
    @Expose
    public int state;
    @SerializedName("expires_in")
    @Expose
    public int expiresIn;

    public boolean isPinSent() {
        return STATUS_PIN_SENT.equalsIgnoreCase(status);
    }

    @Override
    public int getState() {
        return state;
    }

    @Override
    public boolean checkState() {
        return true;
    }
}