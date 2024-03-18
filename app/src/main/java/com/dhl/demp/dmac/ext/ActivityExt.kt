package com.dhl.demp.dmac.ext

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction

inline fun AppCompatActivity.inFragmentTransaction(action: FragmentTransaction.() -> Unit) {
    this.supportFragmentManager
            .beginTransaction()
            .apply(action)
            .commit()
}