package com.dhl.demp.dmac.domain.appfilter

import com.dhl.demp.dmac.model.AppFullInfo

class PublicAppsFilter : Filter<AppFullInfo> {
    override fun match(item: AppFullInfo): Boolean {
        return item.isWebLink
    }
}