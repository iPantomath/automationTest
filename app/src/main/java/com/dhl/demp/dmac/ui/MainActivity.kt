package com.dhl.demp.dmac.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.dhl.demp.dmac.ext.exhaustive
import com.dhl.demp.dmac.ext.inFragmentTransaction
import com.dhl.demp.dmac.ui.MainActivityViewModel.Event
import com.dhl.demp.dmac.ui.appdetils.AppDetailsActivity
import com.dhl.demp.dmac.ui.appslist.AppsListFragment
import com.dhl.demp.dmac.ui.appslist.AppsListType
import com.dhl.demp.dmac.ui.settings.SettingsFragment
import com.dhl.demp.dmac.utils.LauncherPreferenceWrapper
import com.dhl.demp.mydmac.activity.EnterPasscodeActivity
import com.dhl.demp.mydmac.analytics.Analytics
import com.dhl.demp.mydmac.installer.AppInstallerService
import com.dhl.demp.mydmac.service.ReportDeviceInfoService
import com.dhl.demp.mydmac.utils.Constants
import com.dhl.demp.mydmac.utils.Constants.NotificationType
import com.dhl.demp.mydmac.utils.Utils
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import mydmac.BuildConfig
import mydmac.R
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {
    @Inject
    lateinit var launcherPreference: LauncherPreferenceWrapper

    private val viewModel by viewModels<MainActivityViewModel>()

    companion object {
        @JvmStatic
        @JvmOverloads
        fun start(context: Context, flags: Int? = null) {
            val intent = Intent(context, MainActivity::class.java)
            if (flags != null) {
                intent.setFlags(flags)
            }
            context.startActivity(intent)
        }
    }

    private val installEventsReceiver by lazy { InstallEventsReceiver() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val token = launcherPreference.getToken()
            val isChangePasscode = launcherPreference.isChangePasscode()

            if (isChangePasscode) {
                EnterPasscodeActivity.start(this@MainActivity, true)
                finish()
            } else if (token.isEmpty()) {
                onUserNotLoggedIn()
            } else {
                initViews()
                initViewModel()

                val isFirstStart = savedInstanceState == null
                if (isFirstStart && intent != null) {
                    processIntentData(intent)
                }

                Analytics.sendAppInfo()
            }
        }

 //       initViewModel()
    }

    private fun initViewModel() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.needSelfUpdate
                        .filter { it }
                        .collect {
                            showUpgradeDialog()
                        }
                }
                launch {
                    viewModel.events.collect(::onEvent)
                }
            }
        }
    }

    private fun showUpgradeDialog() {
        AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
            .setMessage(R.string.upgrade_dialog_text)
            .setPositiveButton(R.string.upgrade) { _, _ ->
                viewModel.onSelfUpgrade()
            }
            .setNegativeButton(R.string.postpone) { dialog, _ ->
                viewModel.onPostponeSelfUpgrade()
                dialog.dismiss()
            }
            .setCancelable(false)
            .create()
            .show()
    }

    private fun onEvent(event: Event) {
        when (event) {
            is Event.ShowAppDetails -> AppDetailsActivity.start(this, event.appId)
            is Event.ShowUninstallRequest -> showUninstallRequest(event.packageName)
        }.exhaustive
    }

    private fun showUninstallRequest(packageName: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }

    override fun onNewIntent(newIntent: Intent) {
        super.onNewIntent(newIntent)

        processIntentData(newIntent)
    }

    private fun onUserNotLoggedIn() {
        Utils.startLoginActivity(this)
        finish()
    }

    private fun initViews() {
        setSupportActionBar(toolbar)

        if(BuildConfig.FLAVOR.equals("express")
                || BuildConfig.FLAVOR.equals("dhl")
                || BuildConfig.FLAVOR.equals("dmac")
                || BuildConfig.FLAVOR.equals("dmacTip")
                || BuildConfig.FLAVOR.equals("expressTip")
                || BuildConfig.FLAVOR.equals("tca")){
            toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.toolbar_title_color))
            val appName = SpannableStringBuilder(R.string.toolbar_title.toString())
            appName.setSpan(
                    ForegroundColorSpan(Color.RED),
                    0, // start
                    appName.length, // end
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
//            val env = SpannableString(getEnvironmentLabel())
        }

        bottomNavView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.tab_internal -> showFragment(
                    getString(R.string.app_name) /*+ getEnvironmentLabel()*/,
                    AppsListFragment.newInstance(AppsListType.INTERNAL_APPS)
                )
                R.id.tab_public -> showFragment(
                    R.string.public_apps,
                    AppsListFragment.newInstance(AppsListType.PUBLIC_APPS)
                )
                R.id.tab_installed -> showFragment(
                    R.string.installed_apps,
                    AppsListFragment.newInstance(AppsListType.INSTALLED_APPS)
                )
                //R.id.tab_notifications -> showFragment(R.string.notifications, NotificationsListFragment.newInstance())
                //R.id.tab_inventory -> showFragment(R.string.inventory_title, InventoryFragment.newInstance())
                R.id.tab_settings -> showFragment(R.string.settings, SettingsFragment.newInstance())
            }

            true
        }

        //initial tab is "Internal" apps
        showFragment(
            getString(R.string.app_name) /*+ getEnvironmentLabel()*/,
            AppsListFragment.newInstance(AppsListType.INTERNAL_APPS)
        )
    }

    private fun processIntentData(newIntent: Intent) {
        newIntent.data?.let(viewModel::processDeepLink)
        processPushNotificationData(newIntent.extras)
        viewModel.isApp.observe(this,  Observer { isApp ->
            isApp?.let{
                if(isApp == true) {
                    viewModel.isApp.value = false
                    Toast.makeText(getApplicationContext(), "Application was not found", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun processPushNotificationData(extras: Bundle?) {
        if (extras?.containsKey(Constants.EXTRA_NOTIFICATION_TYPE) != true) {
            return
        }

        val notificationType = extras.getString(Constants.EXTRA_NOTIFICATION_TYPE)
        if (notificationType == NotificationType.DMAC_ACTION) {
            val action = extras.getString(Constants.EXTRA_ACTION)?.lowercase()

            when (action) {
                Constants.NotificationAction.OPEN,
                Constants.NotificationAction.UPDATE_APP -> {
                    val appId = extras.getString(Constants.EXTRA_APP_ID)
                    if (appId != null) {
                        AppDetailsActivity.start(this, appId)
                    }
                }
            }
        }
    }

    private fun showFragment(titleId: Int, fragment: Fragment) {
        setTitle(titleId)

        inFragmentTransaction {
            replace(R.id.fragmentContainer, fragment)
        }
    }

    private fun showFragment(title: String, fragment: Fragment) {
        setTitle(title)

        inFragmentTransaction {
            replace(R.id.fragmentContainer, fragment)
        }
    }

    override fun onResume() {
        super.onResume()

        ReportDeviceInfoService.start(this)

        lifecycleScope.launch {
            if (isAppLocked()) {
                EnterPasscodeActivity.start(false, this@MainActivity)
                finish()
            }
        }
    }

    private suspend fun isAppLocked(): Boolean = launcherPreference.getLocked()

    override fun onStart() {
        super.onStart()

        val intentFilter = IntentFilter(AppInstallerService.ACTION_APP_INSTALL_EVENT)
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(installEventsReceiver, intentFilter)
    }

    override fun onStop() {
        super.onStop()

        LocalBroadcastManager.getInstance(this).unregisterReceiver(installEventsReceiver)
    }

    private fun onInstallEvent(event: AppInstallerService.Event) {
        when (event.type) {
            AppInstallerService.Event.TYPE_STARTING_DOWNLOAD ->
                Snackbar.make(rootView, R.string.starting_download, Snackbar.LENGTH_LONG).show()
            AppInstallerService.Event.TYPE_FILE_CORRUPTED ->
                Snackbar.make(rootView, R.string.file_corrupted, Snackbar.LENGTH_LONG).show()
            AppInstallerService.Event.TYPE_MD5_DONOT_MATCH ->
                Snackbar.make(rootView, R.string.md5_donot_match, Snackbar.LENGTH_LONG).show()
        }
    }

    inner class InstallEventsReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val event = AppInstallerService.Event(intent.extras)
            onInstallEvent(event)
        }
    }

//    private fun getEnvironmentLabel(): String {
//        return when (BuildConfig.BUILD_TYPE) {
//            Constants.BuildType.DEBUG -> " TEST"
//            Constants.BuildType.UAT -> " UAT"
//            else -> ""
//        }
//    }
}
