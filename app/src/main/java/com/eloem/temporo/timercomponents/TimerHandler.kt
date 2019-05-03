package com.eloem.temporo.timercomponents

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

    fun next() {
        switchTo(currentComponent.nextComponent())
    }

    fun switchTo(component: Component) {
        detachButtonListener(currentComponent)

        currentComponent = component.also {
            it.handler = this
            it.init()
        }
        if (currentComponent is NoComponent) onFinishedListener?.invoke()
        else onNextComponentListener?.invoke(this, currentComponent)

        attachButtonListener(currentComponent)
    }

    private fun detachButtonListener(component: Component) {
        if (component is ButtonComponent && component.allowListenerRemoval) {
            buttonCallStack.remove(component)
        }
    }

    private fun attachButtonListener(component: Component) {
        if (component is ButtonComponent) {
            //remove when already in stack
            buttonCallStack.remove(component)
            //add on top
            buttonCallStack.add(component)
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