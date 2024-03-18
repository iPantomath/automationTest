package com.dhl.demp.dmac.data.network.dto

import com.google.gson.annotations.SerializedName

class CategoryDTO(
    @SerializedName("name")
    val name: String,
    @SerializedName("tags")
    val tags: List<String>
)