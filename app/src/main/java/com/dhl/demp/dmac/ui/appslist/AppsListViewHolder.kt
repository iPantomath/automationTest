package com.dhl.demp.dmac.ui.appslist

import android.view.View
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.util.Pair
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.dhl.demp.dmac.ext.adjustReleaseTypeLabel
import com.dhl.demp.dmac.ext.loadAppIcon
import com.dhl.demp.dmac.model.AppExtraAction
import com.dhl.demp.dmac.model.AppMainAction
import com.dhl.demp.dmac.ui.shared.getMainActionIconRes
import com.dhl.demp.dmac.ui.shared.getMainActionTextRes
import mydmac.R
import mydmac.databinding.ItemAppsListBinding
import java.util.*

class AppsListViewHolder(
    private val binding: ItemAppsListBinding,
    private val iconSize: Int,
    private val getItem: (Int) -> AppsListItem,
    private val itemListener: AppsListItemListener
) : RecyclerView.ViewHolder(binding.root) {
    init {
        setupListeners()
    }

    private fun setupListeners() {
        binding.root.setOnClickListener {
            withItem { item ->
                itemListener.onAppSelected(
                    appId = item.appId,
                    sharedElements = buildSharedElementsList()
                )
            }
        }
        binding.detail.setOnClickListener {
            withItem { item ->
                itemListener.onAppSelected(
                    appId = item.appId,
                    sharedElements = buildSharedElementsList()
                )
            }
        }
        binding.mainAction.setOnClickListener {
            withItem { item ->
                itemListener.onAppMainActionSelected(item.appId, item.mainAction)
            }
        }
        binding.extraAction1.setOnClickListener {
            withItem { item ->
                itemListener.onExtraActionSelected(item.appId, item.extraActions.first())
            }
        }
        binding.extraActionMore.setOnClickListener {
            withItem { item ->
                showMoreExtraActions(item.appId, item.extraActions)
            }
        }
    }

    private fun withItem(action: (AppsListItem) -> Unit) {
        val position = bindingAdapterPosition
        if (position != RecyclerView.NO_POSITION) {
            val item = getItem(position)
            action(item)
        }
    }

    private fun buildSharedElementsList(): Array<Pair<View, String>> {
        return if (binding.releaseType.isVisible) {
            arrayOf(
                Pair(binding.icon, binding.icon.transitionName),
                Pair(binding.releaseType, binding.releaseType.transitionName),
            )
        } else {
            arrayOf(
                Pair(binding.icon, binding.icon.transitionName)
            )
        }
    }

    private fun showMoreExtraActions(appId: String, extraActions: List<AppExtraAction>) {
        val popup = PopupMenu(binding.extraActionMore.context, binding.extraActionMore)
        val textColor = binding.extraAction1.textColors.defaultColor

        for (i in 1 until extraActions.size) {
            val formattedName = buildSpannedString {
                color(textColor) {
                    append(extraActions[i].actionName.uppercase(Locale.getDefault()))
                }
            }

            popup.menu.add(0, i, i, formattedName)
        }

        popup.setOnMenuItemClickListener { selectedItem ->
            val selectedAction = extraActions[selectedItem.itemId]
            itemListener.onExtraActionSelected(appId, selectedAction)

            true
        }

        popup.show()
    }

    fun bind(item: AppsListItem) {
        binding.icon.loadAppIcon(item.iconUrl, iconSize)
        binding.releaseType.adjustReleaseTypeLabel(item.releaseType)
        binding.name.text = item.name
        binding.appAnnotation.text = item.annotation
        binding.appVersion.text = binding.appVersion.resources.getString(R.string.version_template, item.version)

        bindMainAction(item.mainAction)
        bindExtraActions(item.extraActions)

        //set transaction names for shared view transactions
        binding.icon.transitionName = "app_icon_${item.appId}"
        binding.releaseType.transitionName = "release_type_${item.appId}"
    }

    private fun bindMainAction(action: AppMainAction) {
        if (action is AppMainAction.Absent || action is AppMainAction.Progress) {
            binding.mainAction.visibility = View.INVISIBLE
        } else {
            binding.mainAction.visibility = View.VISIBLE

            binding.mainAction.setText(getMainActionTextRes(action))
            val mainActionTextColor =
                ContextCompat.getColor(binding.mainAction.context, getMainActionTexColorRes(action))
            binding.mainAction.setTextColor(mainActionTextColor)
            binding.mainAction.setIconResource(getMainActionIconRes(action))
        }

        if (action is AppMainAction.Progress) {
            binding.mainActionProgress.visibility = View.VISIBLE
        } else {
            binding.mainActionProgress.visibility = View.GONE
        }
    }

    private fun bindExtraActions(actions: List<AppExtraAction>) {
        var extraAction1Visibility = View.GONE
        var extraActionMoreVisibility = View.GONE

        if (actions.isNotEmpty()) {
            extraAction1Visibility = View.VISIBLE

            binding.extraAction1.text = actions.first().actionName
        }
        if (actions.size > 1) {
            extraActionMoreVisibility = View.VISIBLE
        }

        binding.extraAction1Divider.visibility = extraAction1Visibility
        binding.extraAction1.visibility = extraAction1Visibility
        binding.extraActionMoreDivider.visibility = extraActionMoreVisibility
        binding.extraActionMore.visibility = extraActionMoreVisibility
    }

    private fun getMainActionTexColorRes(action: AppMainAction): Int {
        return when (action) {
            AppMainAction.Requested, AppMainAction.Installed -> R.color.dark_grey
            else -> R.color.confirmButtonNormal
        }
    }
}