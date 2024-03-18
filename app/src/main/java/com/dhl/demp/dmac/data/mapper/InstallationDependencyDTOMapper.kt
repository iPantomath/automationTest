package com.dhl.demp.dmac.data.mapper

import com.dhl.demp.dmac.data.db.entity.AppInstallationDependencyEntity
import com.dhl.demp.dmac.data.network.dto.InstallationDependencyDTO

fun InstallationDependencyDTO.toAppInstallationDependencyEntity(appId: String): AppInstallationDependencyEntity =
    AppInstallationDependencyEntity(
        id = 0,
        appId = appId,
        dependencyAppId = this.appId,
        dependencyPackageId = this.packageId,
        appName = this.appName,
        appIcon = this.appIcon,
        version = this.version,
        type = this.type,
        source = this.source,
        size = this.size,
        md5 = this.md5,
        releaseType = this.releaseType,
        installBefore = this.installBefore,
        available = this.available
    )