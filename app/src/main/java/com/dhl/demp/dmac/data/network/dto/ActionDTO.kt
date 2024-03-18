package com.dhl.demp.dmac.data.network.dto

import com.google.gson.annotations.SerializedName

class ActionDTO(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("target_package_id")
    val targetPackageId: String,
    @SerializedName("message")
    val message: String?
)