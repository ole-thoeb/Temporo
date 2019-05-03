package com.eloem.temporo.timercomponents

interface TimerComponent {
    var onTimerFinishedListener: ((Long) -> Unit)?
    var onTick: ((Long) -> Unit)?
    fun startTimer()
    fun cancelTimer()
}

open class CountdownTimerComponent(
    id: Long,
    title: String,
    showNextTitle: Boolean,
    val length: Long,
    val startSound: Int,
    val endSound: Int
): UiComponent(id, title, showNextTitle) {

    override fun init() {
        //nothing
    }

    override fun nextComponent(): Component {
        return next
    }
}

open class WaitComponent(
    id: Long,
    title: String,
    showNextTitle: Boolean
): UiComponent(id, title, showNextTitle), ButtonComponent {

    override fun init() {
        //nothing
    }

    override fun nextComponent(): Component {
        return next
    }

    override fun onButtonPressed(): Boolean {
        requireHandler().switchTo(nextComponent())
        return true
    }
}