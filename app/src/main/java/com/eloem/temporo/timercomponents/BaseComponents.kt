package com.eloem.temporo.timercomponents

import java.lang.UnsupportedOperationException
import kotlin.reflect.KProperty

sealed class Component(val id: Long) {
    var next: Component by TimerComponentLink()
    abstract fun nextComponent(): Component
    //called when switched to the component
    abstract fun init()

    var handler: TimerHandler? = null

    open fun findComponentById(searchId: Long): Component {
        return if (id == searchId) this
        else next.findComponentById(searchId)
    }

    companion object {
        const val NO_ID = 0L
    }
}

fun Component.requireHandler(): TimerHandler
        = handler ?: throw IllegalStateException("Component $this is not attached to a handler")

abstract class UiComponent(id: Long, val title: String, val showNextTitle: Boolean): Component(id)

abstract class BackgroundComponent(id: Long): Component(id)

object NoComponent: Component(NO_ID) {

    override fun init() {
        //nothing
    }

    override fun nextComponent(): Component {
        throw UnsupportedOperationException()
    }

    override fun findComponentById(searchId: Long): Component {
        return this
    }
}

interface ButtonComponent {
    fun onButtonPressed(): Boolean
    val allowListenerRemoval: Boolean
        get() = true
}

class TimerComponentLink {

    var component: Component? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Component {
        return component ?: NoComponent
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Component) {
        component = value
    }
}