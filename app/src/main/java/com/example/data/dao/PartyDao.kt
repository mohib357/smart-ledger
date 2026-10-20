package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PartyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PartyDao {
    @Query("SELECT * FROM parties ORDER BY updatedAt DESC, id DESC")
    fun getAllParties(): Flow<List<PartyEntity>>

    @Query("SELECT * FROM parties WHERE partyType = :partyType ORDER BY currentAmount DESC")
    fun getPartiesByType(partyType: String): Flow<List<PartyEntity>>

    @Query("SELECT * FROM parties WHERE id = :id")
    fun getPartyById(id: Long): Flow<PartyEntity?>

    @Query("SELECT SUM(currentAmount) FROM parties WHERE partyType = 'PAYABLE'")
    fun getTotalPayables(): Flow<Double?>

    @Query("SELECT SUM(currentAmount) FROM parties WHERE partyType = 'RECEIVABLE'")
    fun getTotalReceivables(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(party: PartyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(parties: List<PartyEntity>)

    @Update
    suspend fun update(party: PartyEntity)

    @Delete
    suspend fun delete(party: PartyEntity)

    @Query("DELETE FROM parties WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM parties")
    suspend fun clearAll()
}
