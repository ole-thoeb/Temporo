package com.eloem.temporo.ui

import androidx.lifecycle.ViewModel
import com.eloem.temporo.timercomponents.EditComponent

class EditorViewModel : ViewModel() {
    var editSequence: MutableList<EditComponent> = mutableListOf()
}