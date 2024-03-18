package com.dhl.demp.dmac.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class DispatchersContainer(
    val Main: CoroutineDispatcher = Dispatchers.Main,
    val MainImmediate: CoroutineDispatcher = Dispatchers.Main.immediate,
    val IO: CoroutineDispatcher = Dispatchers.IO,
    val Default: CoroutineDispatcher = Dispatchers.Default
)