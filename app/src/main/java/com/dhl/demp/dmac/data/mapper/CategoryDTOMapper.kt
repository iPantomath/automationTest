package com.dhl.demp.dmac.data.mapper

import com.dhl.demp.dmac.data.db.entity.AppCategoryEntity
import com.dhl.demp.dmac.data.network.dto.CategoryDTO

fun CategoryDTO.toAppCategoryEntity(appId: String): AppCategoryEntity =
    AppCategoryEntity(
        id = 0,
        appId = appId,
        name = this.name,
        tags = this.tags
    )