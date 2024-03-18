package com.dhl.demp.dmac.ui.appslist

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.os.bundleOf
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.dhl.demp.dmac.ext.*
import com.dhl.demp.dmac.model.AppExtraAction
import com.dhl.demp.dmac.model.AppMainAction
import com.dhl.demp.dmac.ui.appdetils.AppDetailsActivity
import com.dhl.demp.dmac.ui.appslist.AppsListViewModel.Action
import com.dhl.demp.dmac.ui.appslist.AppsListViewModel.Event
import com.dhl.demp.mydmac.dialog.InstallationSequenceApproveDialog
import com.dhl.demp.mydmac.dialog.RequestWaitDialog
import com.dhl.demp.mydmac.obj.InstallationAppItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import mydmac.R
import mydmac.databinding.FragmentAppsListBinding

@AndroidEntryPoint
class AppsListFragment : Fragment(R.layout.fragment_apps_list), AppsListItemListener {
    companion object {
        fun newInstance(listType: AppsListType): AppsListFragment {
            val instance = AppsListFragment()
            instance.arguments = bundleOf(AppsListViewModel.KEY_LIST_TYPE to listType)

            return instance
        }
    }

    private val viewModel by viewModels<AppsListViewModel>()
    private val binding: FragmentAppsListBinding by viewBinding()
    private var adapter: AppsListAdapter? = null
    private val waitDialog by lazy(LazyThreadSafetyMode.NONE) { RequestWaitDialog(requireContext()) }
    private var isFirstShow = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initViewModel()
    }

    override fun onResume() {
        super.onResume()

        viewModel.processAction(
            action = Action.ScreenShown(isFirstShow)
        )

        isFirstShow = false
    }

    override fun onDestroyView() {
        super.onDestroyView()

        adapter = null
    }

    private fun initViews() {
        binding.refreshLayout.setOnRefreshListener {
            forceRefreshAppsList()
        }

        binding.appsList.layoutManager = GridLayoutManager(
            requireContext(),
            resources.getInteger(R.integer.apps_list_columns_count)
        )
        adapter = AppsListAdapter(this)
        binding.appsList.adapter = adapter
    }

    private fun initViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.appsList.collect(::onNewAppsList)
                }
                launch {
                    viewModel.events.collect(::onEvent)
                }
            }
        }
    }

    private fun onEvent(event: Event) {
        when (event) {
            Event.ShowProgress -> binding.refreshLayout.isRefreshing = true
            Event.HideProgress -> binding.refreshLayout.isRefreshing = false
            is Event.ShowErrorNotification -> showToast(event.resId)
            is Event.OpenLink -> requireContext().openLink(event.link)
            is Event.LaunchApp -> requireContext().launchApp(event.packageName)
            is Event.ShowToast -> showToast(event.text)
            Event.ShowWaitDialog -> showWaitDialog()
            Event.HideWaitDialog -> hideWaitDialog()
            is Event.ShowInstallationSequenceApproveDialog -> showInstallationSequenceApproveDialog(event.appItem, event.dependencyItems)
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

    private fun onNewAppsList(appsList: List<AppsListItem>) {
        adapter?.submitList(appsList)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.menu_apps_list, menu)

        val searchView = menu.findItem(R.id.search).actionView as SearchView
        initSearchView(searchView)
    }

    private fun initSearchView(searchView: SearchView) {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newSearchText: String?): Boolean {
                viewModel.processAction(
                    action = Action.SetSearchText(newSearchText.orEmpty())
                )

                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean = true
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.category_filter -> {
                showAppCategoryFilter()
                return true
            }
            R.id.refresh_apps_list -> {
                forceRefreshAppsList()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onAppSelected(appId: String, sharedElements: Array<Pair<View, String>>) {
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), *sharedElements).toBundle()
        AppDetailsActivity.start(requireContext(), appId, options)
    }

    override fun onAppMainActionSelected(appId: String, action: AppMainAction) {
        viewModel.processAction(
            action = Action.OnAppMainAction(appId, action)
        )
    }

    override fun onExtraActionSelected(appId: String, extraAction: AppExtraAction) {
        viewModel.processAction(
            action = Action.OnAppExtraAction(appId, extraAction)
        )
    }

    private fun forceRefreshAppsList() {
        viewModel.processAction(
            action = Action.ForceRefreshAppsList
        )
    }

    private fun showAppCategoryFilter() {
        TODO("Not yet implemented")
    }
}