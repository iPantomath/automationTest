package com.dhl.demp.dmac.model

class AppInstallationInfo(
    val appId: String,
    val name: String,
    val version: String,
    val installationType: InstallationRoutineType,
    val source: String,
    val md5: String
)