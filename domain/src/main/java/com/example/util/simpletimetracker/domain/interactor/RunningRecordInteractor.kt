package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.domain.repo.BaseRunningRecordRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RunningRecordInteractor @Inject constructor(
    private val runningRecordRepo: BaseRunningRecordRepo
) {

    suspend fun getAll(): List<RunningRecord> = withContext(Dispatchers.IO) {
        runningRecordRepo.getAll()
    }

    suspend fun add(runningRecord: RunningRecord) = withContext(Dispatchers.IO) {
        runningRecordRepo.add(runningRecord)
    }

    suspend fun remove(name: String) = withContext(Dispatchers.IO) {
        runningRecordRepo.remove(name)
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        runningRecordRepo.clear()
    }
}