package com.dhl.demp.dmac.data.mapper

import com.dhl.demp.dmac.data.db.model.AppFullInfoEntity
import com.dhl.demp.dmac.model.AppExtraAction
import com.dhl.demp.dmac.model.AppFullInfo

fun AppFullInfoEntity.toAppInfo(): AppFullInfo =
    AppFullInfo(
        appId = this.appInfo.appId,
        packageId = this.appInfo.packageId,
        name = this.appInfo.name,
        version  = this.appInfo.version,
        iconUrl = this.appInfo.iconUrl,
        releaseType = this.appInfo.releaseType,
        annotation = this.appInfo.annotation,
        isAvailable = this.appInfo.available,
        hasDependencies = this.calculatedInfo.hasDependencies,
        isWebLink = this.calculatedInfo.isWebLink,
        isBBfied = this.bbFiedInfo.isBBfied,
        bbfiedApproveState = this.bbFiedInfo.bbfiedApproveState,
        actions = this.actions.map { AppExtraAction(it.id, it.targetPackageId, it.name) }
    )