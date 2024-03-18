package com.dhl.demp.dmac.data.network.dto

import com.google.gson.annotations.SerializedName

class ApproveRequestDTO(
    @SerializedName("user")
    val email: String,
    @SerializedName("appId")
    val appId: String,
    @SerializedName("requestId")
    val requestId: String,
    @SerializedName("deviceId")
    val clientId: String,
)