package com.dhl.demp.dmac.domain.deeplink_parser

import android.net.Uri
import com.dhl.demp.dmac.model.DeepLinkAction

interface DeepLinkParser {
    fun parseDeepLink(data: Uri): DeepLinkAction?
}