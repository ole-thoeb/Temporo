package com.eloem.temporo.database

import com.eloem.temporo.timercomponents.*

fun DataSequence.toSerialisedList(): List<SerialisedComponent> {
    return mapIndexed { index, dComp ->
        dComp.toSerialisedComponent(id, index)
    }
}

fun DataComponent.toSerialisedComponent(sequenceId: Long, index: Int): SerialisedComponent {
    return when(this) {
        is DataComponentLoop -> SerialisedComponent(
            id,
            sequenceId,
            SerialisedComponent.Type.LOOP.ordinal,
            index,
            mode = mode.ordinal,
            times = times
        )
        is DataComponentCountdown -> SerialisedComponent(
            id,
            sequenceId,
            SerialisedComponent.Type.COUNTDOWN.ordinal,
            index,
            title = title,
            showNext = showNextTitle,
            length = length,
            startSound = startSound,
            endSound = endSound
        )
        is DataComponentWait -> SerialisedComponent(
            id,
            sequenceId,
            SerialisedComponent.Type.WAIT.ordinal,
            index,
            title = title,
            showNext = showNextTitle
        )
        is DataComponentMarker -> SerialisedComponent(
            id,
            sequenceId,
            SerialisedComponent.Type.MARKER.ordinal,
            index,
            branchId = associatedBranchComponentId
        )
    }
}

fun List<SerialisedComponent>.toDataComponents(): List<DataComponent> {
    return sortedBy { it.position }.map {
        when(SerialisedComponent.Type.values()[it.type]) {
            SerialisedComponent.Type.COUNTDOWN -> DataComponentCountdown(
                it.id,
                it.title!!,
                it.showNext!!,
                it.length!!,
                it.startSound!!,
                it.endSound!!
            )
            SerialisedComponent.Type.LOOP -> DataComponentLoop(
                it.id,
                LoopComponent.Mode.values()[it.mode!!],
                it.times!!
            )
            SerialisedComponent.Type.MARKER -> DataComponentMarker(
                it.id,
                it.branchId!!
            )
            SerialisedComponent.Type.WAIT -> DataComponentWait(
                it.id,
                it.title!!,
                it.showNext!!
            )
        }
    }
}

fun DataSequence.toSqlSequence() = SequenceSql(id, title)