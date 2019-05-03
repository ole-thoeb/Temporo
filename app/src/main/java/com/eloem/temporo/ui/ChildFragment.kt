package com.eloem.temporo.ui

import androidx.fragment.app.Fragment

open class ChildFragment: Fragment() {
    val hostActivity: HostActivity get() = requireActivity() as HostActivity
}