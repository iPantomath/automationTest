package com.dhl.demp.dmac.domain.appfilter

import androidx.annotation.WorkerThread

interface Filter<T> {
    @WorkerThread
    fun match(item: T): Boolean

    companion object
}