package com.dhl.demp.dmac.ui.appslist

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhl.demp.dmac.data.model.FetchAppsResult
import com.dhl.demp.dmac.data.repository.*
import com.dhl.demp.dmac.domain.appfilter.Filter
import com.dhl.demp.dmac.domain.appfilter.SearchFilter
import com.dhl.demp.dmac.domain.appfilter.forListType
import com.dhl.demp.dmac.ext.exhaustive
import com.dhl.demp.dmac.model.AppExtraAction
import com.dhl.demp.dmac.model.AppFullInfo
import com.dhl.demp.dmac.model.AppMainAction
import com.dhl.demp.dmac.model.BBfiedApproveState
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
class AppsListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val appContext: Context,
    private val appRepository: AppRepository,
    private val appStatusRepository: AppStatusRepository,
    private val appInstallManager: AppInstallManager,
    private val bbfiedManager: BBfiedManager,
    private val dispatchersContainer: DispatchersContainer,
) : ViewModel() {
    companion object {
        const val KEY_LIST_TYPE = "list_type"
    }

    private val _events = Channel<Event>(Channel.BUFFERED)
    val events: Flow<Event> = _events.receiveAsFlow()

    private val baseFilter: Filter<AppFullInfo>
    private val searchFilterFlow: MutableStateFlow<SearchFilter>
    val appsList: Flow<List<AppsListItem>>

    init {
        val listType = savedStateHandle.get<AppsListType>(KEY_LIST_TYPE)
            ?: throw IllegalStateException("$KEY_LIST_TYPE parameter is required for AppsListViewModel")
        baseFilter = Filter.forListType(listType)
        searchFilterFlow = MutableStateFlow(SearchFilter(baseFilter, searchText = ""))

        appsList = combine(
            appRepository.getAppsList(),
            searchFilterFlow,
            createAppsPresenceEventFlow(),
            createAppsInInstallProgressFlow(),
            ::appsListCombine
        )
            .flowOn(dispatchersContainer.Default)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(FLOW_STATE_VM_TIMEOUT),
                emptyList()
            )
    }

    private fun createAppsInInstallProgressFlow(): Flow<AppInstallEvent?> {
        return flow {
            //We must to emmit some initial value
            emit(null)

            emitAll(
                appInstallManager.getAppsInstallEventFlow()
            )
        }
    }

    private fun createAppsPresenceEventFlow(): Flow<AppPresenceEvent?> {
        return flow {
            //We must to emmit some initial value
            emit(null)

            emitAll(
                appStatusRepository.getAppsPresenceEventFlow()
            )
        }
    }

    private fun appsListCombine(
        appsList: List<AppFullInfo>,
        filter: Filter<AppFullInfo>,
        appPresenceEvent: AppPresenceEvent?,
        appInstallEvent: AppInstallEvent?
    ): List<AppsListItem> {
        return appsList
            .filter(filter::match)
            .map { app ->
                val isAppInInstallProgress = appInstallManager.isAppInstalling(app.appId)
                val installStatus = Utils.appInstalledOrNot(appContext, app.packageId, app.version)

                AppsListItem(
                    appId = app.appId,
                    packageId = app.packageId,
                    name = app.name,
                    version = app.version,
                    iconUrl = app.iconUrl,
                    releaseType = app.releaseType,
                    annotation = app.annotation,
                    mainAction = calculateMainAction(app, installStatus, isAppInInstallProgress),
                    extraActions = calculateExtraActions(app, installStatus)
                )
            }
    }

    private fun calculateMainAction(
        appFullInfo: AppFullInfo,
        installStatus: Int,
        isAppInInstallProgress: Boolean
    ): AppMainAction {
        if (!appFullInfo.isAvailable) {
            return AppMainAction.Absent
        }

        if (appFullInfo.isWebLink) {
            return AppMainAction.OpenLink
        }

        if (isAppInInstallProgress) {
            return AppMainAction.Progress
        }

        if (appFullInfo.isBBfied) {
            if (appFullInfo.bbfiedApproveState == BBfiedApproveState.VISIBLE) {
                return AppMainAction.Request
            } else if (appFullInfo.bbfiedApproveState != BBfiedApproveState.INSTALL_ALLOWED) {
                return AppMainAction.Requested
            }
        }

        when (installStatus) {
            Utils.APP_NOT_INSTALLED -> return AppMainAction.Install(appFullInfo.hasDependencies)
            Utils.APP_INSTALLED -> return AppMainAction.Installed
            Utils.APP_INSTALLED_OTHER_VERSION -> return AppMainAction.Update(appFullInfo.hasDependencies)
        }

        return AppMainAction.Absent
    }

    private fun calculateExtraActions(
        appFullInfo: AppFullInfo,
        installStatus: Int
    ): List<AppExtraAction> {
        if (installStatus == Utils.APP_NOT_INSTALLED) {
            return emptyList()
        }

        return appFullInfo.actions
            .filter { extraAction ->
                isExtraActionAvailable(appFullInfo.appId, extraAction)
            }
    }

    private fun isExtraActionAvailable(
        appId: String,
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

    fun processAction(action: Action) {
        when (action) {
            Action.ForceRefreshAppsList -> refetchApps(showProgress = true)
            is Action.SetSearchText -> onSearchTextChanged(action.searchText)
            is Action.OnAppMainAction -> onAppMainAction(action.appId, action.action)
            is Action.OnAppExtraAction -> onAppExtraAction(action.appId, action.extraAction)
            is Action.ScreenShown -> refetchApps(showProgress = action.isFirstShow)
        }.exhaustive
    }

    private fun refetchApps(showProgress: Boolean) {
        viewModelScope.launch {
            if (showProgress) {
                _events.send(Event.ShowProgress)
            }

            val result = appRepository.fetchAndSaveAppsList()

            if (showProgress) {
                _events.send(Event.HideProgress)
            }

            when (result) {
                //Nothing to do on FetchAppsResult.SUCCESS
                FetchAppsResult.FAILED -> showErrorNotification(R.string.failed_load_apps_list)
                FetchAppsResult.SERVER_UNAVAILABLE -> showErrorNotification(R.string.server_unavailable_error)
            }
        }
    }

    private suspend fun showErrorNotification(resId: Int) {
        _events.send(Event.ShowErrorNotification(resId))
    }

    private fun onSearchTextChanged(newSearchText: String) {
        searchFilterFlow.value = SearchFilter(baseFilter, newSearchText)
    }

    private fun onAppMainAction(appId: String, action: AppMainAction) {
        when (action) {
            AppMainAction.OpenLink -> openLink(appId)
            is AppMainAction.Install -> installApp(appId)
            AppMainAction.Installed -> launchApp(appId)
            is AppMainAction.Update -> installApp(appId)
            AppMainAction.Request -> requestApprove(appId)
            AppMainAction.Requested,
            AppMainAction.Absent,
            AppMainAction.Progress -> Unit//Nothing to do
        }.exhaustive
    }

    private fun onAppExtraAction(appId: String, extraAction: AppExtraAction) {
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
                AppDependenciesStatus.Resolved -> startAppInstallation(appId)
            }.exhaustive
        }
    }

    private suspend fun startAppInstallation(appId: String) {
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

    private fun openLink(appId: String) {
        viewModelScope.launch {
            val link = appRepository.getAppWebLink(appId)
            if (link != null) {
                _events.send(Event.OpenLink(link))
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

    sealed class Action {
        object ForceRefreshAppsList : Action()
        class SetSearchText(val searchText: String) : Action()
        class OnAppMainAction(val appId: String, val action: AppMainAction) : Action()
        class OnAppExtraAction(val appId: String, val extraAction: AppExtraAction) : Action()
        class ScreenShown(val isFirstShow: Boolean) : Action()
    }

    sealed class Event {
        object ShowProgress : Event()
        object HideProgress : Event()
        object ShowWaitDialog : Event()
        object HideWaitDialog : Event()
        class ShowErrorNotification(val resId: Int) : Event()
        class OpenLink(val link: String) : Event()
        class LaunchApp(val packageName: String) : Event()
        class ShowToast(val text: String) : Event()
        class ShowInstallationSequenceApproveDialog(
            val appItem: InstallationAppItem,
            val dependencyItems: Array<InstallationAppItem>
        ) : Event()
    }
}