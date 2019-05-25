package com.eloem.temporo.timercomponents

import androidx.annotation.CallSuper

abstract class BranchComponent(id: Long): BackgroundComponent(id) {
    var branchNext: Component by TimerComponentLink()

    override fun findComponentById(searchId: Long): Component {
        if (id == searchId) return this
        val n = next.findComponentById(searchId)
        return if (n !is NoComponent) n
        else branchNext.findComponentById(searchId)
    }
}

open class ConditionalBranchComponent(id: Long, val condition: () -> Boolean): BranchComponent(id) {
    override fun nextComponent(): Component {
        return if (condition()) branchNext
        else next
    }

    override fun previewNext(): Component {
        return if (condition()) branchNext
        else next
    }

    override fun init() {
        //nothing
    }
}

abstract class LoopComponent(id: Long) : BranchComponent(id) {

    var breakingLoop = false

    @CallSuper
    override fun init() {
        breakingLoop = false
    }

    open fun breakLoop() {
        requireHandler().switchTo(branchNext)
    }

    enum class Mode { UNLIMITED, TIMES, BUTTON_PRESS, ADVANCED }
}

open class UnlimitedLoop(id: Long): LoopComponent(id) {

    override fun nextComponent(): Component {
        return next
    }

    override fun previewNext(): Component {
        return next
    }
}

open class TimesLoop(id: Long, val times: Int): LoopComponent(id) {

    private var mutableTimes = times
    private var leftLoop = false

    override fun init() {
        super.init()
        if (leftLoop) {
            mutableTimes = times
            leftLoop = false
        }
    }

    override fun nextComponent(): Component {
        return if (mutableTimes-- > 0) next
        else {
            leftLoop = true
            branchNext
        }
    }

    override fun previewNext(): Component {
        return if (mutableTimes > 0) next
        else branchNext
    }

}

open class ButtonLoop(id: Long): LoopComponent(id), ButtonComponent {

    private var breakNext = false
    private var leftLoop = false

    override fun init() {
        super.init()
        if (leftLoop) {
            breakNext = false
        }
    }

    override fun nextComponent(): Component {
        return if (breakNext) {
            leftLoop = true
            branchNext
        }
        else next
    }

    override fun previewNext(): Component {
        return next
    }

    override fun onButtonPressed(): Boolean {
        breakNext = true
        return true
    }

    override val allowListenerRemoval: Boolean
        get() = breakNext || breakingLoop

}