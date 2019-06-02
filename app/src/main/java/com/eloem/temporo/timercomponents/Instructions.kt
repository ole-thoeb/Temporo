package com.eloem.temporo.timercomponents

interface Instruction {

    fun execute(executor: TimerExecutor)
}

interface UiInstruction: Instruction {
    val title: String
    val showNextTitle: Boolean
}

fun UiInstruction.setUiData(executor: TimerExecutor) {
    executor.title = title
    executor.isShowingNext = if (showNextTitle) {
        val nextUi = executor.nextUiInstruction()
        if (nextUi != null) {
            executor.nextTitle = nextUi.title
            true
        } else {
            false
        }
    } else {
        false
    }
}

interface TimerInstruction: UiInstruction {
    val length: Long
}

/**
 * Jump to targetAddress
 */
open class GoTo(val targetAddress: Int): Instruction {

    init {
        require(targetAddress >= 0) { "address must be non negative, but is $targetAddress" }
    }

    override fun execute(executor: TimerExecutor) {
        executor.programCounter = targetAddress
    }
}

/**
 * Jump to targetAddress if condition is true
 */
class JumpIfTrue(val condition: String, targetAddress: Int): GoTo(targetAddress) {
    override fun execute(executor: TimerExecutor) {
        if (executor.evaluateCondition(condition)) super.execute(executor)
    }
}

/**
 * Jump to targetAddress if condition is false
 */
class JumpIfFalse(val condition: String, targetAddress: Int): GoTo(targetAddress) {
    override fun execute(executor: TimerExecutor) {
        if (!executor.evaluateCondition(condition)) super.execute(executor)
    }
}

/**
 * Update or create new local variable
 */
class AssigneVariable(val variable: String, val newValue: String): Instruction {
    override fun execute(executor: TimerExecutor) {
        executor.localVariables[variable] = executor.evaluateValue(newValue)

        executor.programCounter++
    }
}

class WaitForButton(
    override val title: String,
    override val showNextTitle: Boolean
): UiInstruction {

    override fun execute(executor: TimerExecutor) {
        setUiData(executor)
        executor.suspendTillEvent(TimerExecutor.ExecutorEvent.BUTTON)
        executor.programCounter++
    }
}

class RunTimer(
    override val title: String,
    override val showNextTitle: Boolean,
    override val length: Long
): TimerInstruction {

    override fun execute(executor: TimerExecutor) {
        setUiData(executor)
        executor.suspendTillEvent(TimerExecutor.ExecutorEvent.TIMER_FINISHED)
        executor.startTimer(length)
        executor.programCounter++
    }
}