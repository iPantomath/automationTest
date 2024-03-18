package com.dhl.demp.dmac.data.pageing_source

import android.content.Context
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dhl.demp.dmac.data.network.AppService
import com.dhl.demp.dmac.model.Notification
import com.dhl.demp.mydmac.LauncherPreference
import java.io.IOException
import java.lang.RuntimeException

class NotificationsPagingSource(
    private val appContext: Context,
    private val api: AppService
) : PagingSource<Int, Notification>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Notification> {
        try {
            val offset = params.key ?: 0
            val limit = params.loadSize

            val clientId = LauncherPreference.getId(appContext)

            val response = runCatching {
                api.getNotifications(clientId, limit, offset)
            }.getOrNull()

            if (response?.isSuccessful == true) {
                val notificationsResponse = response.body() ?: return LoadResult.Error(IllegalStateException("Response body is null"))

                val nextKey = if (notificationsResponse.offset + notificationsResponse.limit < notificationsResponse.total) {
                    notificationsResponse.offset + notificationsResponse.limit
                } else {
                    null
                }
                val prevKey = (offset - limit).takeIf { it >= 0 }

                val data = notificationsResponse
                        .notifications
                        .map {
                            Notification(it.date, it.title, it.message)
                        }

                return LoadResult.Page(
                        data = data,
                        prevKey = prevKey,
                        nextKey = nextKey
                )
            } else {
                return LoadResult.Error(RuntimeException("Error with code ${response?.code()}"))
            }
        } catch (e: IOException) {
            return LoadResult.Error(e)
        } catch (e: Throwable) {
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Notification>): Int? = null
}


