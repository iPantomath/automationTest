package com.dhl.demp.dmac.data.network.dto

import com.google.gson.annotations.SerializedName

class AppVersionDTO(
    @SerializedName("version")
    val version: String,
    @SerializedName("release_note")
    val releaseNote: String?,
)