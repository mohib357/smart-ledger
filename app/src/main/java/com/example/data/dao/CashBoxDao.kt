package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CashBoxEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CashBoxDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: CashBoxEntryEntity): Long

    @Query("SELECT * FROM cash_box_entries ORDER BY dateMillis DESC")
    fun getAllEntries(): Flow<List<CashBoxEntryEntity>>

    @Query("SELECT * FROM cash_box_entries WHERE dateMillis >= :startOfDay AND dateMillis < :endOfDay ORDER BY dateMillis DESC")
    fun getTodayEntries(startOfDay: Long, endOfDay: Long): Flow<List<CashBoxEntryEntity>>

    @Query(
        """
        SELECT SUM(
            CASE WHEN type IN ('CASH_IN', 'OWNER_IN') THEN amount
                 ELSE -amount END
        ) FROM cash_box_entries
        """
    )
    fun getCurrentCashBalance(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM cash_box_entries WHERE type = 'CASH_IN' AND dateMillis >= :startOfDay AND dateMillis < :endOfDay")
    fun getTodaySales(startOfDay: Long, endOfDay: Long): Flow<Double?>

    @Delete
    suspend fun delete(entry: CashBoxEntryEntity)
}
