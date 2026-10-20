package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CoinEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CoinDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(coin: CoinEntity)

    @Query("SELECT * FROM coins ORDER BY earnedAt DESC")
    fun getAllCoins(): Flow<List<CoinEntity>>

    @Query("DELETE FROM coins WHERE expiresAt <= :beforeMillis")
    suspend fun deleteExpiredCoins(beforeMillis: Long)

    @Query("SELECT COALESCE(SUM(coinsEarned), 0) FROM coins WHERE expiresAt > :nowMillis")
    suspend fun getTotalActiveCoins(nowMillis: Long): Int
}
