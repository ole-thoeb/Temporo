package com.eloem.temporo.ui

import androidx.lifecycle.ViewModel
import com.eloem.temporo.timercomponents.EditSequence

class EditorViewModel : ViewModel() {
    private var editSequence: EditSequence? = null

    fun getEditSequence(creator: () -> EditSequence): EditSequence {
        return editSequence ?: creator().also { editSequence = it }
    }
}