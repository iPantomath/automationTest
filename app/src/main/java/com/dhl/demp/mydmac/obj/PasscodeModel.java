package com.dhl.demp.mydmac.obj;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by petlebed on 3/10/2017.
 */

public class PasscodeModel {

    public static Integer TRY_TIME_PASSCODE_EXPIRATION_MIN = 10;
    public static Integer PASSCODE_EXPIRATION_TIME_MIN = 15;

    @SerializedName("appId")
    @Expose
    public String appId;
    @SerializedName("hash")
    @Expose
    public String hash;
    @SerializedName("time")
    @Expose
    public Long time;
    @SerializedName("tryPasscode")
    @Expose
    public int tryPasscode;
    @SerializedName("tryTime")
    @Expose
    public Long tryTime;

    public PasscodeModel(String appId, String hash, Long time, Integer tryPasscode){
        this.appId = appId;
        this.hash = hash;
        this.time = time;
        this.tryPasscode = tryPasscode;
        this.tryTime = System.currentTimeMillis();
    }
}
