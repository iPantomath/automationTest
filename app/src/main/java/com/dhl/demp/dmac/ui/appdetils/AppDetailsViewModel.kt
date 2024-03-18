package com.dhl.demp.dmac.ui.appdetils

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhl.demp.dmac.data.model.FetchAppsResult
import com.dhl.demp.dmac.data.repository.*
import com.dhl.demp.dmac.ext.exhaustive
import com.dhl.demp.dmac.model.*
import com.dhl.demp.dmac.utils.DispatchersContainer
import com.dhl.demp.dmac.utils.FLOW_STATE_VM_TIMEOUT
import com.dhl.demp.mydmac.obj.InstallationAppItem
import com.dhl.demp.mydmac.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mydmac.R
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AppDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val appContext: Context,
    private val appRepository: AppRepository,
    private val appStatusRepository: AppStatusRepository,
    private val appInstallManager: AppInstallManager,
    private val bbfiedManager: BBfiedManager,
    private val dispatchersContainer: DispatchersContainer,
) : ViewModel() {
    companion object {
        const val KEY_APP_ID = "app_id"
    }

    private val appId = savedStateHandle.get<String>(KEY_APP_ID)
        ?: throw IllegalStateException("$KEY_APP_ID parameter is required for AppDetailsViewModel")

    val appInfo: Flow<AppDetailsInfo> = createAppDetailsInfoFlow()

    private val _events = Channel<Event>(Channel.BUFFERED)
    val events: Flow<Event> = _events.receiveAsFlow()

    init {
        refreshAppDetails()
    }

    private fun createAppDetailsInfoFlow(): Flow<AppDetailsInfo> {
        return combine(
            appRepository.getAppExtendedInfo(appId),
            createAppsPresenceEventFlow(),
            createAppsInInstallProgressFlow(),
            ::appInfoCombine
        )
            .flowOn(dispatchersContainer.Default)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(FLOW_STATE_VM_TIMEOUT),
                null
            )
            .filterNotNull()
    }

    private fun createAppsInInstallProgressFlow(): Flow<AppInstallEvent?> {
        return flow {
            //We must to emmit some initial value
            emit(null)

            emitAll(
                appInstallManager.getAppInstallEventFlow(appId)
            )
        }
    }

    private fun createAppsPresenceEventFlow(): Flow<AppPresenceEvent?> {
        return flow {
            //We must to emmit some initial value
            emit(null)

            val packageName = appRepository.getAppPackageName(appId).orEmpty()

            emitAll(
                appStatusRepository.getAppPresenceEventFlow(packageName)
            )
        }
    }

    private fun appInfoCombine(
        appDetails: AppExtendedFullInfo,
        appPresenceEvent: AppPresenceEvent?,
        appInstallEvent: AppInstallEvent?
    ): AppDetailsInfo {
        val installStatus =
            Utils.appInstalledOrNot(appContext, appDetails.packageId, appDetails.version)
        val isAppInInstallProgress = appInstallManager.isAppInstalling(appDetails.appId)

        return AppDetailsInfo(
            appId = appDetails.appId,
            name = appDetails.name,
            version = appDetails.version,
            iconUrl = appDetails.iconUrl,
            releaseType = appDetails.releaseType,
            annotation = appDetails.annotation,
            screenshots = appDetails.screenshots,
            versionDate = appDetails.versionDate,
            description = appDetails.description,
            releaseNote = appDetails.releaseNote,
            website = appDetails.website,
            developedBy = appDetails.developedBy,
            supportContact = appDetails.supportContact,
            versions = appDetails.versions,
            tags = formatTags(appDetails.categories),
            installationDependencies = appDetails.installationDependencies,
            resolvedDependencies = calculateResolvedDependencies(appDetails.installationDependencies),
            mainAction = calculateMainAction(appDetails, installStatus, isAppInInstallProgress),
            extraActions = calculateExtraActions(appDetails, installStatus)
        )
    }

    fun processAction(action: Action) {
        when (action) {
            Action.OnAppMainAction -> onAppMainAction()
            is Action.OnAppExtraAction -> onAppExtraAction(action.extraAction)
        }.exhaustive
    }

    private fun onAppMainAction() {
        viewModelScope.launch {
            val action = appInfo.first().mainAction

            when (action) {
                AppMainAction.OpenLink -> openLink(appId)
                is AppMainAction.Install -> installApp(appId)
                AppMainAction.Installed -> launchApp(appId)
                is AppMainAction.Update -> installApp(appId)
                AppMainAction.Request -> requestApprove(appId)
                AppMainAction.Requested,
                AppMainAction.Absent,
                AppMainAction.Progress -> Unit //Nothing to do
            }.exhaustive
        }
    }

    private fun openLink(appId: String) {
        viewModelScope.launch {
            val link = appRepository.getAppWebLink(appId)
            if (link != null) {
                _events.send(Event.OpenLink(link))
            }
        }
    }

    private fun installApp(appId: String) {
        viewModelScope.launch {
            val dependenciesStatus = appRepository.getAppDependenciesStatus(appId)

            when (dependenciesStatus) {
                AppDependenciesStatus.Unresolvable ->
                    _events.send(Event.ShowErrorNotification(R.string.installation_sequence_not_available))
                is AppDependenciesStatus.Unresolved ->
                    _events.send(
                        Event.ShowInstallationSequenceApproveDialog(
                            appItem = appRepository.getInstallationAppItem(appId),
                            dependencyItems = dependenciesStatus.unresolvedDependencies.toTypedArray()
                        )
                    )
                AppDependenciesStatus.Resolved -> startAppInstallation()
            }.exhaustive
        }
    }

    private suspend fun startAppInstallation() {
        val installationInfo = appRepository.getAppInstallationInfo(appId)
        if (installationInfo == null) {
            Timber.e("Installation info for $appId not found")
            return
        }

        appInstallManager.installApp(installationInfo, launchAfterInstallation = false)
    }

    private fun requestApprove(appId: String) {
        viewModelScope.launch {
            _events.send(Event.ShowWaitDialog)
            val success = bbfiedManager.requestApprove(appId)
            _events.send(Event.HideWaitDialog)

            if (!success) {
                _events.send(Event.ShowErrorNotification(R.string.failed_request_approve))
            }
        }
    }

    private fun launchApp(appId: String) {
        viewModelScope.launch {
            val packageName = appRepository.getAppPackageName(appId)
            if (packageName != null) {
                _events.send(Event.LaunchApp(packageName))
            }
        }
    }

    private fun onAppExtraAction(extraAction: AppExtraAction) {
        viewModelScope.launch {
            val actionDetails =
                appRepository.getAppActionDetails(appId, extraAction.id) ?: return@launch

            if (actionDetails.message != null) {
                _events.send(Event.ShowToast(actionDetails.message))
            }

            withContext(dispatchersContainer.Default) {
                Utils.doAppAction(
                    appContext,
                    appId,
                    actionDetails.targetPackageId,
                    actionDetails.url
                )
            }
        }
    }

    private fun refreshAppDetails() {
        viewModelScope.launch {
            val result = appRepository.fetchAndSaveAppDetails(appId)

            when (result) {
                //Nothing to do on FetchAppsResult.SUCCESS
                FetchAppsResult.FAILED -> showErrorNotification(R.string.failed_load_app_details)
                FetchAppsResult.SERVER_UNAVAILABLE -> showErrorNotification(R.string.server_unavailable_error)
            }
        }
    }

    private suspend fun showErrorNotification(resId: Int) {
        _events.send(Event.ShowSnackbar(resId))
    }

    private fun formatTags(categories: Map<String, List<String>>): Map<String, String> {
        return categories
            .mapValues {
                it.value.joinToString(separator = ", ")
            }
            .toSortedMap()
    }

    private fun calculateResolvedDependencies(
        dependencies: List<InstallationDependencyInfo>
    ): Set<String> {
        return dependencies
            .filter { appStatusRepository.isAppDependencyResolved(it.packageId, it.version) }
            .map { it.packageId }
            .toSet()
    }

    private fun calculateMainAction(
        appInfo: AppExtendedFullInfo,
        installStatus: Int,
        isAppInInstallProgress: Boolean
    ): AppMainAction {
        if (!appInfo.isAvailable) {
            return AppMainAction.Absent
        }

        if (appInfo.isWebLink) {
            return AppMainAction.OpenLink
        }

        if (isAppInInstallProgress) {
            return AppMainAction.Progress
        }

        if (appInfo.isBBfied) {
            if (appInfo.bbfiedApproveState == BBfiedApproveState.VISIBLE) {
                return AppMainAction.Request
            } else if (appInfo.bbfiedApproveState != BBfiedApproveState.INSTALL_ALLOWED) {
                return AppMainAction.Requested
            }
        }

        when (installStatus) {
            Utils.APP_NOT_INSTALLED -> return AppMainAction.Install(appInfo.hasDependencies)
            Utils.APP_INSTALLED -> return AppMainAction.Installed
            Utils.APP_INSTALLED_OTHER_VERSION -> return AppMainAction.Update(appInfo.hasDependencies)
        }

        return AppMainAction.Absent
    }

    private fun calculateExtraActions(
        appInfo: AppExtendedFullInfo,
        installStatus: Int
    ): List<AppExtraAction> {
        if (installStatus == Utils.APP_NOT_INSTALLED) {
            return emptyList()
        }

        return appInfo.actions
            .filter(::isExtraActionAvailable)
    }

    private fun isExtraActionAvailable(
        extraAction: AppExtraAction
    ): Boolean {
        if (appId == extraAction.targetPackageId) {
            return true
        }

        return Utils.appInstalledOrNot(
            appContext,
            extraAction.targetPackageId,
            ""
        ) != Utils.APP_NOT_INSTALLED
    }

    sealed class Action {
        object OnAppMainAction : Action()
        class OnAppExtraAction(val extraAction: AppExtraAction) : Action()
    }

    sealed class Event {
        class OpenLink(val link: String) : Event()
        class LaunchApp(val packageName: String) : Event()
        class ShowToast(val text: String) : Event()
        class ShowSnackbar(val resId: Int) : Event()
        class ShowErrorNotification(val resId: Int) : Event()
        object ShowWaitDialog : Event()
        object HideWaitDialog : Event()
        class ShowInstallationSequenceApproveDialog(
            val appItem: InstallationAppItem,
            val dependencyItems: Array<InstallationAppItem>
        ) : Event()
    }
}