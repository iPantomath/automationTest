package com.dhl.demp.dmac.data.repository

import android.content.Context
import com.dhl.demp.dmac.data.db.DmacDatabase
import com.dhl.demp.dmac.utils.DispatchersContainer
import com.dhl.demp.mydmac.utils.Utils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class LogoutDataCleanerImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val db: DmacDatabase,
    private val dispatchersContainer: DispatchersContainer
): LogoutDataCleaner {
    override fun cleanData(deleteFCMToken: Boolean) {
        GlobalScope.launch(dispatchersContainer.IO) {
            db.clearAllTables()
            Utils.cleanUpBeforeLogout(appContext, deleteFCMToken)
        }
    }
}