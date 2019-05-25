package com.eloem.temporo.util

import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import androidx.lifecycle.Observer
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

class ObserveOnceNotNull<T>(private val onceOnChange: (T) -> Unit) : Observer<T?> {

    private var firstTime = true

    override fun onChanged(t: T?) {
        if (firstTime && t != null) {
            firstTime = false
            onceOnChange(t)
        }
    }
}

class OnTextChangedListener(private val onChange: (String) -> Unit) : TextWatcher {
    override fun afterTextChanged(s: Editable?) {
        //nothing
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        //nothing
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        onChange(s.toString())
    }

}

infix fun IntRange.shl(amount: Int) = IntRange(start - amount, endInclusive - amount)

infix fun IntRange.shr(amount: Int) = IntRange(start + amount, endInclusive + amount)