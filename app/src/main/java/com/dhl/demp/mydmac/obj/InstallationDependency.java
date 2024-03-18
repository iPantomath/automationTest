package com.dhl.demp.mydmac.obj;

import static com.dhl.demp.mydmac.obj.InstallationRoutine.*;

import com.google.gson.annotations.SerializedName;

/**
 * Created by robielok on 10/11/2017.
 */

public class InstallationDependency {
    @SerializedName("app_id")
    public String appId;
    @SerializedName("package_id")
    public String packageId;
    @SerializedName("app_name")
    public String appName;
    @SerializedName("marketplace_icon")
    public String appIcon;
    @SerializedName("version")
    public String version;
    @SerializedName("type")
    public String type;
    @SerializedName("source")
    public String source;
    @SerializedName("size")
    public long size;
    @SerializedName("md5")
    public String md5;
    @SerializedName("release_type")
    public String releaseType;
    @SerializedName("install_before")
    public boolean installBefore;
    @SerializedName("available")
    public boolean available;

    public boolean isWebLinkType() {
        return EXTERNAL_LINK.equalsIgnoreCase(type);
    }

    public boolean isApkInstallType() {
        return APKINSTALL.equalsIgnoreCase(type);
    }
}