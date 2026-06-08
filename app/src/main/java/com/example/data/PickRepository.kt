package com.example.data

import kotlinx.coroutines.flow.Flow

class PickRepository(private val pickDao: PickDao) {
    val allPicks: Flow<List<Pick>> = pickDao.getAllPicks()

    fun getPickById(id: Long): Flow<Pick?> {
        return pickDao.getPickById(id)
    }

    suspend fun getPickByIdSuspended(id: Long): Pick? {
        return pickDao.getPickByIdSuspended(id)
    }

    suspend fun savePick(pick: Pick): Long {
        return pickDao.insertPick(pick)
    }

    suspend fun deletePick(id: Long) {
        pickDao.deletePickWithHistory(id)
    }

    fun getHistoryForPick(pickId: Long): Flow<List<PickHistoryEntry>> {
        return pickDao.getHistoryForPick(pickId)
    }

    suspend fun recordSelection(pickId: Long, option: String) {
        val entry = PickHistoryEntry(
            pickId = pickId,
            selectedOption = option
        )
        pickDao.insertHistoryEntry(entry)
    }

    suspend fun clearHistory(pickId: Long) {
        pickDao.clearHistoryForPick(pickId)
    }
}
