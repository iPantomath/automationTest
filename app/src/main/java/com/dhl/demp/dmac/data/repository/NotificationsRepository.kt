package com.dhl.demp.dmac.data.repository

import androidx.paging.PagingData
import com.dhl.demp.dmac.model.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationsRepository {
    fun getNotificationsStream(): Flow<PagingData<Notification>>
}