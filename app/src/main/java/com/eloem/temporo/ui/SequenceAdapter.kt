package com.eloem.temporo.ui

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.eloem.temporo.R
import com.eloem.temporo.recyclerview.TouchHelperCallback
import com.eloem.temporo.timercomponents.*
import com.eloem.temporo.util.*
import com.eloem.temporo.util.editorfactory.CountdownEditFactory
import com.eloem.temporo.util.editorfactory.LoopEditFactory
import com.eloem.temporo.util.editorfactory.WaitEditFactory
import com.example.eloem.dartCounter.recyclerview.ContextAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.dialog_sequence_editor_configure_component.view.*
import kotlin.math.max
import kotlin.math.min

class SequenceAdapter(
    private val globalViewModel: GlobalViewModel
) : ContextAdapter<RecyclerView.ViewHolder>(), TouchHelperCallback.ItemTouchHelperAdapter {

    class ItemViewHolder(layout: View) : RecyclerView.ViewHolder(layout) {
        val infoText: TextView = layout.findViewById(R.id.informationTv)
        val dragHandle: ImageView = layout.findViewById(R.id.dragView)
        val editButton: ImageButton = layout.findViewById(R.id.editButton)
        val typeIcon: ImageView = layout.findViewById(R.id.typeIcon)

        val foregroundView: View = layout.findViewById(R.id.foreground)
        val iconLeft: ImageView = layout.findViewById(R.id.iconLeft)
        val iconRight: ImageView = layout.findViewById(R.id.iconRight)
    }

    class AddComponentViewHolder(layout: View) : RecyclerView.ViewHolder(layout) {
        val root: ViewGroup = layout.findViewById(R.id.foreground)
    }

    class TitleViewHolder(layout: View) : RecyclerView.ViewHolder(layout) {
        val editTextTitle: EditText = layout.findViewById(R.id.titleET)

        init {
            editTextTitle.imeOptions = EditorInfo.IME_ACTION_DONE
            editTextTitle.setRawInputType(InputType.TYPE_CLASS_TEXT)
        }
    }

    var onDragListener: ((RecyclerView.ViewHolder) -> Unit)? = null

    val colorProvider by lazy { ColorProvider(context.resources.getIntArray(R.array.colorArray).toList().shuffled()) }

    var editSequence: EditSequence = EditSequence.EMPTY
        set(value) {
            field = value
            maxId = editSequence.editComponents.maxBy { it.id }?.id ?: Component.NO_ID
            notifyDataSetChanged()
        }

    private var maxId = editSequence.editComponents.maxBy { it.id }?.id ?: Component.NO_ID
    private val idProvider = {
        println(++maxId)
        maxId
    }

    override fun getItemViewType(position: Int): Int = when(position) {
        0 -> VIEW_TYPE_TITLE
        in editSequence.editComponents.indices shr 1 -> VIEW_TYPE_COMPONENT
        editSequence.editComponents.size + 1-> VIEW_TYPE_ADD
        else -> throw Error("position: $position is unknown")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when(viewType) {
        VIEW_TYPE_COMPONENT -> ItemViewHolder(inflate(R.layout.item_sequence_editor_edit, parent))
        VIEW_TYPE_ADD -> AddComponentViewHolder(inflate(R.layout.item_sequence_editor_add, parent))
        VIEW_TYPE_TITLE -> TitleViewHolder(inflate(R.layout.item_sequence_editor_title, parent))
        else -> throw Error("viewType: $viewType is unknown")
    }

    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder.itemViewType) {
            VIEW_TYPE_COMPONENT -> {
                holder as ItemViewHolder
                val editComponent = editSequence.editComponents[holderToCollectionPos(position)]
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

                                    //removeAssociatedEndMarker(holder.adapterPosition)
                                    cacheAssociatedEndMarker(holder.adapterPosition)

                                    factory = WaitEditFactory(factory.updatedComponent.toEditWait())
                                }
                                1 -> {
                                    if (factory is LoopEditFactory) return

                                    factory = LoopEditFactory(factory.updatedComponent.toEditLoop(colorProvider))
                                }
                                2 -> {
                                    if (factory is CountdownEditFactory) return

                                    //removeAssociatedEndMarker(holder.adapterPosition)
                                    cacheAssociatedEndMarker(holder.adapterPosition)

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
                            editSequence.editComponents[holderToCollectionPos(pos)] = updatedComponent
                            globalViewModel.updateTimerComponent(editSequence.id, holderToCollectionPos(pos), updatedComponent.toDataComponent(idProvider))
                            notifyItemChanged(pos)
                            if (updatedComponent is EditLoop) {
                                if (updatedComponent.endMarker !in editSequence.editComponents) {
                                    val insertLocation = holder.adapterPosition + 1
                                    val marker = updatedComponent.endMarker
                                    editSequence.editComponents.add(
                                        holderToCollectionPos(insertLocation),
                                        marker
                                    )
                                    globalViewModel.insertTimerComponent(editSequence.id, holderToCollectionPos(insertLocation), marker.toDataComponent(idProvider))
                                    notifyItemInserted(insertLocation)
                                }
                            }
                            removeCachedEndMarker()
                        }
                        .setNegativeButton(R.string.dialogNegative) { _, _ -> /*nothing*/ }
                        .setOnDismissListener {
                            clearCachedEndMarker()
                        }
                        .show()
                }
            }
            VIEW_TYPE_ADD -> {
                holder as AddComponentViewHolder
                holder.root.setOnClickListener {
                    val insertIndex = editSequence.editComponents.size
                    val insertComponent = EditCountdown(Component.NO_ID, "", false, 0, 0, 0)
                    editSequence.editComponents.add(insertComponent)
                    globalViewModel.insertTimerComponent(editSequence.id, insertIndex, insertComponent.toDataComponent(idProvider))
                    notifyItemInserted(collectionToHolderPos(insertIndex))
                }
            }
            VIEW_TYPE_TITLE -> {
                holder as TitleViewHolder
                holder.editTextTitle.setText(editSequence.title)
                holder.editTextTitle.addTextChangedListener(OnTextChangedListener {
                    editSequence.title = it
                    globalViewModel.updateSequenceTitle(editSequence.id, editSequence.title)
                })
            }
        }
    }

    override fun getItemCount(): Int = editSequence.editComponents.size + 2

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        val updatedFromPosition = holderToCollectionPos(fromPosition)
        val itemToMove = editSequence.editComponents[updatedFromPosition]
        //cant switch with add item
        var updatedToPosition = if(toPosition > collectionToHolderPos(editSequence.editComponents.lastIndex)) {
            editSequence.editComponents.lastIndex
        } else {
            holderToCollectionPos(toPosition)
        }
        //cant switch with title item
        if (updatedToPosition < 0) updatedToPosition = 0
        //make sure loop and end marker are in right order
        when(itemToMove) {
            is EndBranchMarker -> {
                val branchIndex = editSequence.editComponents.indexOf(itemToMove.associatedBranchComponent)
                //marker should not be above loop
                if (updatedToPosition <= branchIndex && branchIndex != -1) {
                    updatedToPosition = branchIndex + 1
                }
            }
            is EditLoop -> {
                val branchIndex = editSequence.editComponents.indexOf(itemToMove.endMarker)
                //loop should not be below marker
                if (updatedToPosition >= branchIndex && branchIndex != -1) {
                    updatedToPosition = branchIndex - 1
                }
            }
        }
        if (updatedFromPosition < updatedToPosition) {
            for (i in updatedFromPosition until updatedToPosition) {
                editSequence.editComponents.swap(i, i + 1)
            }
        } else {
            for (i in updatedFromPosition downTo updatedToPosition + 1) {
                editSequence.editComponents.swap(i, i - 1)
            }
        }
        val startIndex = min(updatedFromPosition, updatedToPosition)
        val endIndex = max(updatedFromPosition, updatedToPosition)
        globalViewModel.updateTimerComponents(editSequence.id, startIndex, editSequence.editComponents.subList(startIndex, endIndex + 1).map { it.toDataComponent(idProvider) })
        notifyItemMoved(collectionToHolderPos(updatedFromPosition), collectionToHolderPos(updatedToPosition))
        return true
    }

    //position ist holder position
    override fun onItemDismiss(position: Int) {
        val itemToRemove = editSequence.editComponents[holderToCollectionPos(position)]
        removeAt(position)
        when (itemToRemove) {
            is EditLoop -> {
                val indexOfMarker = editSequence.editComponents.indexOf(itemToRemove.endMarker)
                if (indexOfMarker >= 0) {
                    removeAt(collectionToHolderPos(indexOfMarker))
                }
            }
            is EndBranchMarker -> {
                val indexOfLoop = editSequence.editComponents.indexOf(itemToRemove.associatedBranchComponent)
                if (indexOfLoop >= 0) {
                    removeAt(collectionToHolderPos(indexOfLoop))
                }
            }
        }
    }

    //position is holder position
    private fun removeAt(position: Int) {
        val itemToRemove = editSequence.editComponents[holderToCollectionPos(position)]
        editSequence.editComponents.removeAt(holderToCollectionPos(position))
        globalViewModel.deleteTimerComponent(editSequence.id, holderToCollectionPos(position), itemToRemove.toDataComponent(idProvider))
        notifyItemRemoved(position)
    }

    //pos is holder position
    /*private fun removeAssociatedEndMarker(pos: Int) {
        editSequence.editComponents[holderToCollectionPos(pos)].let { editComponent ->
            if (editComponent is EditLoop) {
                val index = editSequence.editComponents.indexOf(editComponent.endMarker)
                if (index >= 0) {
                    editSequence.editComponents.removeAt(index)
                    notifyItemRemoved(collectionToHolderPos(index))
                }
            }
        }
    }*/

    private var cachedEndMarker: EndBranchMarker? = null
    //pos is holder position
    private fun cacheAssociatedEndMarker(pos: Int) {
        editSequence.editComponents[holderToCollectionPos(pos)].let { editComponent ->
            if (editComponent is EditLoop) {
                cachedEndMarker = editComponent.endMarker
            }
        }
    }

    private fun removeCachedEndMarker() {
        cachedEndMarker?.let {
            val index = editSequence.editComponents.indexOf(it)
            if (index >= 0) {
                removeAt(collectionToHolderPos(index))
            }
            clearCachedEndMarker()
        }
    }

    private fun clearCachedEndMarker() {
        cachedEndMarker = null
    }

    private fun holderToCollectionPos(holderPos: Int) = holderPos - 1

    private fun collectionToHolderPos(collectionPos: Int) = collectionPos +1

    companion object {
        const val VIEW_TYPE_ADD = 0
        const val VIEW_TYPE_COMPONENT = 1
        //const val VIEW_TYPE_MARKER = 2
        const val VIEW_TYPE_TITLE = 3
    }
}