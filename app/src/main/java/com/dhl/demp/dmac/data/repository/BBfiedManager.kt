package com.dhl.demp.dmac.data.repository

interface BBfiedManager {
    suspend fun requestApprove(appId: String): Boolean
}