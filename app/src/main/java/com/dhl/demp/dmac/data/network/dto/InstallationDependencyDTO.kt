package com.dhl.demp.dmac.data.network.dto

import com.dhl.demp.dmac.model.InstallationRoutineType
import com.google.gson.annotations.SerializedName

class InstallationDependencyDTO(
    @SerializedName("app_id")
    val appId: String,
    @SerializedName("package_id")
    val packageId: String,
    @SerializedName("app_name")
    val appName: String,
    @SerializedName("marketplace_icon")
    val appIcon: String,
    @SerializedName("version")
    val version: String,
    @SerializedName("type")
    val type: InstallationRoutineType,
    @SerializedName("source")
    val source: String,
    @SerializedName("size")
    val size: Long,
    @SerializedName("md5")
    val md5: String,
    @SerializedName("release_type")
    val releaseType: String?,
    @SerializedName("install_before")
    val installBefore: Boolean,
    @SerializedName("available")
    val available: Boolean
)