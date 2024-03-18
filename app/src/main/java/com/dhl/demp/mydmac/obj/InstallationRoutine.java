package com.dhl.demp.mydmac.obj;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by robielok on 10/10/2017.
 */

public class InstallationRoutine {
    public static final String EXTERNAL_LINK = "external_link";
    public static final String APKINSTALL = "apkinstall";

    @SerializedName("type")
    public String type;
    @SerializedName("source")
    public String source;
    @SerializedName("size")
    public long size;
    @SerializedName("md5")
    public String md5;
    @SerializedName("dependency")
    public List<InstallationDependency> dependencies;

    public InstallationDependency installationDependency() {
        return dependencies.get(0);
    }

    public boolean isWebLinkType() {
        return EXTERNAL_LINK.equalsIgnoreCase(type);
    }

    public boolean isApkInstallType() {
        return APKINSTALL.equalsIgnoreCase(type);
    }
}