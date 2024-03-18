package com.dhl.demp.dmac.data.network.dto

import com.dhl.demp.dmac.model.BBfiedApproveState
import com.google.gson.annotations.SerializedName

class AppInfoExtendedDTO(
    @SerializedName("app_id")
    override val appId: String,
    @SerializedName("package_id")
    override val packageId: String,
    @SerializedName("app_name")
    override val name: String,
    @SerializedName("icon")
    override val iconUrl: String,
    @SerializedName("website")
    override val website: String?,
    @SerializedName("app_version")
    override val version: String,
    @SerializedName("app_version_date_ms")
    override val versionDateMillis: Long,
    @SerializedName("annotation")
    override val annotation: String?,
    @SerializedName("release_type")
    override val releaseType: String?,
    @SerializedName("available")
    override val available: Boolean,
    @SerializedName("hidden")
    override val hidden: Boolean,
    @SerializedName("launcher")
    override val launcher: Boolean,
    @SerializedName("installation_routine")
    override val installationRoutines: List<InstallationRoutineDTO>,
    @SerializedName("actions")
    override val actions: List<ActionDTO>?,
    @SerializedName("category")
    override val categories: List<CategoryDTO>?,
    @SerializedName("bbfied")
    override val bbfied: Boolean?,
    @SerializedName("state")
    override val bbfiedApproveState: BBfiedApproveState?,
    @SerializedName("app_desc")
    val appDescription: String,
    @SerializedName("release_note")
    val releaseNote: String,
    @SerializedName("screenshots")
    val screenshots: List<String>,
    @SerializedName("versions")
    val versions: List<AppVersionDTO>,
    @SerializedName("developed_by")
    val developedBy: String,
    @SerializedName("support_contact")
    val supportContact: String?,
) : BaseAppInfoDTO