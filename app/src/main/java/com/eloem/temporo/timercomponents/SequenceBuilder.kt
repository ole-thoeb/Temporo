package com.eloem.temporo.timercomponents

import android.content.Context

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