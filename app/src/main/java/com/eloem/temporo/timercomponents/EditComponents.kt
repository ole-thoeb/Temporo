package com.eloem.temporo.timercomponents

import android.content.Context
import com.eloem.temporo.R
import com.eloem.temporo.util.minuetSecondText
import kotlin.UnsupportedOperationException

sealed class EditComponent(var id: Long) {
    abstract fun toComponent(idProvider: () -> Long): Component
    abstract fun toEditLoop(colorProvider: ColorProvider): EditLoop
    abstract fun toEditWait(): EditWait
    abstract fun toEditCountdown(): EditCountdown

    abstract fun getDisplayText(context: Context): String

    abstract val iconId: Int
}

interface ColorComponent {
    val color: Int
}

sealed class EditUi(
    id: Long,
    var title: String,
    var showNextTitle: Boolean
) : EditComponent(id) {
    abstract override fun toComponent(idProvider: () -> Long): UiComponent

    fun getDisplayTitle(context: Context): String = if (title == "") context.getString(R.string.noTitle) else title
}

class EditLoop(
    id: Long,
    var mode: Mode,
    var times: Int,
    val colorProvider: ColorProvider
) : EditComponent(id), ColorComponent {

    override val color: Int = colorProvider.newValue()

    enum class Mode { TIMES, UNLIMITED, TILL_BUTTON }

    override fun toEditLoop(colorProvider: ColorProvider): EditLoop = this

    override fun toEditWait(): EditWait {
        freeColor()
        return EditWait(id, "", false)
    }

    override fun toEditCountdown(): EditCountdown {
        freeColor()
        return EditCountdown(id, "", false, 0, 0, 0)
    }

    override fun toComponent(idProvider: () -> Long): BranchComponent {
        val finalId = if (id == Component.NO_ID) idProvider() else id
        return when (mode) {
            Mode.TIMES -> TimesLoop(finalId, times)
            Mode.UNLIMITED -> UnlimitedLoop(finalId)
            Mode.TILL_BUTTON -> ButtonLoop(finalId)
        }
    }

    override fun getDisplayText(context: Context): String = when(mode) {
        Mode.TIMES -> context.getString(R.string.loopTimesText, times)
        Mode.UNLIMITED -> context.getString(R.string.loopUnlimitedText)
        Mode.TILL_BUTTON -> context.getString(R.string.loopTillButtonText)
    }

    fun freeColor() {
        colorProvider.free(color)
    }

    val endMarker: EndBranchMarker by lazy { EndBranchMarker(this) }

    override val iconId: Int = R.drawable.ic_loop

    companion object {
        const val DEFAULT_TIMES = 1
    }
}

class EndBranchMarker(
    val associatedBranchComponent: EditLoop
) : EditComponent(Component.NO_ID), ColorComponent {

    override fun toEditLoop(colorProvider: ColorProvider): EditLoop {
        throw UnsupportedOperationException()
    }

    override fun toEditWait(): EditWait {
        throw UnsupportedOperationException()
    }

    override fun toEditCountdown(): EditCountdown {
        throw UnsupportedOperationException()
    }

    override fun toComponent(idProvider: () -> Long): Component {
        throw UnsupportedOperationException()
    }

    override fun getDisplayText(context: Context): String = context.getString(R.string.loopMarkerText)

    override val iconId: Int = R.drawable.ic_stop

    override val color: Int
        get() = associatedBranchComponent.color
}

class EditWait(
    id: Long,
    title: String,
    showNextTitle: Boolean
) : EditUi(id, title, showNextTitle) {

    override fun toEditLoop(colorProvider: ColorProvider): EditLoop = EditLoop(id, EditLoop.Mode.TIMES, EditLoop.DEFAULT_TIMES, colorProvider)

    override fun toEditWait(): EditWait = this

    override fun toEditCountdown(): EditCountdown = EditCountdown(id, title, showNextTitle, 0, 0, 0)

    override fun toComponent(idProvider: () -> Long): WaitComponent {
        val finalId = if (id == Component.NO_ID) idProvider() else id
        return WaitComponent(finalId, title, showNextTitle)
    }

    override fun getDisplayText(context: Context): String =
        "${getDisplayTitle(context)} ${if (showNextTitle) ", ${context.getString(R.string.showsNext)}" else ""}"


    override val iconId: Int = R.drawable.ic_wait
}

class EditCountdown(
    id: Long,
    title: String,
    showNextTitle: Boolean,
    var length: Long,
    var startSound: Int,
    var endSound: Int
) : EditUi(id, title, showNextTitle) {

    override fun toEditLoop(colorProvider: ColorProvider): EditLoop = EditLoop(id, EditLoop.Mode.TIMES, EditLoop.DEFAULT_TIMES, colorProvider)

    override fun toEditWait(): EditWait = EditWait(id, title, showNextTitle)

    override fun toEditCountdown(): EditCountdown = this

    override fun toComponent(idProvider: () -> Long): CountdownTimerComponent {
        val finalId = if (id == Component.NO_ID) idProvider() else id
        return CountdownTimerComponent(finalId, title, showNextTitle, length, startSound, endSound)
    }

    override fun getDisplayText(context: Context): String =
        "${getDisplayTitle(context)} ${if (showNextTitle) ", ${context.getString(R.string.showsNext)}" else ""}, ${minuetSecondText(length)}"

    override val iconId: Int = R.drawable.ic_hour_glass
}



