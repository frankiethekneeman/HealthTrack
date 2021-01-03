package com.healthtrack.app.services.reminders

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import com.healthtrack.app.R
import com.healthtrack.app.activities.history.NewHistoricalEvent
import com.healthtrack.app.data.hydrators.ReminderStateHydrator
import com.healthtrack.app.data.models.HistoricalEvent
import com.healthtrack.app.data.models.Intervention
import com.healthtrack.app.data.models.Trigger
import com.healthtrack.app.data.views.ReminderState
import dagger.hilt.android.AndroidEntryPoint
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ReminderService : LifecycleService() {
    enum class Action {
        REMOVE_NOTIFICATION,
        RE_RENDER,
        STARTUP;
    }

    companion object {
        const val NOTIFICATION_ID: String = "NOTIFICATION_ID"
        const val ACTION: String = "ACTION_KEY"

        private val TIME_FORMATTER = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
        private val LEXICOGRAPHIC_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME

        private const val NOTIFICATION_GROUP_SUMMARY_ID = - 1
        private const val PENDING_RE_RENDER_ID = Integer.MAX_VALUE - 1
        private const val MEAL_NOTIFICATION_ID = Integer.MAX_VALUE - 2
        private const val CIRCADIAN_NOTIFICATION_ID = Integer.MAX_VALUE - 3

        //Private signifier values - do not need i18n
        private const val GROUP_KEY = "INTERVENTIONS"
        private const val OVERDUE_CHANNEL_ID = "OVERDUE"
        private const val UPCOMING_CHANNEL_ID = "UPCOMING"

        //Static Render strings.  Need i18n treatment.
        //TODO: Pull these out for i18n
        private const val BED_TIME_NOTIFICATION_TITLE = "Get Ready for Bed"
        private const val OVERDUE_CHANNEL_DESCRIPTION = "Notifications for interventions you should have done, but haven't marked as complete."
        private const val OVERDUE_CHANNEL_TITLE = "Overdue Intervention Notifications"
        private const val OVERDUE_INTERVENTION_TEXT = "Intervention Overdue"
        private const val UPCOMING_INTERVENTIONS_CHANNEL_DESCRIPTION = "Notifications for HealthTrack's best understanding of when you should next perform an intervention"
        private const val UPCOMING_INTERVENTIONS_CHANNEL_TITLE = "Upcoming Interventions"
        private const val WAKE_UP_NOTIFICATION_TITLE = "Trigger Wakeup"
    }

    @Inject
    lateinit var clock: Clock

    @Inject
    lateinit var reminderStateHydrator: ReminderStateHydrator

    private lateinit var reRenderIntent: PendingIntent

    private var state: LiveData<ReminderState>? = null

    override fun onCreate() {
        super.onCreate()
        state?.removeObservers(this) //Not sure the lifecycle here, but just in case.
        state = reminderStateHydrator.getReminderState()
        state?.observe(this) { reRender(it) }
        reRenderIntent = PendingIntent.getService(
            applicationContext,
            PENDING_RE_RENDER_ID,
            Intent(this, ReminderService::class.java).apply {
                putExtra(ACTION, Action.RE_RENDER)
            },
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        getOrCreateOverdueChannel()
        getOrCreateUpcomingChannel()
    }

    @SuppressLint("MissingSuperCall")
    override fun onStartCommand(maybeIntent: Intent?, flags: Int, startId: Int): Int {
        maybeIntent?.let { intent ->
            when (intent.getSerializableExtra(ACTION) as Action?) {
                Action.REMOVE_NOTIFICATION -> removeNotification(
                    intent.getIntExtra(
                        NOTIFICATION_ID,
                        0
                    )
                )
                //Pending intents seem to drop the action.  Not sure why.  Probably using the alarm wrong.
                null, Action.RE_RENDER -> state?.value?.let { reRender(it) }
                Action.STARTUP -> Unit // Noop - just calling to allow a startup.
            }
        }
        return super.onStartCommand(maybeIntent, flags, startId)
    }

    private fun removeNotification(id: Int) {
        with(NotificationManagerCompat.from(this)) {
            cancel(id)
        }
    }

    private fun reRender(state: ReminderState) {
        val nextExpected = state.interventions.map { intervention ->
            intervention to intervention.schedules
                .mapNotNull {
                    it.getNextOccurrence(
                        state.today.events,
                        clock,
                        state.today.startOfDay
                    )
                }
                .minOrNull()
        }.toMap()

        val now = LocalDateTime.now(clock)

        val overdue = overdueNotifications(nextExpected, now)
        val upcomingNotifications = upcomingNotifications(nextExpected, now)
        val nonInterventionNotifications = mapOf(
            MEAL_NOTIFICATION_ID to nextMeal(state.today.events),
            CIRCADIAN_NOTIFICATION_ID to nextCircadianEvent(state.today.events)
        )

        val upcomingSummary = buildGroupedNotification()
            .setGroupSummary(true)

        with(NotificationManagerCompat.from(this)) {
            (overdue + upcomingNotifications + nonInterventionNotifications).forEach {
                if (it.value != null) {
                    notify(it.key, it.value!!.build())
                } else {
                    cancel(it.key)
                }
            }
            notify(NOTIFICATION_GROUP_SUMMARY_ID, upcomingSummary.build())
        }

        bookWakeup(nextExpected, now)
    }

    private fun bookWakeup(
        nextExpected: Map<Intervention, LocalDateTime?>,
        now: LocalDateTime
    ) {
        nextExpected.values
            .filterNotNull()
            .filter { it.isAfter(now) }
            .minOrNull()
            ?.let {
                val alarmManager =
                    applicationContext.getSystemService(ALARM_SERVICE) as AlarmManager
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    it.toInstant(clock).toEpochMilli(),
                    reRenderIntent
                )
            }
    }

    private fun nextCircadianEvent(history: List<HistoricalEvent>): NotificationCompat.Builder {
        val last =
            history.firstOrNull { it.eventType == Trigger.WAKE_UP || it.eventType == Trigger.BED_TIME }?.eventType
        val next = when (last) {
            Trigger.WAKE_UP, null -> Trigger.BED_TIME
            Trigger.BED_TIME -> Trigger.WAKE_UP
            else -> throw IllegalStateException("Unrecognized Circadian Event: $last")
        }
        val title = when (next) {
            Trigger.BED_TIME -> BED_TIME_NOTIFICATION_TITLE
            Trigger.WAKE_UP -> WAKE_UP_NOTIFICATION_TITLE
            else -> throw IllegalStateException("Unrecognized Circadian Event: $last")
        }
        return buildGroupedNotification()
            .setContentTitle(title)
            .setContentIntent(triggerIntent(next))
            .setSortKey(when(next) {
                Trigger.WAKE_UP -> "C"
                Trigger.BED_TIME -> "E"
                else -> throw java.lang.IllegalStateException("Unrecognized Circadian Event: $last")
            })
    }

    private fun nextMeal(history: List<HistoricalEvent>): NotificationCompat.Builder? {
        @Suppress("MoveVariableDeclarationIntoWhen")
        val last = history.firstOrNull { Trigger.MEALS.contains(it.eventType) }?.eventType
        val next = when (last) {
            Trigger.DINNER -> return null
            Trigger.LUNCH -> Trigger.DINNER
            Trigger.BREAKFAST -> Trigger.LUNCH
            null -> Trigger.BREAKFAST
            else -> throw IllegalStateException("Unrecognized Meal: $last")
        }
        return buildGroupedNotification()
            //TODO: Figure out how to do translated string substitution
            .setContentTitle("Next Meal: ${next.toString().toSentenceCase()}")
            .setContentIntent(triggerIntent(next))
            .setSortKey("D")
    }

    private fun upcomingNotifications(
        nextExpected: Map<Intervention, LocalDateTime?>,
        now: LocalDateTime
    ): Map<Int, NotificationCompat.Builder> {
        return nextExpected.filterNot { it.value?.isBefore(now) ?: false }
            .mapValues { entry ->
                entry.value
                    //TODO: Figure out how to do translated string substitution
                    ?.let { "next up at ${TIME_FORMATTER.format(it)}." }
                    ?: entry.key.schedules.joinToString(" ") { it.description() }
            }.mapValues { entry ->
                buildGroupedNotification()
                    .setContentTitle(entry.key.name)
                    .setContentText(entry.value)
                    .setContentIntent(interventionIntent(entry.key.id!!))
                    .setSortKey( when (val t = nextExpected[entry.key]) {
                        null -> "B"
                        else -> "A " + LEXICOGRAPHIC_DATE_TIME_FORMATTER.format(t)
                    })
            }.mapKeys { it.key.id!!.toInt() }
    }

    private fun overdueNotifications(
        nextExpected: Map<Intervention, LocalDateTime?>,
        now: LocalDateTime
    ): Map<Int, NotificationCompat.Builder> {
        return nextExpected.filter { it.value?.isBefore(now) ?: false }
            .mapValues { it.value!! }
            .mapValues {
                baseNotificationBuilder(OVERDUE_CHANNEL_ID)
                    .setContentTitle(it.key.name)
                    .setContentText(OVERDUE_INTERVENTION_TEXT)
                    .setWhen(it.value.toEpochSecond(clock.zone.rules.getOffset(it.value)) * 1000) //... why doesn't instant have to millis?
                    .setContentIntent(interventionIntent(it.key.id!!))
            }.mapKeys {
                it.key.id!!.toInt()
            }
    }

    private fun triggerIntent(trigger: Trigger): PendingIntent {
        return PendingIntent.getActivity(
            applicationContext,
            System.nanoTime().toInt(),
            Intent(this, NewHistoricalEvent::class.java).apply {
                putExtra(NewHistoricalEvent.EVENT_TYPE, trigger)
                addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            },
            PendingIntent.FLAG_CANCEL_CURRENT
        )
    }

    private fun interventionIntent(id: Long): PendingIntent {
        return PendingIntent.getActivity(
            applicationContext,
            System.nanoTime().toInt(),
            Intent(this, NewHistoricalEvent::class.java).apply {
                putExtra(NewHistoricalEvent.EVENT_TYPE, Trigger.INTERVENTION)
                putExtra(NewHistoricalEvent.INTERVENTION_ID, id)
                addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            },
            PendingIntent.FLAG_CANCEL_CURRENT
        )
    }

    private fun getOrCreateUpcomingChannel() {
        val channel = NotificationChannel(
            UPCOMING_CHANNEL_ID,
            UPCOMING_INTERVENTIONS_CHANNEL_TITLE,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = UPCOMING_INTERVENTIONS_CHANNEL_DESCRIPTION
            setShowBadge(false)
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun getOrCreateOverdueChannel() {
        val channel = NotificationChannel(
            OVERDUE_CHANNEL_ID,
            OVERDUE_CHANNEL_TITLE,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = OVERDUE_CHANNEL_DESCRIPTION
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun baseNotificationBuilder(channelId: String = UPCOMING_CHANNEL_ID): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setAutoCancel(false)
    }

    private fun buildGroupedNotification(): NotificationCompat.Builder {
        return baseNotificationBuilder()
            .setGroup(GROUP_KEY)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setNumber(0)
    }
}

//TODO... This... Definitely needs to be i18n'd
private fun String.toSentenceCase(): String {
    return this.substring(0, 1).toUpperCase(Locale.getDefault()) +
            this.substring(1).toLowerCase(Locale.getDefault())
}

private fun LocalDateTime.toInstant(clock: Clock): Instant {
    return this.toInstant(clock.zone.rules.getOffset(this))
}
