package com.eloem.temporo.ui

import androidx.lifecycle.ViewModel
import com.eloem.temporo.timercomponents.Component
import com.eloem.temporo.timercomponents.TimerHandler

class DisplayViewModel : ViewModel() {
    private var handler: TimerHandler? = null

    fun getHandler(creator: () -> TimerHandler): TimerHandler {
        return handler ?: creator().also { handler = it }
    }

    var endMillis: Long? = null
}