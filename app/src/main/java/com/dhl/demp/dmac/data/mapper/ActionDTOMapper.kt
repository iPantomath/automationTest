package com.dhl.demp.dmac.data.mapper

import com.dhl.demp.dmac.data.db.entity.AppActionEntity
import com.dhl.demp.dmac.data.network.dto.ActionDTO

fun ActionDTO.toAppActionEntity(appId: String): AppActionEntity =
    AppActionEntity(
        id = this.id,
        appId = appId,
        name = this.name,
        url = this.url,
        targetPackageId = this.targetPackageId,
        message = this.message
    )