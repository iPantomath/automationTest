package com.dhl.demp.dmac.ui.settings

import android.content.Context
import com.dhl.demp.mydmac.activity.TestingActivity

class TestingActivityRouterImpl : TestingActivityRouter {
    override fun startTestingActivity(context: Context) {
        TestingActivity.start(context)
    }
}