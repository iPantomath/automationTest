package com.dhl.demp.dmac.ui.notifications

import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter

class NotificationsLoadStateAdapter(
        private val retry: () -> Unit
) : LoadStateAdapter<NotificationsLoadStateViewHolder>() {
    override fun onBindViewHolder(holder: NotificationsLoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): NotificationsLoadStateViewHolder {
        return NotificationsLoadStateViewHolder.create(parent, retry)
    }
}