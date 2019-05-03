package com.eloem.temporo.ui

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eloem.temporo.R
import com.eloem.temporo.recyclerview.BottomSpacingAdapter
import com.eloem.temporo.recyclerview.TouchHelperCallback
import com.eloem.temporo.timercomponents.*
import com.eloem.temporo.util.addAsOnlyChild
import com.eloem.temporo.util.editorfactory.CountdownEditFactory
import com.eloem.temporo.util.editorfactory.LoopEditFactory
import com.eloem.temporo.util.editorfactory.WaitEditFactory
import com.eloem.temporo.util.fragmentViewModel
import com.eloem.temporo.util.getAttribute
import com.eloem.temporo.util.swap
import com.example.eloem.dartCounter.recyclerview.ContextAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.dialog_sequence_editor_configure_component.view.*
import kotlinx.android.synthetic.main.fragment_sequence_editor.*

class SequenceEditorFragment : Fragment() {

    val viewModel: EditorViewModel by fragmentViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sequence_editor, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val recyclerAdapter = RecyclerListAdapter(mutableListOf(
            EditWait(1, "Uhh", true),
            EditCountdown(3, "Ahh", false, 60000, 0, 0)))
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
    }

    class RecyclerListAdapter(var editTimerSequence: MutableList<EditComponent>) : ContextAdapter<RecyclerView.ViewHolder>(),
        TouchHelperCallback.ItemTouchHelperAdapter {

        class ItemViewHolder(layout: View) : RecyclerView.ViewHolder(layout) {
            val infoText: TextView = layout.findViewById(R.id.informationTv)
            val dragHandle: ImageView = layout.findViewById(R.id.dragView)
            val editButton: ImageButton = layout.findViewById(R.id.editButton)
            val typeIcon: ImageView = layout.findViewById(R.id.typeIcon)

            val foregroundView: View = layout.findViewById(R.id.foreground)
            val backgroundView: View = layout.findViewById(R.id.background)
        }

        class AddComponentViewHolder(layout: View) : RecyclerView.ViewHolder(layout) {
            val root: ViewGroup = layout.findViewById(R.id.foreground)
        }

        var onDragListener: ((RecyclerView.ViewHolder) -> Unit)? = null

        private val colorProvider by lazy { ColorProvider(context.resources.getIntArray(R.array.colorArray).toList().shuffled()) }

        override fun getItemViewType(position: Int): Int = when(position) {
            in editTimerSequence.indices -> VIEW_TYPE_COMPONENT
            editTimerSequence.size -> VIEW_TYPE_ADD
            else -> throw Error("position: $position is unknown")
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when(viewType) {
            VIEW_TYPE_COMPONENT -> ItemViewHolder(inflate(R.layout.item_sequence_editor_edit, parent))
            VIEW_TYPE_ADD -> AddComponentViewHolder(inflate(R.layout.item_sequence_editor_add, parent))
            else -> throw Error("viewType: $viewType is unknown")
        }

        @SuppressLint("ClickableViewAccessibility", "InflateParams")
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder.itemViewType == VIEW_TYPE_COMPONENT) {
                holder as ItemViewHolder
                val editComponent = editTimerSequence[position]
                holder.infoText.text = editComponent.getDisplayText(context)
                holder.typeIcon.setImageResource(editComponent.iconId)
                holder.typeIcon.imageTintList = if (editComponent is ColorComponent) {
                    ColorStateList.valueOf(editComponent.color)
                } else {
                    ColorStateList.valueOf(context.getAttribute(R.attr.colorOnBackground).data)
                }

                holder.dragHandle.setOnTouchListener { v, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        onDragListener?.invoke(holder)
                    }
                    false
                }
                if (editComponent is EndBranchMarker) {
                    holder.editButton.visibility = View.GONE
                    return
                } else {
                    holder.editButton.visibility = View.VISIBLE
                }
                holder.editButton.setOnClickListener {
                    val custView =
                        layoutInflater.inflate(R.layout.dialog_sequence_editor_configure_component, null, false)
                    custView.typeSpinner.adapter = ArrayAdapter<String>(
                        context,
                        android.R.layout.simple_spinner_dropdown_item,
                        context.resources.getStringArray(R.array.componentTypes)
                    )

                    var factory = when (editComponent) {
                        is EditCountdown -> CountdownEditFactory(editComponent)
                        is EditWait -> WaitEditFactory(editComponent)
                        is EditLoop -> LoopEditFactory(editComponent)
                        is EndBranchMarker -> throw Error("${EndBranchMarker::class.simpleName} can't have a factory")
                    }

                    custView.typeSpinner.setSelection(
                        when (editComponent) {
                            is EditWait -> 0
                            is EditLoop -> 1
                            is EditCountdown -> 2
                            else -> 0
                        }
                    )

                    custView.typeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {
                            //nothing
                        }

                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                            when (position) {
                                0 -> {
                                    if (factory is WaitEditFactory) return

                                    removeAssociatedEndMarker(holder.adapterPosition)

                                    factory = WaitEditFactory(factory.updatedComponent.toEditWait())
                                }
                                1 -> {
                                    if (factory is LoopEditFactory) return

                                    factory = LoopEditFactory(factory.updatedComponent.toEditLoop(colorProvider))
                                }
                                2 -> {
                                    if (factory is CountdownEditFactory) return

                                    removeAssociatedEndMarker(holder.adapterPosition)

                                    factory = CountdownEditFactory(factory.updatedComponent.toEditCountdown())
                                }
                            }

                            custView.optionsContainer.addAsOnlyChild(factory.createEditorView(custView.optionsContainer))
                        }
                    }

                    custView.optionsContainer.addAsOnlyChild(factory.createEditorView(custView.optionsContainer))

                    MaterialAlertDialogBuilder(context)
                        .setView(custView)
                        .setPositiveButton(R.string.dialogPositive) { _, _ ->
                            val updatedComponent = factory.updatedComponent
                            val pos = holder.adapterPosition
                            editTimerSequence[pos] = updatedComponent
                            notifyItemChanged(pos)
                            if (updatedComponent is EditLoop) {
                                if (updatedComponent.endMarker !in editTimerSequence) {
                                    val insertLocation = holder.adapterPosition + 1
                                    editTimerSequence.add(insertLocation, updatedComponent.endMarker)
                                    notifyItemInserted(insertLocation)
                                }
                            }
                        }
                        .setNegativeButton(R.string.dialogNegative) { _, _ -> /*nothing*/ }
                        .show()
                }
            } else {
                holder as AddComponentViewHolder
                holder.root.setOnClickListener {
                    editTimerSequence.add(EditCountdown(Component.NO_ID, "", false, 0, 0, 0))
                    notifyItemInserted(editTimerSequence.lastIndex)
                }
            }
        }

        override fun getItemCount(): Int = editTimerSequence.size + 1

        override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
            val itemToMove = editTimerSequence[fromPosition]
            //cant switch with add item
            var updatedToPosition = if(toPosition > editTimerSequence.lastIndex) editTimerSequence.lastIndex else toPosition
            //make sure loop and end marker are in right order
            updatedToPosition = when(itemToMove) {
                is EndBranchMarker -> {
                    val branchIndex = editTimerSequence.indexOf(itemToMove.associatedBranchComponent)
                    //marker should not be above loop
                    if (updatedToPosition <= branchIndex && branchIndex != -1) {
                        branchIndex + 1
                    } else {
                        updatedToPosition
                    }
                }
                is EditLoop -> {
                    val branchIndex = editTimerSequence.indexOf(itemToMove.endMarker)
                    //loop should not be below marker
                    if (updatedToPosition >= branchIndex && branchIndex != -1) {
                        branchIndex - 1
                    } else {
                        updatedToPosition
                    }
                }
                else -> updatedToPosition
            }
            if (fromPosition < updatedToPosition) {
                for (i in fromPosition until updatedToPosition) {
                    editTimerSequence.swap(i, i + 1)
                }
            } else {
                for (i in fromPosition downTo updatedToPosition + 1) {
                    editTimerSequence.swap(i, i - 1)
                }
            }
            notifyItemMoved(fromPosition, updatedToPosition)
            return true
        }

        override fun onItemDismiss(position: Int) {
            when (val itemToRemove = editTimerSequence[position]) {
                is EditLoop -> {
                    val indexOfMarker = editTimerSequence.indexOf(itemToRemove.endMarker)
                    if (indexOfMarker >= 0) {
                        removeAt(indexOfMarker)
                    }
                }
                is EndBranchMarker -> {
                    val indexOfLoop = editTimerSequence.indexOf(itemToRemove.associatedBranchComponent)
                    if (indexOfLoop >= 0) {
                        removeAt(indexOfLoop)
                    }
                }
            }
            removeAt(position)
        }

        private fun removeAt(position: Int) {
            editTimerSequence.removeAt(position)
            notifyItemRemoved(position)
        }

        private fun removeAssociatedEndMarker(pos: Int) {
            editTimerSequence[pos].let { editComponent ->
                if (editComponent is EditLoop) {
                    val index = editTimerSequence.indexOf(editComponent.endMarker)
                    if (index >= 0) {
                        editTimerSequence.removeAt(index)
                        notifyItemRemoved(index)
                    }
                }
            }
        }

        companion object {
            const val VIEW_TYPE_COMPONENT = 1
            const val VIEW_TYPE_ADD = 0
            const val VIEW_TYPE_MARKER = 2
        }
    }
}
