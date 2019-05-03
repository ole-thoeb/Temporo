package com.eloem.temporo.database

import com.eloem.temporo.timercomponents.*

fun List<EditComponent>.toSerialisedList(sequenceId: Long): List<SerialisedComponent> {
    return mapIndexed { index, eComp ->
        when(eComp) {
            is EditLoop -> SerialisedComponent(
                eComp.id,
                sequenceId,
                SerialisedComponent.TYPE_LOOP,
                index,
                mode = eComp.mode.ordinal,
                times = eComp.times
            )
            is EditCountdown -> SerialisedComponent(
                eComp.id,
                sequenceId,
                SerialisedComponent.TYPE_COUNTDOWN,
                index,
                title = eComp.title,
                showNext = eComp.showNextTitle,
                length = eComp.length,
                startSound = eComp.startSound,
                endSound = eComp.endSound
            )
            is EditWait -> SerialisedComponent(
                eComp.id,
                sequenceId,
                SerialisedComponent.TYPE_WAIT,
                index,
                title = eComp.title,
                showNext = eComp.showNextTitle
            )
            is EndBranchMarker -> SerialisedComponent(
                eComp.id,
                sequenceId,
                SerialisedComponent.TYPE_MARKER,
                index,
                branchId = eComp.associatedBranchComponent.id
            )
        }
    }
}