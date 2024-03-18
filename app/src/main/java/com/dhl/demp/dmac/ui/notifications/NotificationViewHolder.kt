package com.dhl.demp.dmac.ui.notifications

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dhl.demp.dmac.model.Notification
import mydmac.databinding.ItemNotificationBinding

class NotificationViewHolder(
    private val binding: ItemNotificationBinding
) : RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun create(parent: ViewGroup): NotificationViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemNotificationBinding.inflate(inflater, parent, false)

            return NotificationViewHolder(binding)
        }
    }

    fun bind(notification: Notification) {
        binding.title.text = notification.title
        binding.date.text = notification.displayDate
        binding.message.text = notification.message
    }
}