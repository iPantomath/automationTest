package com.dhl.demp.dmac.data.repository

interface LogoutDataCleaner {
    fun cleanData(deleteFCMToken: Boolean)
}