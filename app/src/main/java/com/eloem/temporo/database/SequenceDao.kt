package com.eloem.temporo.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SequenceDao {

    @Insert
    fun insertSerialisedComponents(components: List<SerialisedComponent>)

    @Transaction
    @Query("SELECT * FROM SequenceSql")
    fun getAllSequences(): LiveData<List<SequenceWithComponents>>

    @Transaction
    fun updateSerialisedComponents(components: List<SerialisedComponent>) {
        deleteAllComponentsFromSequence(components.first().sequenceId)
        insertSerialisedComponents(components)
    }

    @Query("DELETE FROM SerialisedComponent WHERE sequenceId = :id")
    fun deleteAllComponentsFromSequence(id: Long)

    @Update
    fun updateSequence(sequence: SequenceSql)

    @Insert
    fun insertSequence(sequence: SequenceSql)

    @Query("DELETE FROM SequenceSql WHERE id = :id")
    fun deleteSequence(id: Long)

    @Transaction
    fun deleteCompleteSequence(id: Long) {
        deleteSequence(id)
        deleteAllComponentsFromSequence(id)
    }

    @Transaction
    fun updateCompleteSequence(sequence: SequenceSql, components:List<SerialisedComponent>) {
        updateSequence(sequence)
        if (components.isNotEmpty()) updateSerialisedComponents(components)
    }

    @Transaction
    fun insertCompleteSequence(sequence: SequenceSql, components:List<SerialisedComponent>) {
        insertSequence(sequence)
        insertSerialisedComponents(components)
    }

    @Insert
    fun insertSerialisedComponent(component: SerialisedComponent)

    @Update
    fun updateSerialisedComponent(component: SerialisedComponent)

    @Delete
    fun deleteSerialisedComponent(component: SerialisedComponent)

    @Update
    fun updateExistingSerialisedComponents(components: List<SerialisedComponent>)
}