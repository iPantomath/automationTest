package com.dhl.demp.dmac.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhl.demp.dmac.data.network.AppService
import com.dhl.demp.dmac.data.repository.LogoutDataCleaner
import com.dhl.demp.dmac.model.DeviceOwner
import com.dhl.demp.dmac.model.SimOwner
import com.dhl.demp.dmac.utils.DispatchersContainer
import com.dhl.demp.dmac.utils.LauncherPreferenceWrapper
import com.dhl.demp.mydmac.api.ApiFactory
import com.dhl.demp.mydmac.api.UnifiedApi
import com.dhl.demp.mydmac.api.request.UnregisterRequest
import com.dhl.demp.mydmac.obj.DeviceAndSimOwner
import com.dhl.demp.mydmac.utils.Utils
import dagger.Lazy
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mydmac.BuildConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val appService: AppService,
    private val dispatchersContainer: DispatchersContainer,
    private val launcherPreference: LauncherPreferenceWrapper,
    private val logoutDataCleaner: Lazy<LogoutDataCleaner>
) : ViewModel() {
    private val unifiedApi: UnifiedApi by lazy { ApiFactory.buildUnifiedApi() }

    private val _events = Channel<Event>(Channel.BUFFERED)
    val events: Flow<Event> = _events.receiveAsFlow()

    private val _deviceOwner = MutableStateFlow(DeviceOwner.PRIVATE)
    val deviceOwner: StateFlow<DeviceOwner> = _deviceOwner.asStateFlow()

    private val _simOwner = MutableStateFlow(SimOwner.PRIVATE)
    val simOwner: StateFlow<SimOwner> = _simOwner.asStateFlow()

    private val emptyApiCallback by lazy {
        object : Callback<Void> {
            override fun onFailure(call: Call<Void>?, t: Throwable?) {
                //nothing to do
            }

            override fun onResponse(call: Call<Void>?, response: Response<Void>?) {
                //nothing to do
            }
        }
    }

    init {
        loadSimAndDeviceOwner()
    }

    private fun loadSimAndDeviceOwner() {
        viewModelScope.launch {
            _simOwner.emit(launcherPreference.loadSimOwner())
            _deviceOwner.emit(launcherPreference.loadDeviceOwner())
        }
    }

    fun onLockCorporateApps() {
        viewModelScope.launch {
            launcherPreference.setLocked(true)

            _events.send(Event.ShowEnterPasscode)
        }
    }

    fun onChangePasscode() {
        viewModelScope.launch {
            //Set flag that we need to change passcode
            launcherPreference.setChangePasscode(true)

            val email = launcherPreference.getClientEmail()
            _events.send(
                if (email.isEmpty()) Event.ShowSetPasscode else Event.ShowResetPasscode
            )
        }
    }

    fun onLogout() {
        viewModelScope.launch {
            val id = launcherPreference.getId()
            val refreshToken = launcherPreference.getRefToken()

            val request = UnregisterRequest(id, refreshToken)
            unifiedApi.unregister(request, ApiFactory.getApiKey())
                .enqueue(emptyApiCallback)

            withContext(dispatchersContainer.IO) {
                logoutDataCleaner.get().cleanData(deleteFCMToken = true)
            }

            //show login screen
            val email = launcherPreference.getClientEmail()
            _events.send(Event.ShowLoginScreen(email = email))
        }
    }

    suspend fun hasPasscode(): Boolean {
        return launcherPreference.getLastPasscodeForAppId("", true) > 0L
    }

    fun isTestingAvailable(): Boolean = (BuildConfig.DEBUG
            || BuildConfig.BUILD_TYPE=="uat"
            || BuildConfig.FLAVOR.equals(Utils.FLAVOR_NAME_DMAC_TIP)
            || BuildConfig.FLAVOR.equals(Utils.FLAVOR_NAME_TIP_EXPRESS))


    fun onDeviceOwnerChanged(newDeviceOwner: DeviceOwner) {
        viewModelScope.launch {
            _deviceOwner.emit(newDeviceOwner)

            launcherPreference.saveDeviceOwner(newDeviceOwner)

            sendDeviceAndSimOwner(newDeviceOwner, null)
        }
    }

    private suspend fun sendDeviceAndSimOwner(
        newDeviceOwner: DeviceOwner?,
        newSimOwner: SimOwner?
    ) {
        val clientId = launcherPreference.getId()
        val deviceAndSimOwner = DeviceAndSimOwner(newDeviceOwner?.value, newSimOwner?.value)

        runCatching {
            appService.updateDeviceAndSimOwner(clientId, deviceAndSimOwner)
        }
    }

    fun onSimOwnerChanged(newSimOwner: SimOwner) {
        viewModelScope.launch {
            _simOwner.emit(newSimOwner)

            launcherPreference.saveSimOwner(newSimOwner)

            sendDeviceAndSimOwner(null, newSimOwner)
        }
    }

    sealed class Event {
        object ShowEnterPasscode : Event()
        object ShowSetPasscode : Event()
        object ShowResetPasscode : Event()
        class ShowLoginScreen(val email: String?) : Event()
    }
}