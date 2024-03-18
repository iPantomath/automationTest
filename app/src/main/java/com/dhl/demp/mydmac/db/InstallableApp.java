package com.dhl.demp.mydmac.db;

import android.os.Parcel;
import android.os.Parcelable;

//import com.activeandroid.Model;
//import com.activeandroid.annotation.Column;
//import com.activeandroid.annotation.Table;
//import com.activeandroid.query.Delete;
//import com.activeandroid.query.Select;

import java.util.List;

//@Table(name = "app_installable", id = BaseColumns._ID)
public class InstallableApp
         implements Parcelable
{

//    @Column(name = "id")
    public String id;

//    @Column(name = "name")
    public String name;

//    @Column(name = "icon")
    public String icon;

//    @Column(name = "type")
    public Type type = Type.DEPENDENCY_REQUIRED;

//    @Column(name = "version")
    public String version;
    public List<String> thumbnails;

    protected InstallableApp(Parcel in) {
        id = in.readString();
        name = in.readString();
        icon = in.readString();
        version = in.readString();
        thumbnails = in.createStringArrayList();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(icon);
        dest.writeString(version);
        dest.writeStringList(thumbnails);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<InstallableApp> CREATOR = new Creator<InstallableApp>() {
        @Override
        public InstallableApp createFromParcel(Parcel in) {
            return new InstallableApp(in);
        }

        @Override
        public InstallableApp[] newArray(int size) {
            return new InstallableApp[size];
        }
    };

//    public static int getUpdatableCount() {
//        return new Select()
//                .from(InstallableApp.class)
//                .where("type = ?", Type.UPDATABLE.name())
//                .count();
//    }

//    public static void deleteAll() {
//        new Delete()
//                .from(InstallableApp.class)
//                .execute();
//    }

    public enum Type {
        DEPENDENCY_REQUIRED,
        REQUIRED,
        INSTALLABLE,
        UPDATABLE,
        INSTALLED,
        HIDDEN
    }

    public InstallableApp() {
    }

}
