package com.dhl.demp.mydmac.api.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

public class NotificationResponse {
    @SerializedName("notifications")
    @Expose
    public List<NotificationItem> notifications;
    @SerializedName("limit")
    @Expose
    public int limit;
    @SerializedName("offset")
    @Expose
    public int offset;
    @SerializedName("total")
    @Expose
    public int total;

    public static class NotificationItem {
        @SerializedName("f_StatusTimestamp")
        @Expose
        public Date date;
        @SerializedName("f_Title")
        @Expose
        public String title;
        @SerializedName("f_Message")
        @Expose
        public String message;
    }
}

