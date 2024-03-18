package com.dhl.demp.mydmac.obj;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by robielok on 11/10/2017.
 */
public class InstallationAppItem implements Parcelable {
    public String appId;
    public String appName;
    public String version;
    public String source;
    public String md5;

    public InstallationAppItem(String appId, String appName, String version, String source, String md5) {
        this.appId = appId;
        this.appName = appName;
        this.version = version;
        this.source = source;
        this.md5 = md5;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.appId);
        dest.writeString(this.appName);
        dest.writeString(this.version);
        dest.writeString(this.source);
        dest.writeString(this.md5);
    }

    protected InstallationAppItem(Parcel in) {
        this.appId = in.readString();
        this.appName = in.readString();
        this.version = in.readString();
        this.source = in.readString();
        this.md5 = in.readString();
    }

    public static final Creator<InstallationAppItem> CREATOR = new Creator<InstallationAppItem>() {
        @Override
        public InstallationAppItem createFromParcel(Parcel source) {
            return new InstallationAppItem(source);
        }

        @Override
        public InstallationAppItem[] newArray(int size) {
            return new InstallationAppItem[size];
        }
    };
}
