package com.dhl.demp.dmac.data.mapper

import com.dhl.demp.dmac.data.db.entity.AppExtraInfoEntity
import com.dhl.demp.dmac.data.network.dto.AppInfoExtendedDTO
import com.dhl.demp.dmac.model.AppVersionItem

fun AppInfoExtendedDTO.toAppExtraInfoEntity(): AppExtraInfoEntity =
    AppExtraInfoEntity(
        appId = this.appId,
        description = this.appDescription,
        releaseNote = this.releaseNote,
        screenshots = this.screenshots,
        versions = this.versions.map {
            AppVersionItem(
                version = it.version,
                releaseNote = it.releaseNote
            )
        },
        developedBy = this.developedBy,
        supportContact = this.supportContact,
    )