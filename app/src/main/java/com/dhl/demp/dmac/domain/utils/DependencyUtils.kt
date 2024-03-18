package com.dhl.demp.dmac.domain.utils

interface DependencyUtils {
    fun isDependencyResolved(packageId: String, version: String): Boolean
}