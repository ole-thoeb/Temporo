package com.eloem.temporo.ui

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.navigateUp
import com.eloem.temporo.R
import com.eloem.temporo.timercomponents.*
import com.eloem.temporo.util.*
import kotlinx.android.synthetic.main.fragment_display_sequence_timer.*

class DisplaySequenceTimerFragment : ChildFragment() {

    private val TAG = this::class.simpleName

    private lateinit var handler: TimerHandler

    private val args: DisplaySequenceTimerFragmentArgs by navArgs()

    private val globalViewModel: GlobalViewModel by activityViewModel()
    private val displayViewModel: DisplayViewModel by fragmentViewModel()

    private var countdownTimer: CountDownTimer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.fragment_display_sequence_timer, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        /*val repeat = UnlimitedLoop(1)
        val times10 = TimesLoop(2, 10)
        val up = CountdownTimerComponent(3, "Up", 1000, 0, 0)
        val down = CountdownTimerComponent(4, "down", 500, 0, 0)
        val wait10 = CountdownTimerComponent(5, "Warte", 10000, 0, 0)
        val waitButton = WaitComponent(6, "Press it")
        repeat.next = times10
        times10.next = up
        times10.branchNext = wait10
        up.next = times10//down
        //down.next = times10
        wait10.next = repeat*/

        /*val times4 = TimesLoop(1, 4)
        val ready = WaitComponent(2, "Bereit?", true)
        val min1 = CountdownTimerComponent(3, "Plank", false, 60000, 0, 0)
        val miniPause1 = CountdownTimerComponent(4, "Pause", true, 45000, 0, 0)
        val squats = WaitComponent(5, "Squats", false)
        val miniPause2 = CountdownTimerComponent(6, "Pause", true, 45000, 0, 0)
        val lunges = WaitComponent(7, "Lunges", false)
        val miniPause3 = CountdownTimerComponent(8, "Pause", true, 45000, 0, 0)
        val push = WaitComponent(9, "Push Ups", false)
        val miniPause4 = CountdownTimerComponent(10, "Pause", true, 45000, 0, 0)
        val butterfly = WaitComponent(11, "Butterfly", false)
        val miniPause5 = CountdownTimerComponent(12, "Pause", true, 45000, 0, 0)
        val climber = WaitComponent(13, "Mountain Climbers", false)
        val miniPause6 = CountdownTimerComponent(14, "Pause", true, 45000, 0, 0)
        val pikePush = WaitComponent(15, "Pike Push ups", false)
        val wait3 = CountdownTimerComponent(16, "Pause", false, 180000, 0, 0)

        times4.next = ready
        ready.next = min1
        min1.next = miniPause1
        miniPause1.next = squats
        squats.next = miniPause2
        miniPause2.next = lunges
        lunges.next = miniPause3
        miniPause3.next = push
        push.next = miniPause4
        miniPause4.next = butterfly
        butterfly.next = miniPause5
        miniPause5.next = climber
        climber.next = miniPause6
        miniPause6.next = pikePush
        pikePush.next = wait3
        wait3.next = times4


        handler = TimerHandler(times4)*/

    }

    override fun onResume() {
        super.onResume()

        hostActivity.mainFab.apply {
            show()
            animateToIcon(AnimatedIconFab.Icon.NEXT)
        }

        var first = true
        globalViewModel.getTimerSequence(args.sequenceId).observe(viewLifecycleOwner, Observer {
            if (it == null || !first || it.isEmpty()) return@Observer
            first = false
            handler = displayViewModel.getHandler {
                TimerHandler(it.toRuntimeSequence())
            }
            handler.onNextComponentListener = { timerHandler, component ->
                if (component !is UiComponent) {
                    timerHandler.next()
                } else {
                    titleTV.text = component.title
                    if (component.showNextTitle) {
                        val nextUi = timerHandler.previewNextUiComponent()
                        if (nextUi.isNotNoComponent() && nextUi.title != "") {
                            Log.d(TAG, "showing next")
                            nextTitleTV.apply {
                                visibility = View.VISIBLE
                                text = resources.getString(R.string.nextTitle, nextUi.title)
                            }
                        } else {
                            nextTitleTV.visibility = View.INVISIBLE
                        }
                    } else {
                        nextTitleTV.visibility = View.INVISIBLE
                    }
                    if (component is CountdownTimerComponent) {
                        setTimerUiVisibility(View.VISIBLE)
                        progressCircle.max = component.length.toInt()
                        val endMillis = displayViewModel.endMillis
                        val millisInFuture = if (endMillis != null && endMillis > System.currentTimeMillis()) {
                            Log.d(TAG, "resuming Timer")
                            endMillis - System.currentTimeMillis()
                        } else {
                            Log.d(TAG, "new Timer")
                            displayViewModel.endMillis = System.currentTimeMillis() + component.length
                            component.length
                        }
                        countdownTimer = object : CountDownTimer(millisInFuture, 16) {
                            override fun onFinish() {
                                timerHandler.next()
                            }

                            override fun onTick(millisUntilFinished: Long) {
                                progressCircle.progress = millisUntilFinished.toInt()
                                timeTV.text = minuetSecondText(millisUntilFinished)
                            }
                        }.start()
                    } else {
                        setTimerUiVisibility(View.GONE)
                    }
                }
            }
            handler.onButtonStackChangeListener = { size ->
                if (size == 0) {
                    hostActivity.mainFab.hide()
                } else {
                    hostActivity.mainFab.show()
                }
            }
            handler.onFinishedListener = { findNavController().navigateUp() }

            hostActivity.mainFab.setOnClickListener {
                handler.handleButtonPressed()
            }
            hostActivity.supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
            }
        })
    }

    override fun onPause() {
        super.onPause()

        countdownTimer?.cancel()
    }

    private fun setTimerUiVisibility(visibility: Int) {
        progressCircle.visibility = visibility
        timeTV.visibility = visibility
    }
}