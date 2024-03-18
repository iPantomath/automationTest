package com.dhl.demp.dmac.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dhl.demp.dmac.ext.exhaustive
import com.dhl.demp.dmac.ext.openLink
import com.dhl.demp.dmac.ext.showAppNotificationSettings
import com.dhl.demp.dmac.ext.viewBinding
import com.dhl.demp.dmac.model.DeviceOwner
import com.dhl.demp.dmac.model.SimOwner
import com.dhl.demp.dmac.ui.contacts.ContactsActivity
import com.dhl.demp.dmac.ui.settings.SettingsViewModel.Event
import com.dhl.demp.mydmac.LauncherPreference
import com.dhl.demp.mydmac.activity.AboutActivity
import com.dhl.demp.mydmac.activity.ChoosePasscodeActivity
import com.dhl.demp.mydmac.activity.EnterPasscodeActivity
import com.dhl.demp.mydmac.activity.LoginActivity
import com.dhl.demp.mydmac.activity.PersonalizationActivity
import com.dhl.demp.mydmac.utils.Constants
import com.dhl.demp.mydmac.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import mydmac.BuildConfig
import mydmac.R
import mydmac.databinding.FragmentSettingsBinding

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {
    companion object {
        fun newInstance(): SettingsFragment = SettingsFragment()
    }

    private val viewModel by viewModels<SettingsViewModel>()
    private val binding: FragmentSettingsBinding by viewBinding()

    private val testingActivityRouter: TestingActivityRouter by lazy(LazyThreadSafetyMode.NONE) {
        TestingActivityRouterImpl()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adjustViews()
        setViewListeners()
        initViewModel()
    }

    override fun onResume() {
        super.onResume()

        updateNotificationsEnabledState()
    }

    private fun updateNotificationsEnabledState() {
        val areNotificationsEnabled = NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()
        binding.notificationsEnabled.isChecked = areNotificationsEnabled
    }

    private fun adjustViews() {
        val autoReg = LauncherPreference.isAutoreg(context)

        if (viewModel.isTestingAvailable()) {
            binding.settingsTesting.visibility = View.VISIBLE
        }

        if(autoReg){
            binding.settingsLockCorporateApps.visibility = View.GONE
        }

        if (resources.getBoolean(R.bool.is_launcher_switchable)) {
            binding.settingsLauncher.visibility = View.VISIBLE
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val hasPasscode = viewModel.hasPasscode()
            
            binding.settingsChangePasscode.visibility = if (hasPasscode && !autoReg) View.VISIBLE else View.GONE
            binding.settingsSetPasscode.visibility = if (hasPasscode  || autoReg) View.GONE else View.VISIBLE
        }
    }

    private fun setViewListeners() {
        binding.settingsAbout.setOnClickListener {
            startActivity(Intent(requireContext(), AboutActivity::class.java))
        }
        binding.settingsWhatsNew.setOnClickListener {
            //TODO
        }
        binding.settingsTerms.setOnClickListener {
            openLink(
                    if(BuildConfig.BUILD_TYPE==Constants.BuildType.DEBUG){
                        Constants.Uris.TERMS_OF_SERVICE_TEST
                    }else if(BuildConfig.BUILD_TYPE==Constants.BuildType.UAT) {
                        Constants.Uris.TERMS_OF_SERVICE_UAT
                    }else{
                        Constants.Uris.TERMS_OF_SERVICE
                    })
        }
        binding.settingsPrivacyNotice.setOnClickListener {
            openLink(if(BuildConfig.BUILD_TYPE==Constants.BuildType.DEBUG){
                Constants.Uris.PRIVACY_NOTICE_TEST
            }else if(BuildConfig.BUILD_TYPE==Constants.BuildType.UAT) {
                Constants.Uris.PRIVACY_NOTICE_UAT
            }else{
                Constants.Uris.PRIVACY_NOTICE
            })
        }
        binding.settingsDisclaimer.setOnClickListener {
            openLink(Constants.Uris.DISCLAIMER)
        }
        binding.settingsContact.setOnClickListener {
            ContactsActivity.start(requireContext())
        }
        binding.settingsLauncher.setOnClickListener {
            PersonalizationActivity.start(requireContext())
        }

        binding.notificationsEnabledContainer.setOnClickListener {
            requireContext().showAppNotificationSettings()
        }

        binding.settingsLockCorporateApps.setOnClickListener {
            viewModel.onLockCorporateApps()
        }

        binding.settingsSetPasscode.setOnClickListener {
            activity?.let { hostActivity ->
                ChoosePasscodeActivity.start(hostActivity)
                hostActivity.finish()
            }

        }
        binding.settingsChangePasscode.setOnClickListener {
            showDialog(
                titleResId = R.string.change_passcode,
                messageResId = R.string.to_change_your_passcode,
                positiveButtonResId = android.R.string.ok,
                positiveAction = viewModel::onChangePasscode
            )
        }
        binding.settingsLogout.setOnClickListener {
            showDialog(
                titleResId = R.string.logout,
                messageResId = R.string.really_unregister,
                positiveButtonResId = R.string.logout,
                positiveAction = viewModel::onLogout
            )
        }
        binding.settingsTesting.setOnClickListener {
            testingActivityRouter.startTestingActivity(requireContext())
        }
        binding.deviceOwner.setOnClickListener {
            onSelectDeviceOwner()
        }
        binding.simOwner.setOnClickListener {
            onSelectSimOwner()
        }
    }

    private fun initViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.events.collect(::onEvent)
                }
                launch {
                    viewModel.deviceOwner.collect(::onDeviceOwnerChanged)
                }
                launch {
                    viewModel.simOwner.collect(::onSimOwnerChanged)
                }
            }
        }
    }

    private fun onEvent(event: Event) {
        when (event) {
            Event.ShowEnterPasscode -> showEnterPasscode()
            Event.ShowSetPasscode -> showSetPasscode()
            Event.ShowResetPasscode -> showResetPasscode()
            is Event.ShowLoginScreen -> showLogin(event.email)
        }.exhaustive
    }

    private fun onDeviceOwnerChanged(deviceOwner: DeviceOwner) {
        val deviceOwnerText = when (deviceOwner) {
            DeviceOwner.PRIVATE -> getString(R.string.owner_private)
            DeviceOwner.CORPORATE -> getString(R.string.owner_corporate)
        }

        binding.deviceOwner.text = deviceOwnerText
    }

    private fun onSimOwnerChanged(simOwner: SimOwner) {
        val simOwnerText = when (simOwner) {
            SimOwner.PRIVATE -> getString(R.string.owner_private)
            SimOwner.CORPORATE -> getString(R.string.owner_corporate)
            SimOwner.BOTH -> getString(R.string.owner_both)
        }

        binding.simOwner.text = simOwnerText
    }

    private fun showEnterPasscode() {
        activity?.let { hostActivity ->
            EnterPasscodeActivity.start(true, hostActivity)
            hostActivity.finish()
        }
    }

    private fun showLogin(email: String?) {
        Utils.showLoginScreen(context, email)

        activity?.finish()
    }

    private fun showSetPasscode() {
        //Copied from old code. In this case we show LoginActivity without any extra data. Why in this case we do it - ¯\_(ツ)_/¯
        val intent = Intent(context, LoginActivity::class.java)
        startActivity(intent)

        activity?.finish()
    }

    private fun showResetPasscode() {
        EnterPasscodeActivity.start(context, true)

        activity?.finish()
    }

    private fun showDialog(
        titleResId: Int,
        messageResId: Int,
        positiveButtonResId: Int,
        positiveAction: () -> Unit
    ) {
        val builder = AlertDialog.Builder(requireContext(), R.style.MyAlertDialogStyle)
            .setTitle(titleResId)
            .setMessage(messageResId)
            .setPositiveButton(positiveButtonResId) { _,_ -> positiveAction() }
            .setNegativeButton(android.R.string.cancel, null)

        val dialog = builder.create()


        dialog.show()
    }

    private fun onSelectDeviceOwner() {
        val items = listOf(R.string.owner_private, R.string.owner_corporate)

        showPopupMenu(binding.deviceOwner, items) { index ->
            val newDeviceOwner = when (index) {
                0 -> DeviceOwner.PRIVATE
                1 -> DeviceOwner.CORPORATE
                else -> throw IllegalArgumentException("Selected unknown device owner $index")
            }

            viewModel.onDeviceOwnerChanged(newDeviceOwner)
        }
    }

    private fun onSelectSimOwner() {
        val items = listOf(R.string.owner_private, R.string.owner_corporate, R.string.owner_both)

        showPopupMenu(binding.simOwner, items) { index ->
            val newSimOwner = when (index) {
                0 -> SimOwner.PRIVATE
                1 -> SimOwner.CORPORATE
                2 -> SimOwner.BOTH
                else -> throw IllegalArgumentException("Selected unknown sim owner $index")
            }

            viewModel.onSimOwnerChanged(newSimOwner)
        }
    }

    private fun showPopupMenu(view: View, items: List<Int>, onItemSelected: (Int) -> Unit) {
        val popup = PopupMenu(requireContext(), view)

        items.forEachIndexed { index, item ->
            popup.menu.add(0, index, index, item)
        }

        popup.setOnMenuItemClickListener { menuItem ->
            onItemSelected(menuItem.itemId)
            true
        }

        popup.show()
    }
}