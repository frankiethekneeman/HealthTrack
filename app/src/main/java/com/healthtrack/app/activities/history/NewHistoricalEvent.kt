package com.healthtrack.app.activities.history

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.healthtrack.app.R
import com.healthtrack.app.data.daos.HistoricalEventsDao
import com.healthtrack.app.data.daos.InterventionDao
import com.healthtrack.app.data.models.HistoricalEvent
import com.healthtrack.app.data.models.Intervention
import com.healthtrack.app.data.models.Trigger
import com.healthtrack.app.services.reminders.ReminderService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Instant
import javax.inject.Inject

@AndroidEntryPoint
class NewHistoricalEvent : AppCompatActivity() {

    @Inject
    lateinit var historicalEventsDao: HistoricalEventsDao
    @Inject
    lateinit var interventionDao: InterventionDao
    @Inject
    lateinit var clock: Clock

    companion object {
        const val EVENT_TYPE = "EVENT_TYPE"
        const val INTERVENTION_ID = "INTERVENTION_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_historical_event)

        unPack{ trigger, intervention ->
            when {
                trigger == null -> finish()
                intervention == null -> confirm(trigger)
                else -> confirm(trigger, intervention)
            }
        }
    }

    private fun startReminderService() {
        startService(Intent(this, ReminderService::class.java).apply {
            putExtra(ReminderService.ACTION, ReminderService.Action.STARTUP)
        })
    }

    private fun unPack(response: (Trigger?, Intervention?) -> Unit) {
        val trigger = intent.getSerializableExtra(EVENT_TYPE) as Trigger?
        val interventionId = intent.getLongExtra(INTERVENTION_ID, -1)
        when {
            trigger == null -> response(null, null)
            trigger != Trigger.INTERVENTION -> response(trigger, null)
            else -> lifecycleScope.launch(Dispatchers.IO) {
                val intervention = interventionDao.get(interventionId)
                lifecycleScope.launch(Dispatchers.Main) { response(trigger, intervention) }
            }
        }
    }

    private fun confirm(trigger: Trigger) {
        confirmDialog(trigger, null)
            .setTitle("Record ${trigger.confirmName}?")
            .show()

    }
    private fun confirm(trigger: Trigger, intervention: Intervention) {
        confirmDialog(trigger, intervention)
            .setTitle("Record a new occurrence of: ${intervention.name}?")
            .show()
    }

    private fun confirmDialog(trigger: Trigger, intervention: Intervention?): AlertDialog.Builder {
        return AlertDialog.Builder(this)
            .setPositiveButton("Yes") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    historicalEventsDao.insert(
                        HistoricalEvent(
                            Instant.now(clock),
                            trigger,
                            intervention?.id
                        )
                    )
                }
            }.setNegativeButton("No", null)
            .setOnDismissListener {
                // The reminder service can sometimes get stopped in the background.  This isn't
                // really a problem, as it schedules itself for updates if there's a next intervention,
                // but when all interventions are waiting on a trigger, it means new events don't cause
                // the notifications to update, as the liveData updates are paused.  So this startup intent
                // is a no-op for an already started service, but it ensures that the reminders are up to
                // date if it got stopped in the background.
                startReminderService()
                finish()
            }

    }


}