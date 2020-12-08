package org.kde.bettercounter

import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.recyclerview.widget.RecyclerView
import org.kde.bettercounter.persistence.Counter


class EntryViewHolder(
    view: View,
    private var viewModel: ViewModel
) : RecyclerView.ViewHolder(view), LifecycleOwner {

    private val countText: TextView = view.findViewById(R.id.count)
    private val nameText: TextView = view.findViewById(R.id.name)
    private val lastEditText : TextView = view.findViewById(R.id.last_edit)
    private val increaseButton: ImageButton = view.findViewById(R.id.btn_increase)
    private val undoButton: ImageButton = view.findViewById(R.id.btn_undo)

    var counter : Counter? = null;

    fun onBind(counter: Counter) {
        this.counter = counter
        increaseButton.setOnClickListener { viewModel.incrementCounter(counter.name) }
        undoButton.setOnClickListener { viewModel.decrementCounter(counter.name) }
        nameText.text = counter.name
        countText.text = counter.count.toString()
        val lastEditDate = counter.lastEdit
        if (lastEditDate != null) {
            lastEditText.text = DateUtils.getRelativeDateTimeString(
                itemView.context,
                lastEditDate.time,
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.WEEK_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE or DateUtils.FORMAT_NUMERIC_DATE)
            undoButton.isEnabled = true
        } else {
            lastEditText.setText(R.string.never)
            undoButton.isEnabled = false
        }
    }

    private var lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
    init {
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }
    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }
}
