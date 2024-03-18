package com.dhl.demp.dmac.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

fun <T> broadcastToFlow(
    context: Context,
    intentFilter: IntentFilter,
    mapper: (Intent) -> T
): Flow<T> = callbackFlow {
    val appContext = context.applicationContext

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val nextValue = mapper(intent)
            trySend(nextValue)
        }
    }
    appContext.registerReceiver(receiver, intentFilter)

    awaitClose {
        appContext.unregisterReceiver(receiver)
    }
}

fun <T> localBroadcastToFlow(
    context: Context,
    intentFilter: IntentFilter,
    mapper: (Intent) -> T
): Flow<T> = callbackFlow {
    val appContext = context.applicationContext

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val nextValue = mapper(intent)
            trySend(nextValue)
        }
    }
    LocalBroadcastManager.getInstance(appContext).registerReceiver(receiver, intentFilter)

    awaitClose {
        LocalBroadcastManager.getInstance(appContext).unregisterReceiver(receiver)
    }
}