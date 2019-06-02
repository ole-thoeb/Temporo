package com.eloem.temporo.timercomponents

interface TimerExecutor {

    var programCounter: Int

    val localVariables: MutableMap<String, Int>

    fun evaluateCondition(condition: String): Boolean

    fun evaluateValue(valueExpression: String): Int

    fun nextUiInstruction(): UiInstruction?

    fun suspendTillEvent(event: ExecutorEvent)
    fun startTimer(length: Long)

    enum class ExecutorEvent { BUTTON, TIMER_FINISHED }

    var title: String
    var nextTitle: String
    var isShowingNext: Boolean

}

