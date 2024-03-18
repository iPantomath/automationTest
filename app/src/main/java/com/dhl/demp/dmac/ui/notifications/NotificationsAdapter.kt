package com.dhl.demp.dmac.ui.notifications

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.dhl.demp.dmac.model.Notification

class NotificationsAdapter : PagingDataAdapter<Notification, NotificationViewHolder>(NOTIFICATIONS_COMPARATOR) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        return NotificationViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val repoItem = getItem(position)
        if (repoItem != null) {
            holder.bind(repoItem)
        }
    }

    companion object {
        private val NOTIFICATIONS_COMPARATOR = object : DiffUtil.ItemCallback<Notification>() {
            override fun areItemsTheSame(oldItem: Notification, newItem: Notification): Boolean =
                    oldItem.date == newItem.date

            override fun areContentsTheSame(oldItem: Notification, newItem: Notification): Boolean =
                    oldItem == newItem
        }
    }
}