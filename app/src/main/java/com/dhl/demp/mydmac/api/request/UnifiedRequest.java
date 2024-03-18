package com.dhl.demp.mydmac.api.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public abstract class UnifiedRequest<T> {
    @SerializedName("service")
    @Expose
    public final String service;
    @SerializedName("method")
    @Expose
    public final String method;
    @SerializedName("data")
    @Expose
    public final T data;

    public UnifiedRequest(String service, String method, T data) {
        this.service = service;
        this.method = method;
        this.data = data;
    }
}
