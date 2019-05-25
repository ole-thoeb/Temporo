package com.eloem.temporo.timercomponents

import java.lang.UnsupportedOperationException
import kotlin.reflect.KProperty

sealed class Component(val id: Long) {
    var next: Component by TimerComponentLink()
    abstract fun nextComponent(): Component

    //returns next component without changing any state
    abstract fun previewNext(): Component

    //called when switched to the component
    abstract fun init()

    var handler: TimerHandler? = null

    open fun findComponentById(searchId: Long): Component {
        return if (id == searchId) this
        else next.findComponentById(searchId)
    }

    override fun equals(other: Any?): Boolean {
        return other is Component && other.id == id
    }

    override fun hashCode(): Int = id.toInt()

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

    override fun previewNext(): Component = this

    override fun findComponentById(searchId: Long): Component {
        return this
    }
}

object NoUIComponent: UiComponent(NO_ID, "", false) {
    override fun init() {
        //nothing
    }

    override fun nextComponent(): Component {
        throw UnsupportedOperationException()
    }

    override fun previewNext(): Component = this

    override fun findComponentById(searchId: Long): Component {
        return this
    }
}

fun Component.isNotNoComponent(): Boolean = this != NoComponent && this != NoUIComponent

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