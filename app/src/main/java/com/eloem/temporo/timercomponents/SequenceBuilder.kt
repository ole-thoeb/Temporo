package com.eloem.temporo.timercomponents

import android.content.Context
import java.lang.Error

interface IdProvider {
    fun newId(): Long
}

class SequenceBuilder(val context: Context, val sequence: List<EditComponent>) : IdProvider {
    override fun newId(): Long {
        return 1
    }


}

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