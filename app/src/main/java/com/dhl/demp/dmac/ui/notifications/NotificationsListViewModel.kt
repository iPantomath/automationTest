package com.dhl.demp.dmac.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dhl.demp.dmac.model.Notification
import com.dhl.demp.dmac.data.repository.NotificationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class NotificationsListViewModel @Inject constructor(
    notificationsRepository: NotificationsRepository
) : ViewModel() {
    val notifications: Flow<PagingData<Notification>> =
        notificationsRepository
            .getNotificationsStream()
            .cachedIn(viewModelScope)
}