package com.eloem.temporo.timercomponents

import com.eloem.temporo.util.StringCounter
import com.eloem.temporo.util.exhaustive

fun editComponentsToSequence(editComponents: List<EditComponent>): Component {
    require(editComponents.isNotEmpty()) { "list can't be empty" }
    var curMaxId = editComponents.maxBy { it.id }?.id ?: Component.NO_ID

    val newIdFun = { ++curMaxId }

    val componentCache = mutableMapOf<EditComponent, Component>()
    val sequence: Component = editComponents.first().toComponent(newIdFun)
    var lastComponent = sequence

    fun asComponent (c: EditComponent): Component {
        return componentCache[c] ?: c.toComponent(newIdFun).also {
            componentCache[c] = it
        }
    }

    fun convertAndAddComponent(c: EditComponent) {
        val convertedC = asComponent(c)
        lastComponent.next = convertedC
        lastComponent = convertedC
    }

    editComponents.forEachIndexed { index, editComponent ->
        when(editComponent) {
            is EndBranchMarker -> if (index + 1 < editComponents.size) {
                componentCache[editComponent.associatedBranchComponent]?.let {
                    if (it is BranchComponent) it.branchNext = asComponent(editComponent)
                }
            }

            /*is EditLoop -> {
                editComponent.toComponent(newIdFun).let {
                    lastComponent.next = it
                    lastComponent = it
                    componentCache[editComponent] = it
                }
            }*/
            else -> asComponent(editComponent).let {
                lastComponent.next = it
                lastComponent = it
            }
        }
    }
    return sequence
}

fun DataSequence.toRuntimeSequence(): Component {
    require(isNotEmpty()) { "there must be more than zero components" }

    val componentCache = mutableMapOf<Long, Component>()

    fun asComponent (c: DataComponent): Component {
        return componentCache[c.id] ?: c.toRuntimeComponent().also {
            componentCache[c.id] = it
        }
    }

    val sequence: Component = asComponent(first())
    var lastComponent = sequence

    /*fun convertAndAddComponent(c: EditComponent) {
        val convertedC = asComponent(c)
        lastComponent.next = convertedC
        lastComponent = convertedC
    }*/

    fun convertToComponent(index: Int, dataComponent: DataComponent): Component {
        return when(dataComponent) {
            is DataComponentMarker -> componentCache[dataComponent.associatedBranchComponentId]?.also {
                if (it is BranchComponent) {
                    if (index + 1 < size) {
                        it.branchNext = convertToComponent(index + 1, this[index + 1])
                    }
                }
            } ?: throw Error()

            else -> asComponent(dataComponent)
        }
    }

    forEachIndexed { index, dataComponent ->
        if (index != 0) {
            convertToComponent(index, dataComponent).also {
                //dont override connections set because of looping
                if (lastComponent.next == NoComponent) {
                    lastComponent.next = it
                }
                lastComponent = it
            }
        }
    }

    return sequence
}

data class LoopBuildData(val loopStart: Int, val jumpOut: GoTo, val teardown: List<Instruction> = emptyList())

@Suppress("IMPLICIT_CAST_TO_ANY")
fun DataSequence.toInstructions(): List<Instruction> {
    require(isNotEmpty()) { "there must be more than zero components" }
    val instructions: MutableList<Instruction> = mutableListOf()
    val loopMap: MutableMap<Long, LoopBuildData> = mutableMapOf()
    val variableNameGenerator = StringCounter("autoGenVariable")
    forEach { component ->
        when(component) {
            is DataComponentLoop -> {
                when(component.mode) {
                    LoopComponent.Mode.BUTTON_PRESS -> {
                        val indexVariableName = variableNameGenerator.next()
                        val buttonVariableName = variableNameGenerator.next()
                        instructions.apply {
                            add(AssignVariable(indexVariableName, "0"))
                            add(RegisterButtonVariable(buttonVariableName))
                            val loopStart = size
                            add(AssignVariable(indexVariableName, "$indexVariableName + 1"))
                            val jump = JumpIfTrue("$buttonVariableName == 1", 0)
                            add(jump)
                            loopMap[component.id] = LoopBuildData(loopStart, jump, listOf(UnregisterButtonVariable(buttonVariableName)))
                        }
                    }
                    LoopComponent.Mode.TIMES -> {
                        val indexVariableName = variableNameGenerator.next()
                        instructions.apply {
                            add(AssignVariable(indexVariableName, "0"))
                            val loopStart = size
                            add(AssignVariable(indexVariableName, "$indexVariableName + 1"))
                            val jump = JumpIfFalse("$indexVariableName <= ${component.times}", 0)
                            add(jump)
                            loopMap[component.id] = LoopBuildData(loopStart, jump)
                        }
                    }
                    LoopComponent.Mode.UNLIMITED -> {
                        val indexVariableName = variableNameGenerator.next()
                        instructions.apply {
                            add(AssignVariable(indexVariableName, "0"))
                            val loopStart = size
                            add(AssignVariable(indexVariableName, "$indexVariableName + 1"))
                            //need a jump for BuildData
                            val jump = JumpIfFalse("true", 0)
                            loopMap[component.id] = LoopBuildData(loopStart, jump)
                        }
                    }
                    LoopComponent.Mode.ADVANCED -> throw Error()
                }
            }
            is DataComponentMarker -> {
                loopMap[component.associatedBranchComponentId]?.let {
                    instructions.add(GoTo(it.loopStart))
                    it.jumpOut.targetAddress = instructions.size
                    instructions.addAll(it.teardown)
                }
            }
            is DataComponentWait -> {
                instructions.add(WaitForButton(component.title, component.showNextTitle))
            }
            is DataComponentCountdown -> {
                instructions.add(RunTimer(component.title, component.showNextTitle, component.length))
            }
        }.exhaustive()
    }
    return instructions
}