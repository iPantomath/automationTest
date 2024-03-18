package com.dhl.demp.dmac.ui.notifications

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import mydmac.databinding.NotificationsLoadStateFooterBinding

class NotificationsLoadStateViewHolder(
        private val binding: NotificationsLoadStateFooterBinding,
        private val retry: () -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun create(parent: ViewGroup, retry: () -> Unit): NotificationsLoadStateViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = NotificationsLoadStateFooterBinding.inflate(inflater, parent, false)

            return NotificationsLoadStateViewHolder(binding, retry)
        }
    }

    init {
        binding.retryButton.setOnClickListener { retry() }
    }

    fun bind(loadState: LoadState) {
        binding.progressBar.isVisible = loadState is LoadState.Loading
        binding.retryButton.isVisible = loadState !is LoadState.Loading
    }
}