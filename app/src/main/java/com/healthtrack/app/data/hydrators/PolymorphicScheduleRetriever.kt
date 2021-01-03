package com.healthtrack.app.data.hydrators

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import com.healthtrack.app.data.daos.schedule.*
import com.healthtrack.app.data.models.Intervention
import com.healthtrack.app.data.models.schedule.Schedule
import javax.inject.Inject
import kotlin.reflect.KClass

@JvmSuppressWildcards
class PolymorphicScheduleRetriever @Inject constructor(
    private val scheduleDaos: Set<ScheduleDao<*>>
){

    fun getForInterventions(interventions: Iterable<Intervention>): LiveData<Set<Schedule>> {
        val liveAggregation: MediatorLiveData<Map<KClass<*>, List<Schedule>>> = MediatorLiveData()
        var aggregator = mapOf<KClass<*>, List<Schedule>>()
        val interventionIds = interventions.mapNotNull { it.id }.toSet()
        scheduleDaos.forEach { dao ->
            liveAggregation.addSource(dao.getForInterventions(interventionIds)) {
                aggregator = aggregator + mapOf(dao::class to it)
                if (aggregator.size == scheduleDaos.size) {
                    liveAggregation.value = aggregator
                }
            }
        }
        return Transformations.map(liveAggregation) { it.values.flatten().toSet() }
    }

}