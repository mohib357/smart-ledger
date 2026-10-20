package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parties")
data class PartyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // ব্যক্তি বা প্রতিষ্ঠানের নাম
    val phone: String = "",
    val partyType: String = "PAYABLE", // "PAYABLE" (সে পাবে / দেনা) বা "RECEIVABLE" (আমি পাবো / পাওনা)
    val currentAmount: Double = 0.0,
    val updatedAt: Long = System.currentTimeMillis(),
    val note: String = ""
)
