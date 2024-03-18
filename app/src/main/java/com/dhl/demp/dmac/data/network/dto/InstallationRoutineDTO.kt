package com.dhl.demp.dmac.data.network.dto

import com.dhl.demp.dmac.model.InstallationRoutineType
import com.google.gson.annotations.SerializedName

class InstallationRoutineDTO(
    @SerializedName("type")
    val type: InstallationRoutineType,
    @SerializedName("source")
    val source: String,
    @SerializedName("size")
    val size: Long?,
    @SerializedName("md5")
    val md5: String?,
    @SerializedName("dependency")
    val dependencies: List<InstallationDependencyDTO>?
)