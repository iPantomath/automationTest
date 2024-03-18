package com.dhl.demp.dmac.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhl.demp.dmac.data.repository.AppInstallManager
import com.dhl.demp.dmac.data.repository.AppRepository
import com.dhl.demp.dmac.domain.deeplink_parser.DeepLinkParser
import com.dhl.demp.dmac.ext.exhaustive
import com.dhl.demp.dmac.ext.launchApp
import com.dhl.demp.dmac.model.DeepLinkAction.*
import com.dhl.demp.dmac.utils.LauncherPreferenceWrapper
import com.dhl.demp.mydmac.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val appRepository: AppRepository,
    private val appInstallManager: AppInstallManager,
    private val launcherPreferenceWrapper: LauncherPreferenceWrapper,
    private val deepLinkParser: DeepLinkParser
) : ViewModel() {
    var isApp = MutableLiveData<Boolean?>()
    val needSelfUpdate: Flow<Boolean>
        get() = appRepository.needSelfUpdate

    private val _events = Channel<Event>(Channel.BUFFERED)
    val events: Flow<Event> = _events.receiveAsFlow()

    fun processDeepLink(data: Uri) {
        val action = deepLinkParser.parseDeepLink(data) ?: return

        when (action) {
            is Install -> processInstallAction(action)
            is Open -> processOpenAction(action)
            is ShowDetails -> processShowDetailsAction(action)
        }.exhaustive
    }

    private fun processInstallAction(action: Install) {
        viewModelScope.launch {
            if (appRepository.hasApp(action.appId)) {
                //first show the app details
                _events.send(
                    Event.ShowAppDetails(action.appId)
                )

                val packageName = appRepository.getAppPackageName(action.appId) ?: action.appId
                if (action.uninstallBeforeInstallation && isAppInstalled(packageName)) {
                    _events.send(
                        Event.ShowUninstallRequest(packageName)
                    )
                } else {
                    val installationInfo = appRepository.getAppInstallationInfo(action.appId)
                    if (installationInfo != null) {
                        appInstallManager.installApp(installationInfo, action.openAfterInstallation)
                    }
                }
            }
        }
    }

    private fun isAppInstalled(packageName: String): Boolean {
        return Utils.getInstalledAppVersion(appContext, packageName) != null
    }

    private fun processOpenAction(action: Open) {
        viewModelScope.launch {
            val packageName = appRepository.getAppPackageName(action.appId) ?: action.appId
            appContext.launchApp(packageName)
        }
    }

    private fun processShowDetailsAction(action: ShowDetails) {
        viewModelScope.launch {
            if (appRepository.hasApp(action.appId)) {
                _events.send(
                    Event.ShowAppDetails(action.appId)
                )
            }else{
                isApp.value = true
            }
        }
    }

    fun onSelfUpgrade() {
        viewModelScope.launch {
            val selfAppId = appRepository.getAppIdByPackageName(appContext.packageName).orEmpty()
            val installationInfo = appRepository.getAppInstallationInfo(selfAppId)
            if (installationInfo == null) {
                Timber.e("Installation info for $selfAppId not found")
                return@launch
            }

            appInstallManager.installApp(installationInfo, launchAfterInstallation = true)
        }
    }

    fun onPostponeSelfUpgrade() {
        viewModelScope.launch(NonCancellable) {
            val postponeTo = System.currentTimeMillis() + UPGRADE_POSTPONE_PERIOD
            launcherPreferenceWrapper.setUpgradePostponedTo(postponeTo)
        }
    }

    companion object {
        val UPGRADE_POSTPONE_PERIOD = TimeUnit.DAYS.toMillis(1)
    }

    sealed class Event {
        class ShowAppDetails(val appId: String) : Event()
        class ShowUninstallRequest(val packageName: String) : Event()
    }
}