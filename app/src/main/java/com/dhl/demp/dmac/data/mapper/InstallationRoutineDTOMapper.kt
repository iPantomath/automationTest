package com.dhl.demp.dmac.data.mapper

import com.dhl.demp.dmac.data.db.entity.AppInstallationRoutineEntity
import com.dhl.demp.dmac.data.network.dto.InstallationRoutineDTO

fun InstallationRoutineDTO.toAppInstallationRoutineEntity(appId: String): AppInstallationRoutineEntity =
    AppInstallationRoutineEntity(
        appId = appId,
        type = this.type,
        source = this.source,
        size = this.size,
        md5 = this.md5
    )