package com.eloem.temporo.util

import android.app.Activity
import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import java.util.*

fun <T: View> Activity.lazyView(@IdRes id: Int) = lazy { findViewById<T>(id) }

fun minuetSecondText(time: Long): String {
    val seconds = Math.ceil(time / 1000.0).toInt()
    val displaySeconds = seconds % 60
    val displayMinuets = (seconds - displaySeconds) / 60

    //val displaySecondsStr = displaySeconds.toString().run { if (length == 1) "0$this" else this }
    //val displayMinuetsStr = displayMinuets.toString().run { if (length == 1) "0$this" else this }

    return "${String.format("%02d", displayMinuets)}:${String.format("%02d", displaySeconds)}"
}

fun ViewGroup.addAsOnlyChild(child: View) {
    removeAllViews()
    addView(child)
}

fun <T> MutableList<T>.swap(pos1: Int, pos2: Int) {
    Collections.swap(this, pos1, pos2)
}

fun Context.getAttribute(resourceId: Int, resolveRef: Boolean = true): TypedValue {
    val tv = TypedValue()
    theme.resolveAttribute(resourceId, tv, resolveRef)
    return tv
}