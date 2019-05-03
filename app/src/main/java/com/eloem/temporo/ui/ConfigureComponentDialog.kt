package com.eloem.temporo.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialog
import androidx.core.app.DialogCompat
import androidx.fragment.app.DialogFragment
import com.eloem.temporo.R


class ConfigureComponentDialog @JvmOverloads constructor(context: Context, theme: Int = 0) : AppCompatDialog(context, theme) {


    companion object {
        const val TAG = "ConfigureComponentDialog"
    }
}