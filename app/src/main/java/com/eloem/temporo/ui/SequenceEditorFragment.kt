package com.eloem.temporo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.eloem.temporo.R
import com.eloem.temporo.recyclerview.BottomSpacingAdapter
import com.eloem.temporo.recyclerview.TouchHelperCallback
import com.eloem.temporo.timercomponents.*
import com.eloem.temporo.util.*
import kotlinx.android.synthetic.main.fragment_sequence_editor.*
import java.lang.Error

class SequenceEditorFragment : ChildFragment() {

    private val viewModel: EditorViewModel by fragmentViewModel()
    private val globalViewModel: GlobalViewModel by activityViewModel()

    private val args: SequenceEditorFragmentArgs by navArgs()

    private lateinit var recyclerAdapter: SequenceAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sequence_editor, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        recyclerAdapter = SequenceAdapter(globalViewModel)
        val callback = TouchHelperCallback(recyclerAdapter, requireContext())
        val touchHelper = ItemTouchHelper(callback)

        recyclerAdapter.onDragListener = {
            touchHelper.startDrag(it)
        }

        touchHelper.attachToRecyclerView(recyclerView)
        recyclerView.apply {
            adapter = BottomSpacingAdapter(recyclerAdapter, resources.getDimensionPixelSize(R.dimen.paddingBottomRecyclerView))
            layoutManager = LinearLayoutManager(requireContext())
        }

        globalViewModel.getTimerSequence(args.sequenceId).observe(viewLifecycleOwner, ObserveOnceNotNull {
            recyclerAdapter.editSequence = viewModel.getEditSequence {
                it.toEditSequence(recyclerAdapter.colorProvider)
            }
            recyclerAdapter.notifyDataSetChanged()
        })

        hostActivity.mainFab.apply {
            animateToIcon(AnimatedIconFab.Icon.CHECK)
            setOnClickListener {
                if (recyclerAdapter.editSequence.editComponents.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        R.string.emptySequence,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    this@SequenceEditorFragment.findNavController()
                        .navigate(
                            SequenceEditorFragmentDirections
                                .actionSequenceEditorToDisplaySequenceTimer(args.sequenceId)
                        )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()

        //globalViewModel.updateTimerSequence(recyclerAdapter.editSequence.toDataSequence())
    }
}