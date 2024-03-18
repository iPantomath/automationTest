package com.dhl.demp.dmac.data.network.dto

import com.google.gson.annotations.SerializedName

class AppInfoListDTO(
    @SerializedName("applist")
    val apps: List<AppInfoDTO>
)