package com.dhl.demp.dmac.model

sealed class AppMainAction {
    object Absent : AppMainAction()
    object OpenLink : AppMainAction()
    class Install(val hasDependencies: Boolean) : AppMainAction()
    object Progress : AppMainAction()
    object Installed : AppMainAction()
    class Update(val hasDependencies: Boolean) : AppMainAction()
    object Request : AppMainAction()
    object Requested : AppMainAction()

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (this is Install && other is Install) {
            return this.hasDependencies == other.hasDependencies
        }

        if (this is Update && other is Update) {
            return this.hasDependencies == other.hasDependencies
        }

        return false
    }

    override fun hashCode(): Int {
        //Not recommended to use AppMainAction as a key in Map or Hash
        return 0
    }
}