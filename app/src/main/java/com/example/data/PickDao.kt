package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PickDao {
    @Query("SELECT * FROM picks ORDER BY createdAt DESC")
    fun getAllPicks(): Flow<List<Pick>>

    @Query("SELECT * FROM picks WHERE id = :id")
    fun getPickById(id: Long): Flow<Pick?>

    @Query("SELECT * FROM picks WHERE id = :id")
    suspend fun getPickByIdSuspended(id: Long): Pick?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPick(pick: Pick): Long

    @Query("DELETE FROM picks WHERE id = :id")
    suspend fun deletePickById(id: Long)

    @Query("SELECT * FROM pick_history WHERE pickId = :pickId ORDER BY timestamp DESC")
    fun getHistoryForPick(pickId: Long): Flow<List<PickHistoryEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryEntry(entry: PickHistoryEntry)

    @Query("DELETE FROM pick_history WHERE pickId = :pickId")
    suspend fun clearHistoryForPick(pickId: Long)

    @Transaction
    suspend fun deletePickWithHistory(pickId: Long) {
        clearHistoryForPick(pickId)
        deletePickById(pickId)
    }
}
