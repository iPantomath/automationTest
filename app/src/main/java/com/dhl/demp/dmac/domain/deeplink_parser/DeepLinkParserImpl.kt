package com.dhl.demp.dmac.domain.deeplink_parser

import android.net.Uri
import com.dhl.demp.dmac.di.qualifier.DeepLinkScheme
import com.dhl.demp.dmac.model.DeepLinkAction
import com.dhl.demp.mydmac.utils.Constants.*
import javax.inject.Inject

class DeepLinkParserImpl @Inject constructor(
    @DeepLinkScheme private val deepLinkScheme: String
) : DeepLinkParser {
    override fun parseDeepLink(data: Uri): DeepLinkAction? {
        return if (data.scheme == deepLinkScheme) {
            parseAsDeepLink(data)
        } else {
            parseAsAppLink(data)
        }
    }

    private fun parseAsDeepLink(data: Uri): DeepLinkAction? {
        /**
            Format of deep links:
            1) dmac://apps/{appID}
            2) dmac://{appID}
            {appID} is optional
         **/
        val appId = if (data.host == DEEP_LINK_HOST_NAME) {
            //This is dmac://apps/{appID} case
            data.pathSegments.firstOrNull()
        } else {
            //This is dmac://{appID} case, use host as appId
            data.host
        }

        if (appId.isNullOrBlank()) {
            return null
        }

        val install = "true".equals(data.getQueryParameter(PARAM_INSTALL), true)
        val uninstallRequired = "true".equals(data.getQueryParameter(PARAM_UNINSTALL_REQUIRED), true)

        return if (install) {
            DeepLinkAction.Install(appId, uninstallRequired, openAfterInstallation = true)
        } else {
            DeepLinkAction.ShowDetails(appId)
        }
    }

    private fun parseAsAppLink(data: Uri): DeepLinkAction? {
        /**
        Format of applinks:
        https://applinks.dhl.com/dmac/{appID}?action=[detail|install|open]

        {appID} is optional(applink will be just https://applinks.dhl.com/dmac)

        Default action is "detail"

        In case of "action=install" extra optional parameters can be presented: uninstall_required=[true|false]&open_app=[true|false]
        uninstall_required - uninstall app before new installation
        open_app - open app after new installation
        Example: https://applinks.dhl.com/dmac/{appID}?action=install&uninstall_required=true&open_app=true
         **/


        val appId = if (data.pathSegments.size >= 2) {
            data.pathSegments.get(1)
        } else {
            null
        }

        if (appId.isNullOrBlank()) {
            return null
        }

        //AppLinkAction.DETAIL is default action
        val action = data.getQueryParameter(PARAM_ACTION) ?: AppLinkAction.DETAIL

        return when (action) {
            AppLinkAction.DETAIL -> DeepLinkAction.ShowDetails(appId)
            AppLinkAction.INSTALL -> {
                val uninstallRequired = "true".equals(data.getQueryParameter(PARAM_UNINSTALL_REQUIRED), true)
                val openApp = "true".equals(data.getQueryParameter(PARAM_OPEN_APP), true)

                DeepLinkAction.Install(appId, uninstallRequired, openApp)
            }
            AppLinkAction.OPEN -> DeepLinkAction.Open(appId)
            else -> null
        }
    }
}