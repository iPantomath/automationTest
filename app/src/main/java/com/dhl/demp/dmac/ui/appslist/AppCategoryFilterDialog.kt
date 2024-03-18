package com.dhl.demp.dmac.ui.appslist

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.FrameLayout
import com.dhl.demp.dmac.model.AppCategory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import mydmac.R
import java.lang.IllegalArgumentException

private val CHECKBOX_IDS = arrayOf(
        R.id.show_all,
        R.id.dpdhl_group,
        R.id.global_forwarding_freight,
        R.id.supply_chain,
        R.id.ecs,
        R.id.express,
        R.id.gbs_csi_cc,
        R.id.pnp
)

class AppCategoryFilterDialog(
    private val initialSelectedCategories: Set<AppCategory>,
    private val onSelectionChanged: (Set<AppCategory>) -> Unit
) : BottomSheetDialogFragment() {
    private var checkboxes: List<CheckBox>? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        //on several devices not whole UI is shown. This is workaround for showing whole UI
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
                    ?: return@setOnShowListener
            val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
            behavior.skipCollapsed = true
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_app_category_filter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkboxes = CHECKBOX_IDS.map { id -> view.findViewById(id) }

        checkInitialSelectedCategories()
        setupListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        checkboxes = null
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)

        val currentSelectedCategories = buildSelectedCategories()
        if (currentSelectedCategories != initialSelectedCategories) {
            onSelectionChanged(currentSelectedCategories)
        }
    }

    private fun checkInitialSelectedCategories() {
        initialSelectedCategories.forEach {
            val viewId = when (it) {
                AppCategory.ALL -> R.id.show_all
                AppCategory.DPDHL_GROUP -> R.id.dpdhl_group
                AppCategory.DGFF -> R.id.global_forwarding_freight
                AppCategory.DSC -> R.id.supply_chain
                AppCategory.ECS -> R.id.ecs
                AppCategory.EXP -> R.id.express
                AppCategory.GBS -> R.id.gbs_csi_cc
                AppCategory.CSI -> R.id.gbs_csi_cc
                AppCategory.CC -> R.id.gbs_csi_cc
                AppCategory.PNP -> R.id.pnp
            }

            view?.findViewById<CheckBox>(viewId)?.isChecked = true
        }
    }

    private fun setupListeners() {
        val listener = CompoundButton.OnCheckedChangeListener { cb, isChecked ->
            if (cb.id == R.id.show_all && isChecked) {
                checkAllCheckboxes()
            } else {
                validateCheckboxes()
            }
        }

        checkboxes?.forEach { cb ->
            cb.setOnCheckedChangeListener(listener)
        }
    }

    private fun validateCheckboxes() {
        val noSelection = checkboxes?.all { !it.isChecked } == true
        if (noSelection) {
            checkAllCheckboxes()
        }
    }

    private fun checkAllCheckboxes() {
        checkboxes?.forEach { it.isChecked = true }
    }

    private fun buildSelectedCategories(): Set<AppCategory> {
        return checkboxes
                ?.filter { it.isChecked }
                ?.flatMap(::mapSelectedCategories)
                ?.toSet()
                .orEmpty()
    }

    private fun mapSelectedCategories(item: CheckBox): List<AppCategory> {
        return when (item.id) {
            R.id.show_all -> listOf(AppCategory.ALL)
            R.id.dpdhl_group -> listOf(AppCategory.DPDHL_GROUP)
            R.id.global_forwarding_freight -> listOf(AppCategory.DGFF)
            R.id.supply_chain -> listOf(AppCategory.DSC)
            R.id.ecs -> listOf(AppCategory.ECS)
            R.id.express -> listOf(AppCategory.EXP)
            R.id.gbs_csi_cc -> listOf(AppCategory.GBS, AppCategory.CSI, AppCategory.CC)
            R.id.pnp -> listOf(AppCategory.PNP)
            else -> throw IllegalArgumentException("Unknown view ${item.id}(${item.text})")
        }
    }
}