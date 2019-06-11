package com.eloem.temporo.ui

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.eloem.temporo.R
import com.eloem.temporo.timercomponents.*
import com.eloem.temporo.util.*
import kotlinx.android.synthetic.main.fragment_display_sequence_timer.*
import org.jetbrains.anko.doAsync

class DisplayTimerFragment : ChildFragment() {

    val viewModel: DisplayTimerViewModel by viewModels()
    private val globalViewModel: GlobalViewModel by activityViewModels()

    private val args: DisplaySequenceTimerFragmentArgs by navArgs()

    private var countdownTimer: CountDownTimer? = null
    private var timerExecutor: TimerExecutorImpl? = null

    private val TAG = this::class.simpleName

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.fragment_display_sequence_timer, container, false)
    }

    override fun onResume() {
        super.onResume()

        hostActivity.mainFab.apply {
            show()
            animateToIcon(AnimatedIconFab.Icon.NEXT)
            setOnClickListener {
                timerExecutor?.notifyButtonPressed()
            }
        }

        var first = true
        globalViewModel.getTimerSequence(args.sequenceId).observe(viewLifecycleOwner, Observer {
            if (it == null || !first || it.isEmpty()) return@Observer
            first = false
            timerExecutor = TimerExecutorImpl(viewModel.getInstructions { it.toInstructions() }).apply {
                onFinish{ findNavController().navigateUp() }.doAsync {  }
                continueExecution()
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

    inner class TimerExecutorImpl(override val instructions: List<Instruction>) : TimerExecutor {

        override var programCounter: Int
            get() = viewModel.programCounter
            set(value) {
                viewModel.programCounter = value
            }

        override val localVariables: MutableMap<String, Int>
            get() = viewModel.localVariables

        override var title: String
            get() = viewModel.title
            set(value) {
                viewModel.title = value
                titleTV.text = value
            }

        override var nextTitle: String
            get() = viewModel.nextTitle
            set(value) {
                viewModel.nextTitle = value
                nextTitleTV.text = resources.getString(R.string.nextTitle, value)
            }

        override var isShowingNext: Boolean
            get() = viewModel.isShowingNext
            set(value) {
                viewModel.isShowingNext = value
                nextTitleTV.visibility = if (value) View.VISIBLE else View.INVISIBLE
            }

        init {
            //refresh ui data
            title = viewModel.title
            isShowingNext = viewModel.isShowingNext
            nextTitle = viewModel.nextTitle

            updateButtonVisibility()
            viewModel.error?.let { displayParsError(it) }
        }

        fun continueExecution() {
            if (viewModel.error != null) return

            val endMillis = viewModel.endMillis
            val lastTimerInstruction = viewModel.lastTimerInstruction
            if (endMillis != null && lastTimerInstruction != null && endMillis > System.currentTimeMillis()) {
                suspendTillTimerFinished()
                startTimer(lastTimerInstruction)
                lastTimerInstruction.setUiData(this)
                return
            }
            if (viewModel.waitingForButton) {
                setTimerUiVisibility(View.INVISIBLE)
                return
            }
            while (programCounter < instructions.size && !isSuspended) {
                Log.d(TAG, "executing: ${instructions[programCounter]}")
                setTimerUiVisibility(View.INVISIBLE)
                instructions[programCounter].execute(this)
            }
            if (programCounter >= instructions.size && !isSuspended) {
                onFinishListener?.invoke()
            }
        }

        /*override fun evaluateCondition(condition: String): Boolean {
            return condition.splitToTokens()
                .chainSuccess { mapOperations(this) }
                .chainSuccess { addBrackets(this) }
                .chainSuccess { buildOperationTree(this) }
                .chainSuccess { buildLogicOperation(this, localVariables) }
                .throwError().execute().also { Log.d(TAG, "condition: $condition = $it") }
        }

        override fun evaluateValue(valueExpression: String): Int {
            return valueExpression.splitToTokens()
                .chainSuccess { mapOperations(this) }
                .chainSuccess { addBrackets(this) }
                .chainSuccess { buildOperationTree(this) }
                .chainSuccess { buildArithmeticOperation(this, localVariables) }
                .throwError().execute().also { Log.d(TAG, "expression: $valueExpression = $it") }
        }*/

        override fun buildArithmeticOperation(expression: String): Optional<ArithmeticOperation, ParsError> {
            return expression.splitToTokens()
                .chainSuccess { mapOperations(this) }
                .chainSuccess { addBrackets(this) }
                .chainSuccess { buildOperationTree(this) }
                .chainSuccess { buildArithmeticOperation(this, localVariables) }
                .ifFailure { displayParsError(it as ParsError) } as Optional<ArithmeticOperation, ParsError>
        }

        override fun buildLogicOperation(expression: String): Optional<LogicOperation, ParsError> {
            return expression.splitToTokens()
                .chainSuccess { mapOperations(this) }
                .chainSuccess { addBrackets(this) }
                .chainSuccess { buildOperationTree(this) }
                .chainSuccess { buildLogicOperation(this, localVariables) }
                .ifFailure { displayParsError(it as ParsError) } as Optional<LogicOperation, ParsError>
        }

        override fun suspendTillButtonPressed() {
            viewModel.waitingForButton = true
            updateButtonVisibility()
        }

        override fun notifyButtonPressed() {
            viewModel.waitingForButton = false
            updateButtonVisibility()
            viewModel.buttonVariables.forEach { localVariables[it] = 1 }
            continueExecution()
        }

        private var waitingForTimer = false

        override fun suspendTillTimerFinished() {
            waitingForTimer = true
        }

        override fun notifyTimerFinished() {
            waitingForTimer = false
            continueExecution()
        }

        override val isSuspended: Boolean get() = viewModel.waitingForButton || waitingForTimer

        override fun startTimer(timerInstruction: TimerInstruction) {
            setTimerUiVisibility(View.VISIBLE)
            viewModel.lastTimerInstruction = timerInstruction
            progressCircle.max = timerInstruction.length.toInt()
            val endMillis = viewModel.endMillis
            val millisInFuture = if (endMillis != null && endMillis > System.currentTimeMillis()) {
                //Log.d(TAG, "resuming Timer")
                endMillis - System.currentTimeMillis()
            } else {
                //Log.d(TAG, "new Timer")
                viewModel.endMillis = System.currentTimeMillis() + timerInstruction.length
                timerInstruction.length
            }
            countdownTimer?.cancel()
            countdownTimer = object : CountDownTimer(millisInFuture, 16) {
                override fun onFinish() {
                    notifyTimerFinished()
                }

                override fun onTick(millisUntilFinished: Long) {
                    progressCircle.progress = millisUntilFinished.toInt()
                    timeTV.text = minuetSecondText(millisUntilFinished)
                }
            }.start()
        }

        private fun displayParsError(error: ParsError) {
            //TODO
        }

        override fun registerButtonVariable(variable: String) {
            viewModel.buttonVariables.add(variable)
            updateButtonVisibility()
        }

        override fun unregisterButtonVariable(variable: String) {
            viewModel.buttonVariables.remove(variable)
            updateButtonVisibility()
        }

        private fun updateButtonVisibility() {
            if (viewModel.waitingForButton || viewModel.buttonVariables.isNotEmpty()) {
                hostActivity.mainFab.show()
            } else {
                hostActivity.mainFab.hide()
            }
        }

        private var onFinishListener: (() -> Unit)? = null
        fun onFinish(action: () -> Unit) {
            onFinishListener = action
        }
    }

    class DisplayTimerViewModel : ViewModel() {
        var programCounter: Int = 0
        val localVariables: MutableMap<String, Int> = mutableMapOf()

        private var instructions: List<Instruction>? = null
        fun getInstructions(creator: () -> List<Instruction>): List<Instruction> {
            return instructions.ifNull(creator).also { instructions = it }
        }

        var title: String = ""
        var nextTitle: String = ""
        var isShowingNext: Boolean = false

        var endMillis: Long? = null
        var lastTimerInstruction: TimerInstruction? = null

        var waitingForButton = false

        val buttonVariables: MutableList<String> = mutableListOf()

        var error: ParsError? = null
    }
}