package com.healthtrack.app.activities.configuration

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.healthtrack.app.R
import com.healthtrack.app.data.hydrators.InterventionHydrator
import com.healthtrack.app.data.daos.InterventionDao
import com.healthtrack.app.activities.newintervention.NewInterventionActivity
import com.healthtrack.app.services.reminders.ReminderService
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ConfigureTrackedInterventions @Inject constructor(

) : AppCompatActivity() {
    /**
     * Can't do constructor injection with android stuff, I guess.
     */
    @Inject
    lateinit var interventionHydrator: InterventionHydrator
    @Inject
    lateinit var interventionDao: InterventionDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.title_activity_configure_tracked_interventions)
        setContentView(R.layout.activity_configure_tracked_interventions)
        setSupportActionBar(findViewById(R.id.toolbar))

        initializeRecyclerView()
        bindFabToNewIntervention()
        startReminderService()
    }

    private fun startReminderService() {
        //Just... make sure this service starts.
        startService(Intent(this, ReminderService::class.java).apply {
            putExtra(ReminderService.ACTION, ReminderService.Action.STARTUP)
        })
    }

    private fun bindFabToNewIntervention() {
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            val intent = Intent(this, NewInterventionActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initializeRecyclerView() {
        val interventionListAdapter = InterventionListAdapter(listOf(), lifecycleScope) {
            lifecycleScope.launch(Dispatchers.IO) { interventionDao.delete(it) }

            startService(
                Intent(
                    this@ConfigureTrackedInterventions,
                    ReminderService::class.java
                ).apply {
                    putExtra(ReminderService.ACTION, ReminderService.Action.REMOVE_NOTIFICATION)
                    putExtra(ReminderService.NOTIFICATION_ID, it.id!!.toInt())
                })

        }

        lifecycleScope.launch(Dispatchers.IO) {
            val interventions = interventionHydrator.getAllInterventions()
            lifecycleScope.launch(Dispatchers.Main) { //Can't contact the DB on the main thread, can't observe on the IO thread.  Jesus.
                interventions.observe(this@ConfigureTrackedInterventions) {
                    interventionListAdapter.accept(it)
                }
            }
        }

        findViewById<RecyclerView>(R.id.interventions).adapter = interventionListAdapter
    }
}