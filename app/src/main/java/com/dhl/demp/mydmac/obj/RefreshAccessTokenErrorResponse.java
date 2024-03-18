package com.dhl.demp.mydmac.obj;

/**
 * Created by robielok on 10/17/2017.
 */

public class RefreshAccessTokenErrorResponse extends ErrorResponse {
    private static final String ERROR_AUTHORIZATION_REQUIRED = "authorization_required";

    public boolean needLogoutUser() {
        return ERROR_AUTHORIZATION_REQUIRED.equalsIgnoreCase(error);
    }
}
