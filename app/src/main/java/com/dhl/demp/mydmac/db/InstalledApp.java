package com.dhl.demp.mydmac.db;

//import com.activeandroid.Model;
//import com.activeandroid.annotation.Column;
//import com.activeandroid.annotation.Table;
//import com.activeandroid.query.Delete;
//import com.activeandroid.query.Select;

//@Table(name = "app_installed", id = BaseColumns._ID)
public class InstalledApp {

//    @Column(name = "id")
    public String id;

//    @Column(name = "show_in_launcher")
    public boolean show_in_launcher;

//    public static boolean check(String name) {
//        return new Select()
//                .from(InstalledApp.class)
//                .where("id = ? AND show_in_launcher = ?", name, true)
//                .count() > 0;
//    }

//    public static void deleteAll() {
//        new Delete()
//                .from(InstalledApp.class)
//                .execute();
//    }
}
