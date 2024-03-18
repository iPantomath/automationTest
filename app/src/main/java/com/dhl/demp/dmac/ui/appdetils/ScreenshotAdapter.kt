package com.dhl.demp.dmac.ui.appdetils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dhl.demp.mydmac.activity.ActivityGallery
import mydmac.R
import mydmac.databinding.ItemScreenshotBinding

class ScreenshotAdapter :
    ListAdapter<String, ScreenshotAdapter.ScreenshotViewHolder>(ScreenshotDiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScreenshotViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemScreenshotBinding.inflate(inflater, parent, false)

        return ScreenshotViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScreenshotViewHolder, position: Int) {
        val url = getItem(position)
        holder.bind(url)
    }

    object ScreenshotDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }

    private fun getUrls(): ArrayList<String> {
        val urls = ArrayList<String>(itemCount)
        repeat(itemCount) { i ->
            urls.add(getItem(i))
        }

        return urls
    }

    inner class ScreenshotViewHolder(
        private val binding: ItemScreenshotBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.screenshot.setOnClickListener {
                var position = bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) {
                    position = 0
                }

                ActivityGallery.start(it.context, getUrls(), position)
            }
        }

        fun bind(url: String) {
            Glide.with(binding.screenshot.context)
                .load(url)
                .error(R.drawable.homepage_logo)
                .into(binding.screenshot)
        }
    }
}

