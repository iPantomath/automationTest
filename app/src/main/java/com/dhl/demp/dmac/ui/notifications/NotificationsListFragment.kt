package com.dhl.demp.dmac.ui.notifications

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.flowWithLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import com.dhl.demp.dmac.ext.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import mydmac.R
import mydmac.databinding.FragmentNotificationsListBinding

@AndroidEntryPoint
class NotificationsListFragment : Fragment(R.layout.fragment_notifications_list) {
    companion object {
        fun newInstance(): NotificationsListFragment = NotificationsListFragment()
    }

    private val viewModel by viewModels<NotificationsListViewModel>()
    private val binding: FragmentNotificationsListBinding by viewBinding()
    private var adapter: NotificationsAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        adapter = null
    }

    private fun initViews() {
        binding.retryButton.setOnClickListener {
            adapter?.retry()
        }

        binding.swipeLayout.setOnRefreshListener {
            adapter?.refresh()
        }

        binding.notificationsList.setHasFixedSize(true)
        binding.notificationsList.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))

        adapter = NotificationsAdapter()
        binding.notificationsList.adapter = adapter?.withLoadStateFooter(
            footer = NotificationsLoadStateAdapter { adapter?.retry() }
        )

        adapter?.addLoadStateListener { loadState ->
            if (loadState.source.refresh is LoadState.Error) {
                Toast.makeText(requireContext(), R.string.failed_load_notifications, Toast.LENGTH_SHORT).show()
            }

            if (loadState.source.append is LoadState.Error) {
                Toast.makeText(requireContext(), R.string.failed_load_further_notifications, Toast.LENGTH_SHORT).show()
            }

            binding.retryButton.isVisible = (adapter?.itemCount == 0) && (loadState.source.refresh is LoadState.Error)
            binding.swipeLayout.isRefreshing = loadState.source.refresh is LoadState.Loading
        }
    }

    private fun initViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.notifications
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collectLatest { notifications ->
                    adapter?.submitData(notifications)
                }
        }
    }
}