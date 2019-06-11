package com.eloem.temporo.timercomponents

import com.eloem.temporo.util.ArithmeticOperation
import com.eloem.temporo.util.LogicOperation
import com.eloem.temporo.util.Optional
import com.eloem.temporo.util.ParsError

interface TimerExecutor {

    val instructions: List<Instruction>

    var programCounter: Int

    val localVariables: MutableMap<String, Int>

    //fun evaluateCondition(condition: String): Boolean

    //fun evaluateValue(valueExpression: String): Int

    fun buildArithmeticOperation(expression: String): Optional<ArithmeticOperation, ParsError>

    fun buildLogicOperation(expression: String): Optional<LogicOperation, ParsError>

    fun nextUiInstruction(): UiInstruction? {
        val maxSteps = 20
        var steps = 0
        var i = programCounter + 1
        while (i < instructions.size && steps < maxSteps) {
            val instruction = instructions[i]
            if (instruction is UiInstruction) {
                return instruction
            } else if (instruction is GoTo) {
                i = instruction.targetAddress
                continue
            }
            steps++
            i++
        }
        return null
    }

    //fun suspendTillEvent(event: ExecutorEvent)

    fun suspendTillButtonPressed()
    fun suspendTillTimerFinished()
    fun notifyButtonPressed()
    fun notifyTimerFinished()
    val isSuspended: Boolean

    fun startTimer(timerInstruction: TimerInstruction)

    fun registerButtonVariable(variable: String)
    fun unregisterButtonVariable(variable: String)

    //enum class ExecutorEvent { BUTTON, TIMER_FINISHED }

    var title: String
    var nextTitle: String
    var isShowingNext: Boolean
}