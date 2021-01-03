package com.healthtrack.app.activities.configuration

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.healthtrack.app.R
import com.healthtrack.app.data.models.Intervention
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InterventionListAdapter(
    private var interventions: List<Intervention>,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val deleteHandler: (Intervention) -> Unit
) : RecyclerView.Adapter<InterventionListAdapter.InterventionHolder>() {
    class InterventionHolder(view: View): RecyclerView.ViewHolder(view) {
        val nameView: TextView = view.findViewById(R.id.name_view)
        val schedulesView: RecyclerView = view.findViewById(R.id.schedulesRecycler)
        val deleteButton: ImageButton = view.findViewById(R.id.imageButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InterventionHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.intervention_item_layout, parent, false)
        return InterventionHolder(view)
    }

    override fun onBindViewHolder(holder: InterventionHolder, position: Int) {
        holder.nameView.text = interventions[position].name
        holder.schedulesView.adapter = ScheduleBulletsAdapter(interventions[position].schedules)
        holder.deleteButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                deleteHandler(interventions[position])
            }
        }
    }

    override fun getItemCount() = interventions.size

    fun accept(newList: List<Intervention>?) {
        this.interventions = newList ?: listOf()
        notifyDataSetChanged()
    }
}