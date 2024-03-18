package com.dhl.demp.mydmac.obj;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by rd on 11/05/16.
 */
public class Apka {
    @SerializedName("annotation")
    @Expose
    public String annotation;
    @SerializedName("package_id")
    @Expose
    public String packageId;
    @SerializedName("app_id")
    @Expose
    public String appID;
    @SerializedName("app_name")
    @Expose
    public String appName;
    @SerializedName("release_type")
    @Expose
    public String releaseType;
    @SerializedName("release_note")
    @Expose
    public String releaseNote;
    @SerializedName("app_version")
    @Expose
    public String appVersion;
    @SerializedName("app_version_date_ms")
    @Expose
    public long appVersionDate;
    @SerializedName("available")
    @Expose
    public boolean available;
    @SerializedName("launcher")
    @Expose
    public boolean launcher;
    @SerializedName("installation_routine")
    @Expose
    public List<InstallationRoutine> installRoutine;
    @SerializedName("marketplace_icon")
    @Expose
    public String marketplaceIcon;
    @SerializedName("rating")
    @Expose
    public int rating;
    @SerializedName("rating_count")
    @Expose
    public int rating_count;
    @SerializedName("required")
    @Expose
    public boolean required;
    @SerializedName("start_after_install")
    @Expose
    public boolean startAfterInstall;
    @SerializedName("website")
    @Expose
    public String website;
    @SerializedName("wifi_install_only")
    @Expose
    public boolean wifiInstallOnly;
    @SerializedName("hidden")
    @Expose
    public boolean hidden;
    @SerializedName("division")
    @Expose
    public String division;
    @SerializedName("category")
    @Expose
    public List<AppCategory> categories;

    @SerializedName("bbfied")
    @Expose
    public boolean bbFied; //app can be installed only after approve
    @SerializedName("state")
    @Expose
    public String approveState;

    @SerializedName("use_case")
    @Expose
    public String useCase;

    @SerializedName("actions")
    @Expose
    public Action[] actions;

    public static class Action {
        @SerializedName("url")
        @Expose
        public String url;
        @SerializedName("name")
        @Expose
        public String name;
        @SerializedName("message")
        @Expose
        public String message;
        @SerializedName("target_package_id")
        @Expose
        public String targetPackageId;
    }

    public InstallationRoutine getInstallationRoutine() {
        return installRoutine.get(0);
    }
}
