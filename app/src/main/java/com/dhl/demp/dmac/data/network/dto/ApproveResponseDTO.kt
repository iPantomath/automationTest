package com.dhl.demp.dmac.data.network.dto

import com.dhl.demp.dmac.model.BBfiedApproveState
import com.google.gson.annotations.SerializedName

class ApproveResponseDTO(
    @SerializedName("state")
    val approveState: BBfiedApproveState?,
    @SerializedName("message")
    val message: String?
)