package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.PartyLedgerEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PartyLedgerEntryDao {
    @Query("SELECT * FROM party_ledger_entries WHERE partyId = :partyId ORDER BY dateMillis DESC, id DESC")
    fun getEntriesForParty(partyId: Long): Flow<List<PartyLedgerEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: PartyLedgerEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<PartyLedgerEntryEntity>)

    @Delete
    suspend fun delete(entry: PartyLedgerEntryEntity)

    @Query("DELETE FROM party_ledger_entries WHERE partyId = :partyId")
    suspend fun deleteByPartyId(partyId: Long)

    @Query("DELETE FROM party_ledger_entries")
    suspend fun clearAll()
}
