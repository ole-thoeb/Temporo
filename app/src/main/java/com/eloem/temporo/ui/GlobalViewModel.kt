package com.eloem.temporo.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.eloem.temporo.database.SequenceRepository
import com.eloem.temporo.database.SequenceRoomDatabase
import com.eloem.temporo.timercomponents.DataComponent
import com.eloem.temporo.timercomponents.DataSequence
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.anko.defaultSharedPreferences
import kotlin.coroutines.CoroutineContext

class GlobalViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SequenceRepository

    val allTimerSequences: LiveData<List<DataSequence>>

    private fun newId(): Long {
        return getApplication<Application>().defaultSharedPreferences.run {
            val id = getLong(PREFERENCE_KEY_MAX_ID, 0)
            edit().putLong(PREFERENCE_KEY_MAX_ID, id + 1).apply()
            id
        }
    }

    init {
        val chooserDao = SequenceRoomDatabase.getDatabase(getApplication()).sequenceDao()
        repository = SequenceRepository(chooserDao)
        allTimerSequences = repository.allTimerSequences
    }


    fun getTimerSequence(id: Long) = repository.getTimerSequence(id)

    fun newTimerSequence(): DataSequence {
        return DataSequence(newId(), "", emptyList()).also {
            insertTimerSequence(it)
        }
    }

    private fun insertTimerSequence(sequence: DataSequence) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertTimerSequence(sequence)
    }

    fun updateTimerSequence(sequence: DataSequence) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateTimerSequence(sequence)
    }

    fun deleteTimerSequence(sequence: DataSequence) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteTimerSequence(sequence)
    }

    fun insertTimerComponent(sequenceId: Long, index: Int, component: DataComponent) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertTimerComponent(sequenceId, index, component)
    }

    fun updateTimerComponent(sequenceId: Long, index: Int, component: DataComponent) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateTimerComponent(sequenceId, index, component)
    }

    fun deleteTimerComponent(sequenceId: Long, index: Int, component: DataComponent) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteTimerComponent(sequenceId, index, component)
    }

    fun updateTimerComponents(sequenceId: Long, startIndex: Int, components: List<DataComponent>) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateExistingSerialisedComponents(sequenceId, startIndex, components)
    }

    fun updateSequenceTitle(sequenceId: Long, title: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateSequenceTitle(sequenceId, title)
    }

    companion object {
        private const val PREFERENCE_KEY_MAX_ID = "maxIdKey"
    }
}