package com.dhl.demp.mydmac.obj;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by robielok on 10/17/2017.
 */

public class ErrorResponse {
    @SerializedName("error")
    @Expose
    public String error;

    @SerializedName("error_description")
    @Expose
    public String errorDescription;
}
