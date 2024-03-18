package com.dhl.demp.mydmac.api.response;

import com.dhl.demp.mydmac.api.listener.Statefull;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RegistrationResponse implements Statefull {
    private static final String TYPE_PASSWORD = "password";
    private static final String TYPE_AAD = "aad";

    @SerializedName("type")
    @Expose
    public String type;
    @SerializedName("status")
    @Expose
    public String status;
    @SerializedName("state")
    @Expose
    public int state;
    @SerializedName("expires_in")
    @Expose
    public int expiresIn;
    @SerializedName("auth_types_sequence")
    @Expose
    public String[] authTypesSequence;

    @Override
    public int getState() {
        return state;
    }

    public boolean isPasswordLogin() {
        return TYPE_PASSWORD.equalsIgnoreCase(type);
    }

    public boolean isAzureLogin() {
        return TYPE_AAD.equalsIgnoreCase(type);
    }

    @Override
    public boolean checkState() {
        return !isAzureLogin();
    }

    public boolean passwordLoginAllowed() {
        for (String tp: authTypesSequence) {
            if (TYPE_PASSWORD.equalsIgnoreCase(tp)) {
                return true;
            }
        }

        return false;
    }
}
