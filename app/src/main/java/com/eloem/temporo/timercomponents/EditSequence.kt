package com.eloem.temporo.timercomponents

import java.lang.Error

class EditSequence(var id: Long, var title: String, val editComponents: MutableList<EditComponent>) {

    companion object {
        val EMPTY = EditSequence(0, "", mutableListOf())
    }
}

fun EditSequence.toDataSequence(): DataSequence {
    var maxId = editComponents.maxBy { it.id }?.id ?: Component.NO_ID
    val newId = { ++maxId }

    return DataSequence(id, title, editComponents.map {
        it.toDataComponent(newId)
    })
}

fun DataSequence.toEditSequence(colorProvider: ColorProvider): EditSequence {
    val branchCache = mutableMapOf<Long, EditLoop>()
    return EditSequence(id, title, map { dc ->
        when(dc) {
            is DataComponentLoop -> {
                val editMode = when(dc.mode) {
                    LoopComponent.Mode.UNLIMITED -> EditLoop.Mode.UNLIMITED
                    LoopComponent.Mode.TIMES -> EditLoop.Mode.TIMES
                    LoopComponent.Mode.BUTTON_PRESS -> EditLoop.Mode.TILL_BUTTON
                    LoopComponent.Mode.ADVANCED -> throw Error("Advanced is not jet supported")
                }
                EditLoop(dc.id, editMode, dc.times, colorProvider).also {
                    branchCache[it.id] = it
                }
            }
            is DataComponentWait -> {
                EditWait(dc.id, dc.title, dc.showNextTitle)
            }
            is DataComponentMarker -> {
                val branchComponent = branchCache[dc.associatedBranchComponentId] ?: throw Error("couldn't find BranchComponent")
                EndBranchMarker(dc.id, branchComponent).also {
                    branchComponent.endMarker = it
                }
            }
            is DataComponentCountdown -> {
                EditCountdown(dc.id, dc.title, dc.showNextTitle, dc.length, dc.startSound, dc.endSound)
            }
        }
    }.toMutableList())
}