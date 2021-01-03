package com.healthtrack.app.data.hydrators

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.healthtrack.app.data.daos.InterventionDao
import com.healthtrack.app.data.models.Intervention
import com.healthtrack.app.data.models.schedule.OneTimeSchedule
import javax.inject.Inject

class InterventionHydrator @Inject constructor(
    private val interventionDao: InterventionDao,
    private val polymorphicScheduleRetriever: PolymorphicScheduleRetriever
) {
    fun getAllInterventions(): LiveData<List<Intervention>> {
        return Transformations.switchMap(interventionDao.getAll()) { interventions ->
            Transformations.map(polymorphicScheduleRetriever.getForInterventions(interventions)) { schedules ->
                val scheduleHydrator = ScheduleHydrator(schedules)
                val scheduleLookup = schedules.filter{ it !is OneTimeSchedule || it.parentScheduleId == null }
                    .groupBy{ it.interventionId }
                    .mapValues { entry -> entry.value.map{ it.accept(scheduleHydrator)} }
                interventions.map { intervention ->
                    val i = Intervention(intervention.name)
                    i.id = intervention.id
                    i.schedules = scheduleLookup[i.id] ?: listOf()
                    i
                }
            }
        }
    }


}