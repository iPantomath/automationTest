package com.dhl.demp.dmac.ui.settings

import android.content.Context
import com.dhl.demp.mydmac.activity.TestingActivity
import com.dhl.demp.mydmac.utils.Utils
import mydmac.BuildConfig
import kotlin.IllegalStateException

class TestingActivityRouterImpl: TestingActivityRouter {
    override fun startTestingActivity(context: Context) {
        if((BuildConfig.FLAVOR.equals(Utils.FLAVOR_NAME_DMAC_TIP))
                || (BuildConfig.FLAVOR.equals(Utils.FLAVOR_NAME_TIP_EXPRESS))){
            TestingActivity.start(context)
        }else {
            throw IllegalStateException("TestingActivity is not supported")
        }
    }
}