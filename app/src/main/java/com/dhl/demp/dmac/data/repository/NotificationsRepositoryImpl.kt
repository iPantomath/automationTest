package com.dhl.demp.dmac.data.repository

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dhl.demp.dmac.data.network.AppService
import com.dhl.demp.dmac.data.pageing_source.NotificationsPagingSource
import com.dhl.demp.dmac.model.Notification
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

private const val NETWORK_PAGE_SIZE = 20

class NotificationsRepositoryImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val appService: AppService,
) : NotificationsRepository {
    override fun getNotificationsStream(): Flow<PagingData<Notification>> {
        return Pager(
            config = PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                NotificationsPagingSource(appContext, appService)
            }
        ).flow
    }
}