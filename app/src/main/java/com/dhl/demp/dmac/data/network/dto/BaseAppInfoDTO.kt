package com.dhl.demp.dmac.data.network.dto

import com.dhl.demp.dmac.model.BBfiedApproveState

interface BaseAppInfoDTO {
    val appId: String
    val packageId: String
    val name: String
    val iconUrl: String
    val website: String?
    val version: String
    val versionDateMillis: Long
    val annotation: String?
    val releaseType: String?
    val available: Boolean
    val hidden: Boolean
    val launcher: Boolean
    val installationRoutines: List<InstallationRoutineDTO>
    val actions: List<ActionDTO>?
    val categories: List<CategoryDTO>?

    //Blackberry related fields
    val bbfied: Boolean?
    val bbfiedApproveState: BBfiedApproveState?
}