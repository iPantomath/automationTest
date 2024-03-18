package com.dhl.demp.dmac.ext

import android.text.Spanned
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment

val <T> T.exhaustive: T
    get() = this

fun Fragment.showToast(text: String) {
    Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
}

fun Fragment.showToast(resId: Int) {
    Toast.makeText(requireContext(), resId, Toast.LENGTH_SHORT).show()
}

fun String.fromHtml(): Spanned = HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY)