package com.eloem.temporo.util.editorfactory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.NumberPicker
import com.eloem.temporo.R
import com.eloem.temporo.timercomponents.EditComponent
import com.eloem.temporo.timercomponents.EditCountdown
import com.eloem.temporo.timercomponents.EditLoop
import com.eloem.temporo.timercomponents.EditWait

interface EditorFactory {
    fun createEditorView(parent: ViewGroup): View
    val updatedComponent: EditComponent
}

class LoopEditFactory(
    private val component: EditLoop
) : EditorFactory {

    private var curView: View? = null

    override fun createEditorView(parent: ViewGroup): View {
        return LayoutInflater.from(parent.context)
            .inflate(R.layout.options_sequence_editor_loop, parent, false).apply {
                val timesTV: EditText = findViewById(R.id.editTextTimes)
                val unlimitedCheck: CheckBox = findViewById(R.id.checkUnlimited)
                val tillButtonCheck: CheckBox = findViewById(R.id.checkTillButton)

                unlimitedCheck.setOnCheckedChangeListener { buttonView, isChecked ->
                    tillButtonCheck.isEnabled = isChecked
                    timesTV.isEnabled = !isChecked
                }

                timesTV.setText(component.times.toString())
                unlimitedCheck.isChecked = component.mode ==
                        EditLoop.Mode.TILL_BUTTON || component.mode == EditLoop.Mode.UNLIMITED
                tillButtonCheck.isChecked = component.mode == EditLoop.Mode.TILL_BUTTON

            }.also { curView = it }
    }

    override val updatedComponent: EditLoop
        get() = curView?.let {
            val newMode = when {
                it.findViewById<CheckBox>(R.id.checkTillButton).isChecked -> EditLoop.Mode.TILL_BUTTON
                it.findViewById<CheckBox>(R.id.checkUnlimited).isChecked -> EditLoop.Mode.UNLIMITED
                else -> EditLoop.Mode.TIMES
            }
            component.apply {
                mode = newMode
                times = try {
                    it.findViewById<EditText>(R.id.editTextTimes).text.toString().toInt()
                } catch (e: NumberFormatException) {
                    EditLoop.DEFAULT_TIMES
                }
            }
        } ?: component

}

class WaitEditFactory(private val component: EditWait) : EditorFactory {

    private var curView: View? = null

    override fun createEditorView(parent: ViewGroup): View {
        return LayoutInflater.from(parent.context)
            .inflate(R.layout.options_sequence_editor_wait, parent, false).apply {
                findViewById<CheckBox>(R.id.checkUnlimited).isChecked = component.showNextTitle
                findViewById<EditText>(R.id.editTextTimes).setText(component.title)
            }.also { curView = it }
    }

    override val updatedComponent: EditWait
        get() = curView?.let {
            EditWait(component.id,
                it.findViewById<EditText>(R.id.editTextTimes).text.toString(),
                it.findViewById<CheckBox>(R.id.checkUnlimited).isChecked)
        } ?: component

}

class CountdownEditFactory(private val component: EditCountdown) : EditorFactory {

    private var curView: View? = null

    override fun createEditorView(parent: ViewGroup): View {
        return LayoutInflater.from(parent.context)
            .inflate(R.layout.options_sequence_editor_timer, parent, false).apply {

            val seconds = Math.ceil(component.length / 1000.0).toInt()
            val displaySeconds = seconds % 60
            val displayMinuets = (seconds - displaySeconds) / 60

            findViewById<NumberPicker>(R.id.minuitPicker).apply {
                maxValue = 60
                minValue = 0
                setFormatter { String.format("%02d", it) }
                value = displayMinuets
            }
            findViewById<NumberPicker>(R.id.secondPicker).apply {
                maxValue = 60
                minValue = 0
                setFormatter { String.format("%02d", it) }
                value = displaySeconds
            }
            findViewById<EditText>(R.id.editTextTimes).setText(component.title)
            findViewById<CheckBox>(R.id.checkUnlimited).isChecked = component.showNextTitle
        }.also { curView = it }
    }

    override val updatedComponent: EditCountdown
        get() = curView?.let {
            val length = it.findViewById<NumberPicker>(R.id.minuitPicker).value * 60000L +
                    it.findViewById<NumberPicker>(R.id.secondPicker).value * 1000
            EditCountdown(component.id,
                it.findViewById<EditText>(R.id.editTextTimes).text.toString(),
                it.findViewById<CheckBox>(R.id.checkUnlimited).isChecked,
                length,
                0,
                0)
        } ?: component

}