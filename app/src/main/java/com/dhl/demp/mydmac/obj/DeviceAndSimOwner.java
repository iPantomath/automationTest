package com.dhl.demp.mydmac.obj;

import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DeviceAndSimOwner {
    @SerializedName("device_owner_type")
    @Expose
    public String  deviceOwnerType;
    @SerializedName("sim_card_ownership")
    @Expose
    public String  simCardOwnership;

    public DeviceAndSimOwner(@Nullable String deviceOwnerType, @Nullable String simCardOwnership) {
        this.deviceOwnerType = deviceOwnerType;
        this.simCardOwnership = simCardOwnership;
    }
}
