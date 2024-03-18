package com.dhl.demp.dmac.utils

import android.annotation.SuppressLint
import android.util.Log
import timber.log.Timber

class ProdTree : Timber.Tree() {
    @SuppressLint("LogNotTimber")
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return
        }

        when (priority) {
            Log.INFO -> Log.i(tag, message, t)
            Log.WARN -> Log.w(tag, message, t)
            Log.ERROR -> Log.e(tag, message, t)
        }
    }
}