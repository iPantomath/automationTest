package com.dhl.demp.dmac.ui.inventory

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import mydmac.R

private const val URL = "https://dmac.dhl.com"

class InventoryFragment : Fragment(R.layout.fragment_inventory) {
    companion object {
        fun newInstance(): InventoryFragment = InventoryFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val webView = (view as? WebView) ?: return

        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()

        webView.loadUrl(URL)

        activity?.onBackPressedDispatcher?.addCallback(this.viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    isEnabled = false
                }
            }
        })
    }
}