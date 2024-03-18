package com.dhl.demp.dmac.data.mapper

import com.dhl.demp.dmac.data.db.entity.AppBBFiedEntity
import com.dhl.demp.dmac.data.db.entity.AppCalculatedInfoEntity
import com.dhl.demp.dmac.data.db.entity.AppInfoEntity
import com.dhl.demp.dmac.data.network.dto.BaseAppInfoDTO
import com.dhl.demp.dmac.model.InstallationRoutineType

fun BaseAppInfoDTO.toAppInfoEntity(): AppInfoEntity =
    AppInfoEntity(
        appId = this.appId,
        packageId = this.packageId,
        name = this.name,
        iconUrl = this.iconUrl,
        website = this.website,
        version = this.version,
        versionDateMillis = this.versionDateMillis,
        releaseType = this.releaseType,
        annotation = this.annotation,
        available = this.available,
        launcher = this.launcher,
        hidden = this.hidden
    )

fun BaseAppInfoDTO.toAppBBFiedEntity(): AppBBFiedEntity =
    AppBBFiedEntity(
        appId = this.appId,
        isBBfied = (this.bbfied == true),
        bbfiedApproveState = this.bbfiedApproveState
    )

fun BaseAppInfoDTO.toAppCalculatedInfoEntity(): AppCalculatedInfoEntity {
    val installationRoutine = this.installationRoutines.first()

    return AppCalculatedInfoEntity(
        appId = this.appId,
        hasDependencies = installationRoutine.dependencies != null,
        isWebLink = installationRoutine.type == InstallationRoutineType.EXTERNAL_LINK
    )
}