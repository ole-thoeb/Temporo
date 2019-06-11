package com.eloem.temporo.timercomponents

import com.eloem.temporo.util.ArithmeticOperation
import com.eloem.temporo.util.LogicOperation
import com.eloem.temporo.util.ifNull
import com.eloem.temporo.util.onFailure

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
open class GoTo(var targetAddress: Int): Instruction {

    init {
        require(targetAddress >= 0) { "address must be non negative, but is $targetAddress" }
    }

    override fun execute(executor: TimerExecutor) {
        executor.programCounter = targetAddress
    }

    override fun toString(): String = "GoTo: $targetAddress"
}

/**
 * Jump to targetAddress if condition is true
 */
class JumpIfTrue(val condition: String, targetAddress: Int): GoTo(targetAddress) {

    private var expression: LogicOperation? = null

    override fun execute(executor: TimerExecutor) {
        val calculatedValue = expression
            .ifNull { executor.buildLogicOperation(condition).onFailure { return } }
            .also { expression = it }
            .execute()
        if (calculatedValue) super.execute(executor)
        else executor.programCounter++
    }

    override fun toString(): String = "if $condition GoTo $targetAddress"
}

/**
 * Jump to targetAddress if condition is false
 */
class JumpIfFalse(val condition: String, targetAddress: Int): GoTo(targetAddress) {

    private var expression: LogicOperation? = null

    override fun execute(executor: TimerExecutor) {
        val calculatedValue = expression
            .ifNull { executor.buildLogicOperation(condition).onFailure { return } }
            .also { expression = it }
            .execute()
        if (!calculatedValue) super.execute(executor)
        else executor.programCounter++
    }

    override fun toString(): String = "if !$condition GoTo $targetAddress"
}

/**
 * Update or create new local variable
 */
class AssignVariable(val variable: String, val newValue: String): Instruction {

    private var expression: ArithmeticOperation? = null

    override fun execute(executor: TimerExecutor) {
        val calculatedValue = expression
            .ifNull { executor.buildArithmeticOperation(newValue).onFailure { return } }
            .also { expression = it }
            .execute()
        executor.localVariables[variable] = calculatedValue

        executor.programCounter++
    }

    override fun toString(): String = "Assign: $variable = $newValue"
}

class WaitForButton(
    override val title: String,
    override val showNextTitle: Boolean
): UiInstruction {

    override fun execute(executor: TimerExecutor) {
        setUiData(executor)
        executor.suspendTillButtonPressed()
        executor.programCounter++
    }

    override fun toString(): String = "WaitForButton(title = $title, showNextTitle = $showNextTitle)"
}

class RegisterButtonVariable(val variable: String) : Instruction {
    override fun execute(executor: TimerExecutor) {
        executor.localVariables[variable] = 0
        executor.registerButtonVariable(variable)
        executor.programCounter++
    }

    override fun toString(): String = "Register $variable for Button"
}


class UnregisterButtonVariable(val variable: String) : Instruction {
    override fun execute(executor: TimerExecutor) {
        executor.unregisterButtonVariable(variable)
        executor.programCounter++
    }

    override fun toString(): String = "Unregister $variable for Button"
}

data class RunTimer(
    override val title: String,
    override val showNextTitle: Boolean,
    override val length: Long
): TimerInstruction {

    override fun execute(executor: TimerExecutor) {
        setUiData(executor)
        executor.suspendTillTimerFinished()
        executor.startTimer(this)
        executor.programCounter++
    }

    //override fun toString(): String = "RunTimer(title = $title, showNextTitle = $showNextTitle, length = $length)"
}