package com.dhl.demp.mydmac.obj;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AppCategory {
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("tags")
    @Expose
    public List<String> tags;
}
