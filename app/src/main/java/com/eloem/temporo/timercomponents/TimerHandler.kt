package com.eloem.temporo.timercomponents

import android.util.Log
import org.jetbrains.anko.collections.forEachReversedByIndex

class TimerHandler(sequence: Component) {
    var currentComponent: Component = sequence
        private set(value) { field = value }

    private val buttonCallStack: MutableList<ButtonComponent> = mutableListOf()

    var onFinishedListener: (() -> Unit)? = null
        set(value) {
            field = value
            if (currentComponent is NoComponent) value?.invoke()
        }

    var onNextComponentListener: ((TimerHandler, Component) -> Unit)? = null
        set(value) {
            field = value
            value?.invoke(this, currentComponent)
        }

    var onButtonStackChangeListener: ((Int) -> Unit)? = null
        set(value) {
            field = value
            value?.invoke(buttonCallStack.size)
        }


    init {
        switchTo(sequence)
    }

    fun next() {
        switchTo(currentComponent.nextComponent())
    }

    fun switchTo(component: Component) {
        Log.d("handler", "switching to $component")
        detachButtonListener(currentComponent)

        currentComponent = component.also {
            it.handler = this
            it.init()
        }

        attachButtonListener(currentComponent)

        if (currentComponent is NoComponent) onFinishedListener?.invoke()
        else onNextComponentListener?.invoke(this, currentComponent)
    }

    fun previewNextUiComponent(): UiComponent {
        var preview = currentComponent
        for (i in 0..20) {
            preview = preview.previewNext()
            when (preview) {
                NoComponent -> return NoUIComponent
                currentComponent -> return NoUIComponent
                is UiComponent -> return preview
            }
        }
        return NoUIComponent
    }

    private fun detachButtonListener(component: Component) {
        if (component is ButtonComponent && component.allowListenerRemoval) {
            buttonCallStack.remove(component)
            onButtonStackChangeListener?.invoke(buttonCallStack.size)
        }
    }

    private fun attachButtonListener(component: Component) {
        if (component is ButtonComponent) {
            //remove when already in stack
            buttonCallStack.remove(component)
            //add on top
            buttonCallStack.add(component)
            onButtonStackChangeListener?.invoke(buttonCallStack.size)
        }
    }

    fun handleButtonPressed(): Boolean {
        var handled = false
        buttonCallStack.forEachReversedByIndex {
            if (it.onButtonPressed()) {
                handled = true
                return@forEachReversedByIndex
            }
        }
        return handled
    }
}