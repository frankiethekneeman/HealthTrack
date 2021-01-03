package com.healthtrack.app.data.persistors

import androidx.room.Transaction
import com.healthtrack.app.data.daos.InterventionDao
import com.healthtrack.app.data.models.Intervention
import javax.inject.Inject

class InterventionPersistor @Inject constructor(
    private val interventionDao: InterventionDao,
    private val schedulePersistor: SchedulePersistorVisitor
) {
    @Transaction
    fun persist(intervention: Intervention): Long {
        intervention.id =  interventionDao.insert(intervention)
        intervention.schedules.forEach {
            it.interventionId = intervention.id
            it.id = it.accept(schedulePersistor)
        }
        return intervention.id !!
    }
}
