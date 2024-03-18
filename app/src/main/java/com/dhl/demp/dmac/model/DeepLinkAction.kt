package com.dhl.demp.dmac.model

sealed class DeepLinkAction {
    data class ShowDetails(val appId: String) : DeepLinkAction()
    data class Open(val appId: String) : DeepLinkAction()
    data class Install(
        val appId: String,
        val uninstallBeforeInstallation: Boolean,
        val openAfterInstallation: Boolean
    ) : DeepLinkAction()
}