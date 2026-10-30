package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.WakeUpRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WakeUpRecordDao {
    @Query("SELECT * FROM wakeup_records ORDER BY timestampMillis ASC")
    fun getAllRecordsFlow(): Flow<List<WakeUpRecordEntity>>

    @Query("SELECT * FROM wakeup_records ORDER BY timestampMillis DESC LIMIT :limit")
    fun getRecentRecordsFlow(limit: Int): Flow<List<WakeUpRecordEntity>>

    @Query("SELECT * FROM wakeup_records ORDER BY timestampMillis ASC")
    suspend fun getAllRecordsList(): List<WakeUpRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: WakeUpRecordEntity): Long

    @Query("DELETE FROM wakeup_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("SELECT COUNT(*) FROM wakeup_records")
    suspend fun getRecordCount(): Int

    @Query("DELETE FROM wakeup_records")
    suspend fun clearAllRecords()
}
