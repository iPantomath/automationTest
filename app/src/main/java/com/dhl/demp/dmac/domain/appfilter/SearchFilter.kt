package com.dhl.demp.dmac.domain.appfilter

import com.dhl.demp.dmac.model.AppFullInfo

class SearchFilter(
    private val baseFilter: Filter<AppFullInfo>,
    private val searchText: String
) : Filter<AppFullInfo> {
    override fun match(item: AppFullInfo): Boolean {
        //first apply base filter
        if (!baseFilter.match(item)) {
            return false
        }

        if (searchText.isBlank()) {
            return true
        }

        if (item.name.contains(searchText, ignoreCase = true)) {
            return true
        }

        if (item.annotation?.contains(searchText, ignoreCase = true) == true) {
            return true
        }

        //TODO check all needed fields:
        //2) Utils.searchInCategories(item.categories, queryLower)

        return false
    }
}