package com.dhl.demp.dmac.data.mapper

import com.dhl.demp.dmac.data.db.model.AppExtendedFullInfoEntity
import com.dhl.demp.dmac.model.AppExtendedFullInfo
import com.dhl.demp.dmac.model.AppExtraAction
import com.dhl.demp.dmac.model.InstallationDependencyInfo

fun AppExtendedFullInfoEntity.toAppExtendedInfo(): AppExtendedFullInfo =
    AppExtendedFullInfo(
        appId = this.appInfo.appId,
        packageId = this.appInfo.packageId,
        name = this.appInfo.name,
        version = this.appInfo.version,
        versionDate = this.appInfo.versionDateMillis,
        iconUrl = this.appInfo.iconUrl,
        releaseType = this.appInfo.releaseType,
        annotation = this.appInfo.annotation,
        screenshots = this.extraInfo?.screenshots,
        versions = this.extraInfo?.versions,
        isAvailable = this.appInfo.available,
        hasDependencies = this.calculatedInfo.hasDependencies,
        isWebLink = this.calculatedInfo.isWebLink,
        isBBfied = this.bbFiedInfo.isBBfied,
        bbfiedApproveState = this.bbFiedInfo.bbfiedApproveState,
        actions = this.actions.map { AppExtraAction(it.id, it.targetPackageId, it.name) },
        categories = this.categories.associate { it.name to it.tags },
        description = this.extraInfo?.description,
        releaseNote = this.extraInfo?.releaseNote,
        website = this.appInfo.website,
        developedBy = this.extraInfo?.developedBy,
        supportContact = this.extraInfo?.supportContact,
        installationDependencies = this.installationDependencies.map {
            InstallationDependencyInfo(
                packageId = it.dependencyPackageId,
                name = it.appName,
                iconUrl = it.appIcon,
                version = it.version,
                releaseType = it.releaseType
            )
        }
    )