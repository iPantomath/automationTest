package com.dhl.demp.dmac.ui.appdetils

import android.os.Bundle
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.dhl.demp.dmac.ext.*
import com.dhl.demp.dmac.model.AppExtraAction
import com.dhl.demp.dmac.model.AppMainAction
import com.dhl.demp.dmac.model.AppVersionItem
import com.dhl.demp.dmac.model.InstallationDependencyInfo
import com.dhl.demp.dmac.ui.appdetils.AppDetailsViewModel.Action
import com.dhl.demp.dmac.ui.appdetils.AppDetailsViewModel.Event
import com.dhl.demp.dmac.ui.shared.getMainActionIconRes
import com.dhl.demp.dmac.ui.shared.getMainActionTextRes
import com.dhl.demp.mydmac.dialog.InstallationSequenceApproveDialog
import com.dhl.demp.mydmac.dialog.RequestWaitDialog
import com.dhl.demp.mydmac.obj.InstallationAppItem
import com.dhl.demp.mydmac.utils.Utils
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import mydmac.R
import mydmac.databinding.AppActionButtonLayoutBinding
import mydmac.databinding.AppTagsLayoutBinding
import mydmac.databinding.DependencyItemBinding
import mydmac.databinding.FragmentAppDetailsBinding
import java.text.DateFormat

@AndroidEntryPoint
class AppDetailsFragment : Fragment(R.layout.fragment_app_details) {
    companion object {
        private const val KEY_ALL_VERSIONS_SHOWN = "all_versions_shown"
        private val DATE_TIME_FORMAT =
            DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)

        fun newInstance(appId: String): AppDetailsFragment {
            val instance = AppDetailsFragment()
            instance.arguments = bundleOf(
                AppDetailsViewModel.KEY_APP_ID to appId
            )

            return instance
        }
    }

    private val viewModel by viewModels<AppDetailsViewModel>()
    private val binding: FragmentAppDetailsBinding by viewBinding()
    private var screenshotAdapter: ScreenshotAdapter? = null
    private val waitDialog by lazy(LazyThreadSafetyMode.NONE) { RequestWaitDialog(requireContext()) }

    private var latestVersionHtml: Spanned? = null
    private var allVersionsHtml: Spanned? = null
    private var allVersionsShown = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            allVersionsShown =
                savedInstanceState.getBoolean(KEY_ALL_VERSIONS_SHOWN, allVersionsShown)
        }

        initViews()
        initViewModel()
    }

    private fun initViews() {
        val appId = arguments?.getString(AppDetailsViewModel.KEY_APP_ID)

        //set transaction names for shared view transactions
        binding.icon.transitionName = "app_icon_${appId}"
        binding.releaseType.transitionName = "release_type_${appId}"

        binding.screenshots.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        screenshotAdapter = ScreenshotAdapter()
        binding.screenshots.adapter = screenshotAdapter

        binding.versionsExpand.setOnClickListener {
            allVersionsShown = !allVersionsShown
            adjustVersionsView()
        }

        binding.mainActionButton.setOnClickListener {
            viewModel.processAction(
                action = Action.OnAppMainAction
            )
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean(KEY_ALL_VERSIONS_SHOWN, allVersionsShown)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        screenshotAdapter = null
    }

    private fun initViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.appInfo.collect(::onNewAppInfo)
                }
                launch {
                    viewModel.events.collect(::onEvent)
                }
            }
        }
    }

    private fun onNewAppInfo(appInfo: AppDetailsInfo) {
        requireActivity().title = appInfo.name

        binding.icon.loadAppIcon(
            appInfo.iconUrl,
            resources.getDimensionPixelSize(R.dimen.app_details_icon_size) * 2
        )
        binding.releaseType.adjustReleaseTypeLabel(appInfo.releaseType)
        binding.title.text = appInfo.name
        binding.annotation.text = appInfo.annotation
        binding.version.text = getString(R.string.version_template, appInfo.version)

        updateAppScreenshots(appInfo.screenshots)

        binding.annotationFull.text = appInfo.annotation
        binding.updateDate.text = DATE_TIME_FORMAT.format(appInfo.versionDate)
        binding.description.text = appInfo.description
        updateAppWebsite(appInfo.website)

        updateInstallationDependencies(
            appInfo.installationDependencies,
            appInfo.resolvedDependencies
        )

        updateTags(appInfo.tags)

        updateReleaseNote(appInfo.releaseNote)

        updateAppVersions(appInfo.versions)

        updateVendorInfo(appInfo.developedBy, appInfo.supportContact)

        bindMainAction(appInfo.mainAction)
        bindExtraActions(appInfo.extraActions)
    }

    private fun onEvent(event: Event) {
        when (event) {
            is Event.LaunchApp -> requireContext().launchApp(event.packageName)
            is Event.OpenLink -> requireContext().openLink(event.link)
            is Event.ShowToast -> showToast(event.text)
            is Event.ShowSnackbar ->
                Snackbar.make(binding.root, event.resId, Snackbar.LENGTH_LONG).show()
            is Event.ShowErrorNotification -> showToast(event.resId)
            Event.ShowWaitDialog -> showWaitDialog()
            Event.HideWaitDialog -> hideWaitDialog()
            is Event.ShowInstallationSequenceApproveDialog ->
                showInstallationSequenceApproveDialog(
                    event.appItem,
                    event.dependencyItems
                )
        }.exhaustive
    }

    private fun showWaitDialog() {
        waitDialog.show()
    }

    private fun hideWaitDialog() {
        waitDialog.dismiss()
    }

    private fun showInstallationSequenceApproveDialog(
        appItem: InstallationAppItem,
        dependencyItems: Array<InstallationAppItem>
    ) {
        InstallationSequenceApproveDialog
            .newInstance(appItem, dependencyItems)
            .show(parentFragmentManager, null)
    }

    private fun updateAppScreenshots(screenshots: List<String>?) {
        if (screenshots.isNullOrEmpty()) {
            binding.screenshotsCard.hide()
            return
        }

        binding.screenshotsCard.show()
        screenshotAdapter?.submitList(screenshots)
    }

    private fun updateAppWebsite(website: String?) {
        if (website.isNullOrEmpty()) {
            binding.websiteTitle.hide()
            binding.website.hide()
        } else {
            binding.websiteTitle.show()
            binding.website.show()

            binding.website.text = ("<u>" + getString(R.string.visit_web) + "</u>").fromHtml()

            binding.website.setOnClickListener {
                openLink(website)
            }
        }
    }

    private fun updateInstallationDependencies(
        dependencies: List<InstallationDependencyInfo>,
        resolvedDependencies: Set<String>
    ) {
        if (dependencies.isEmpty()) {
            binding.dependenciesCard.hide()
            return
        }

        binding.dependenciesCard.show()

        binding.dependenciesContainer.removeAllViews()
        binding.dependenciesContainer.addView(binding.dependenciesTitle)

        val layoutInflater = LayoutInflater.from(requireContext())

        dependencies.forEach { dependency ->
            val item =
                DependencyItemBinding.inflate(layoutInflater, binding.dependenciesContainer, false)

            item.appName.text = dependency.name
            item.appVersion.text = getString(R.string.version_template, dependency.version)
            Utils.createIconRequest(requireContext(), dependency.iconUrl)
                .placeholder(R.drawable.apps_list_default_icon)
                .into(item.appIcon)
            item.releaseType.adjustReleaseTypeLabel(dependency.releaseType)
            if (resolvedDependencies.contains(dependency.packageId)) {
                item.appInstalledMarker.show()
            } else {
                item.appInstalledMarker.hide()
            }

            binding.dependenciesContainer.addView(item.root)
        }
    }

    private fun updateTags(tags: Map<String, String>) {
        if (tags.isEmpty()) {
            binding.tagsCard.hide()
            return
        }

        binding.tagsCard.show()

        binding.tagsContainer.removeAllViews()
        binding.tagsContainer.addView(binding.tagsTitle)

        val layoutInflater = LayoutInflater.from(requireContext())

        tags.forEach { (name, values) ->
            val item = AppTagsLayoutBinding.inflate(layoutInflater, binding.tagsContainer, false)
            item.name.text = name
            item.tags.text = values

            binding.tagsContainer.addView(item.root)
        }
    }

    private fun updateReleaseNote(releaseNote: String?) {
        if (releaseNote.isNullOrEmpty()) {
            binding.releaseNoteCard.hide()
        } else {
            binding.releaseNoteCard.show()
            binding.releaseNote.text = releaseNote
        }
    }

    private fun updateAppVersions(versions: List<AppVersionItem>?) {
        if (versions.isNullOrEmpty()) {
            binding.versionsCard.hide()
            return
        }

        binding.versionsCard.show()

        latestVersionHtml = appVersionItemToHtml(versions.first()).fromHtml()
        allVersionsHtml = versions
            .fold("", { acc, item -> acc + appVersionItemToHtml(item) })
            .fromHtml()

        if (versions.size > 1) {
            binding.versionsSeparator.show()
            binding.versionsExpand.show()

            adjustVersionsView()
        } else {
            binding.versionsSeparator.hide()
            binding.versionsExpand.hide()
        }
    }

    private fun adjustVersionsView() {
        if (allVersionsShown) {
            binding.versions.text = allVersionsHtml
            binding.versionsExpand.setText(R.string.less)
        } else {
            binding.versions.text = latestVersionHtml
            binding.versionsExpand.setText(R.string.more)
        }
    }

    private fun updateVendorInfo(developedBy: String?, supportContact: String?) {
        if (!developedBy.isNullOrEmpty() || !supportContact.isNullOrEmpty()) {
            binding.vendorInfoCard.show()

            //developed by
            if (!developedBy.isNullOrEmpty()) {
                binding.developedBy.text = developedBy

                binding.developedByTitle.show()
                binding.developedBy.show()
            } else {
                binding.developedByTitle.hide()
                binding.developedBy.hide()
            }

            //support contact
            if (!supportContact.isNullOrEmpty()) {
                binding.supportContact.text = supportContact

                binding.supportContactTitle.show()
                binding.supportContact.show()
            } else {
                binding.supportContactTitle.hide()
                binding.supportContact.hide()
            }
        } else {
            binding.vendorInfoCard.hide()
        }
    }

    private fun bindMainAction(action: AppMainAction) {
        if (action is AppMainAction.Absent || action is AppMainAction.Progress) {
            binding.mainActionButton.isInvisible = true
        } else {
            binding.mainActionButton.isInvisible = false

            val bgColorRes = getMainActionColorRes(action)
            binding.mainActionButton.backgroundTintList =
                resources.getColorStateList(bgColorRes, null)
            binding.mainActionButton.setText(getMainActionTextRes(action))
            binding.mainActionButton.setIconResource(getMainActionIconRes(action))
        }

        if (action is AppMainAction.Progress) {
            binding.mainActionProgress.show()
        } else {
            binding.mainActionProgress.hide()
        }
    }

    private fun getMainActionColorRes(action: AppMainAction): Int {
        return when (action) {
            AppMainAction.Installed, AppMainAction.Requested -> R.color.dark_grey
            else -> R.color.confirmButtonNormal
        }
    }

    private fun bindExtraActions(extraActions: List<AppExtraAction>) {
        if (extraActions.isEmpty()) {
            binding.extraActionsContainer.hide()
            return
        }

        binding.extraActionsContainer.show()
        binding.extraActionsContainer.removeAllViews()

        extraActions.forEach { action ->
            val layoutInflater = LayoutInflater.from(requireContext())

            val actionBinding = AppActionButtonLayoutBinding.inflate(
                layoutInflater,
                binding.extraActionsContainer,
                false
            )

            actionBinding.action.text = action.actionName
            actionBinding.action.setOnClickListener {
                viewModel.processAction(
                    action = Action.OnAppExtraAction(action)
                )
            }

            binding.extraActionsContainer.addView(actionBinding.root)
        }
    }

    private fun appVersionItemToHtml(versionItem: AppVersionItem): String {
        val versionText = versionItem.releaseNote
            .orEmpty()
            .replace("\n", "<br/>")

        return "<b>${versionItem.version}</b> $versionText<br/>"
    }
}