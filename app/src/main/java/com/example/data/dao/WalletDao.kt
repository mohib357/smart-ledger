package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.WalletEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallets ORDER BY id ASC")
    fun getAllWallets(): Flow<List<WalletEntity>>

    @Query("SELECT * FROM wallets WHERE id = :id")
    suspend fun getWalletById(id: Long): WalletEntity?

    @Query("SELECT SUM(balance) FROM wallets")
    fun getTotalWalletBalance(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wallet: WalletEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(wallets: List<WalletEntity>)

    @Update
    suspend fun update(wallet: WalletEntity)

    @Delete
    suspend fun delete(wallet: WalletEntity)

    @Query("UPDATE wallets SET balance = balance + :amount, updatedAt = :timestamp WHERE id = :id")
    suspend fun adjustBalance(id: Long, amount: Double, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM wallets")
    suspend fun clearAll()
}
