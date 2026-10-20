package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "party_ledger_entries")
data class PartyLedgerEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val partyId: Long,
    val entryType: String, // "GIVE" (টাকা দেওয়া হলো / পরিশোধ) or "TAKE" (টাকা নেওয়া হলো / নতুন দেনা বা পাওনা বৃদ্ধি)
    val amount: Double,
    val dateMillis: Long = System.currentTimeMillis(),
    val note: String = ""
)
