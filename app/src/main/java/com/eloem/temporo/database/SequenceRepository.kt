package com.eloem.temporo.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.eloem.temporo.timercomponents.DataComponent
import com.eloem.temporo.timercomponents.DataSequence

class SequenceRepository(private val sequenceDao: SequenceDao) {

    val allTimerSequences: LiveData<List<DataSequence>> = Transformations.map(sequenceDao.getAllSequences()) { list ->
        list.map { sequence ->
            DataSequence(sequence.sequence.id, sequence.sequence.title, sequence.components.toDataComponents())
        }
    }

    fun getTimerSequence(id: Long): LiveData<DataSequence?> = Transformations.map(allTimerSequences) { list ->
        list.find { it.id == id }
    }

    suspend fun updateTimerSequence(sequence: DataSequence) {
        sequenceDao.updateCompleteSequence(sequence.toSqlSequence(), sequence.toSerialisedList())
    }

    suspend fun insertTimerSequence(sequence: DataSequence) {
        sequenceDao.insertCompleteSequence(sequence.toSqlSequence(), sequence.toSerialisedList())
    }

    suspend fun deleteTimerSequence(sequence: DataSequence) {
        sequenceDao.deleteCompleteSequence(sequence.id)
    }

    suspend fun insertTimerComponent(sequenceId: Long, index: Int, component: DataComponent) {
        sequenceDao.insertSerialisedComponent(component.toSerialisedComponent(sequenceId, index))
    }

    suspend fun updateTimerComponent(sequenceId: Long, index: Int, component: DataComponent) {
        sequenceDao.updateSerialisedComponent(component.toSerialisedComponent(sequenceId, index))
    }

    suspend fun deleteTimerComponent(sequenceId: Long, index: Int, component: DataComponent) {
        sequenceDao.deleteSerialisedComponent(component.toSerialisedComponent(sequenceId, index))
    }

    suspend fun updateExistingSerialisedComponents(sequenceId: Long, startIndex: Int, components: List<DataComponent>) {
        sequenceDao.updateExistingSerialisedComponents(components.mapIndexed { indexOffset, dc ->
            dc.toSerialisedComponent(sequenceId, startIndex + indexOffset)
        })
    }

    suspend fun updateSequenceTitle(sequenceId: Long, title: String) {
        sequenceDao.updateSequence(SequenceSql(sequenceId, title))
    }
}