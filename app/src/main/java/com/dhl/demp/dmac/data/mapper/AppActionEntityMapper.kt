package com.dhl.demp.dmac.data.mapper

import com.dhl.demp.dmac.data.db.entity.AppActionEntity
import com.dhl.demp.dmac.model.AppActionInfo

fun AppActionEntity.toAppActionInfo(): AppActionInfo =
    AppActionInfo(
        appId = this.appId,
        actionId = this.id,
        name = this.name,
        url = this.url,
        targetPackageId = this.targetPackageId,
        message = this.message
    )