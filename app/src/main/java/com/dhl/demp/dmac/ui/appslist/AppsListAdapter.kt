package com.dhl.demp.dmac.ui.appslist

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import mydmac.R
import mydmac.databinding.ItemAppsListBinding

class AppsListAdapter(
    private val itemListener: AppsListItemListener
) : ListAdapter<AppsListItem, AppsListViewHolder>(AppInfoDiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppsListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemAppsListBinding.inflate(inflater, parent, false)
        val iconSize = getAppIconSize(parent.context)
        return AppsListViewHolder(binding, iconSize, ::getItem, itemListener)
    }

    private fun getAppIconSize(context: Context): Int {
        //need to load an icon twice bigger to have a good quality
        //app_details_icon_size is used because it has bigger size and the image will be cached
        //in memory with this size and will be instantly available on the details screen
        return context.resources.getDimensionPixelSize(R.dimen.app_details_icon_size) * 2
    }

    override fun onBindViewHolder(holder: AppsListViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    object AppInfoDiffCallback : DiffUtil.ItemCallback<AppsListItem>() {
        override fun areItemsTheSame(oldItem: AppsListItem, newItem: AppsListItem): Boolean {
            return oldItem.appId == newItem.appId
        }

        override fun areContentsTheSame(oldItem: AppsListItem, newItem: AppsListItem): Boolean {
            return oldItem == newItem
        }
    }
}