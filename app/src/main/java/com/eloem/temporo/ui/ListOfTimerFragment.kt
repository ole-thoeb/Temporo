package com.eloem.temporo.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eloem.temporo.R
import com.eloem.temporo.recyclerview.BottomSpacingAdapter
import com.eloem.temporo.timercomponents.DataSequence
import com.eloem.temporo.util.AnimatedIconFab
import com.eloem.temporo.util.activityViewModel
import com.example.eloem.dartCounter.recyclerview.ContextAdapter
import com.example.eloem.dartCounter.recyclerview.GridSpacingItemDecoration
import kotlinx.android.synthetic.main.fragment_list_of_timer.*

class ListOfTimerFragment : ChildFragment() {

    private val globalViewModel: GlobalViewModel by activityViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list_of_timer, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val mainAdapter = MainListAdapter(emptyList(), globalViewModel)

        list.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = BottomSpacingAdapter(mainAdapter, resources.getDimensionPixelOffset(R.dimen.paddingBottomRecyclerView),2)
            addItemDecoration(GridSpacingItemDecoration(resources.getDimensionPixelOffset(R.dimen.gridSpacingRecyclerView), 2, true))
            emptyThreshold = 2
            emptyView = empty
        }

        globalViewModel.allTimerSequences.observe(viewLifecycleOwner, Observer {
            mainAdapter.sequences = it
            mainAdapter.notifyDataSetChanged()
        })

        hostActivity.mainFab.apply {
            show()
            animateToIcon(AnimatedIconFab.Icon.ADD)
            setOnClickListener {
                this@ListOfTimerFragment.findNavController()
                    .navigate(ListOfTimerFragmentDirections
                        .actionListOfTimerFragmentToSequenceEditor(globalViewModel.newTimerSequence().id))
            }
        }
    }

    class MainListAdapter(
        var sequences: List<DataSequence>,
        private val globalViewModel: GlobalViewModel
    ): ContextAdapter<MainListAdapter.SequenceViewHolder>() {

        class SequenceViewHolder(layout: View): RecyclerView.ViewHolder(layout) {
            val titleTV: TextView = layout.findViewById(R.id.titleTV)
            val card: CardView = layout.findViewById(R.id.rootCard)
            val optionsButton: ImageButton = layout.findViewById(R.id.options)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SequenceViewHolder {
            return SequenceViewHolder(inflate(R.layout.item_timer_list_timer_card, parent))
        }

        override fun getItemCount(): Int = sequences.size

        override fun onBindViewHolder(holder: SequenceViewHolder, position: Int) {
            holder.titleTV.text = sequences[position].title
            holder.optionsButton.setOnClickListener { button ->
                val popupMenu = PopupMenu(context, button)
                popupMenu.apply {
                    menuInflater.inflate(R.menu.popup_item_sequence_list, popupMenu.menu)

                    setOnMenuItemClickListener {
                        when(it.itemId) {
                            R.id.edit -> {
                                button.findNavController()
                                    .navigate(ListOfTimerFragmentDirections
                                        .actionListOfTimerFragmentToSequenceEditor(sequences[holder.adapterPosition].id))
                                true
                            }
                            R.id.delete -> {
                                globalViewModel.deleteTimerSequence(sequences[holder.adapterPosition])
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }
            holder.card.setOnClickListener {
                val sequence = sequences[holder.adapterPosition]
                if (sequence.isEmpty()) {
                    it.findNavController()
                        .navigate(ListOfTimerFragmentDirections
                            .actionListOfTimerFragmentToSequenceEditor(sequence.id))
                } else {
                    it.findNavController()
                        .navigate(
                            ListOfTimerFragmentDirections
                                .actionListOfTimerFragmentToDisplaySequenceTimer(sequence.id)
                        )
                }
            }
        }
    }
}
