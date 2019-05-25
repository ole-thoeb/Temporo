package com.eloem.temporo.timercomponents

import java.lang.UnsupportedOperationException

sealed class DataComponent(val id: Long) {
     abstract fun toRuntimeComponent(): Component
}

sealed class DataComponentUi(
    id: Long,
    var title: String,
    var showNextTitle: Boolean
) : DataComponent(id)

class DataComponentLoop(
    id: Long,
    val mode: LoopComponent.Mode,
    val times: Int
) : DataComponent(id) {

    override fun toRuntimeComponent(): LoopComponent {
        return when(mode) {
            LoopComponent.Mode.BUTTON_PRESS -> ButtonLoop(id)
            LoopComponent.Mode.TIMES -> TimesLoop(id, times)
            LoopComponent.Mode.UNLIMITED -> UnlimitedLoop(id)
            LoopComponent.Mode.ADVANCED -> throw Error("not yet supported")
        }
    }
}

class DataComponentMarker(
    id: Long,
    val associatedBranchComponentId: Long
) : DataComponent(id) {

    override fun toRuntimeComponent(): Component {
        throw UnsupportedOperationException()
    }
}

class DataComponentWait(
    id: Long,
    title: String,
    showNextTitle: Boolean
) : DataComponentUi(id, title, showNextTitle) {

    override fun toRuntimeComponent(): WaitComponent {
        return WaitComponent(id, title, showNextTitle)
    }
}

class DataComponentCountdown(
    id: Long,
    title: String,
    showNextTitle: Boolean,
    val length: Long,
    val startSound: Int,
    val endSound: Int
) : DataComponentUi(id, title, showNextTitle) {

    override fun toRuntimeComponent(): CountdownTimerComponent {
        return CountdownTimerComponent(id, title, showNextTitle, length, startSound, endSound)
    }
}


