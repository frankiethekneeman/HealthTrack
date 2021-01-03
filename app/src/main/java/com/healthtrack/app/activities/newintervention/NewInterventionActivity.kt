package com.healthtrack.app.activities.newintervention

import android.content.DialogInterface
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.healthtrack.app.R
import com.healthtrack.app.data.models.Intervention
import com.healthtrack.app.data.persistors.InterventionPersistor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NewInterventionActivity : AppCompatActivity() {
    @Inject
    lateinit var interventionPersistor: InterventionPersistor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_intervention)
        setSupportActionBar(findViewById(R.id.toolbar))

        val editableScheduleAdapter = NewScheduleAdapter(mutableListOf())
        findViewById<RecyclerView>(R.id.schedulesRecycler).adapter = editableScheduleAdapter

        findViewById<ImageButton>(R.id.addSchedule).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("What kind of schedule would you like to add?")
                .setItems(
                    NewScheduleAdapter.ScheduleType.values()
                        .map { it.displayName }
                        .toTypedArray()
                ) { _: DialogInterface, which: Int ->
                    editableScheduleAdapter.addSchedule(NewScheduleAdapter.ScheduleType.values()[which])
                }.show()
        }

        findViewById<Button>(R.id.saveButton).setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val newIntervention = Intervention(findViewById<EditText>(R.id.editInterventionName).text.toString())
                newIntervention.schedules = editableScheduleAdapter.getSchedules(newIntervention)
                interventionPersistor.persist(newIntervention)
                finish()
            }
        }
    }
}