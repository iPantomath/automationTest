package com.dhl.demp.mydmac.api.response;

import com.dhl.demp.mydmac.obj.ErrorResponse;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RegistrationErrorResponse extends ErrorResponse {
    private static final String ERROR_INVALID_SCOPE = "invalid_scope";
    private static final String ERROR_MISSING_TOKEN = "missing_token";
    private static final String ERROR_INVALID_REQUEST = "invalid_request";
    private static final String ERROR_NOT_REGISTERED_DEVICE_OR_MAIL = "not_registered_device_or_mail";
    private static final String ERROR_DEACTIVATED_USER = "deactivated_user";
    private static final String ERROR_REJECTED_EMAIL = "rejected_email";
    private static final String ERROR_REQUEST_REJECTED = "request_rejected";
    private static final String ERROR_PIN_SENT = "pin_sent";
    private static final String ERROR_GENERAL_ERROR = "general_error";

    @SerializedName("uniqueId")
    @Expose
    public String uniqueId;

    public boolean isPinSentError() {
        return ERROR_PIN_SENT.equalsIgnoreCase(error);
    }

    public boolean isNotRegisteredError() {
        return ERROR_NOT_REGISTERED_DEVICE_OR_MAIL.equalsIgnoreCase(error);
    }

    public boolean isRejectedEmailError() {
        return ERROR_REJECTED_EMAIL.equalsIgnoreCase(error);
    }

    public boolean isDeactivatedUserError() {
        return ERROR_DEACTIVATED_USER.equalsIgnoreCase(error);
    }
}
