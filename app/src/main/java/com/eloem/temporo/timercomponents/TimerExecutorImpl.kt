package com.eloem.temporo.timercomponents

class TimerExecutorImpl : TimerExecutor {

    override var programCounter: Int = 0

    override val localVariables: MutableMap<String, Int> = mutableMapOf()

    override fun evaluateCondition(condition: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun evaluateValue(valueExpression: String): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun nextUiInstruction(): UiInstruction? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun suspendTillEvent(event: TimerExecutor.ExecutorEvent) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun startTimer(length: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override var title: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    override var nextTitle: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    override var isShowingNext: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}

}