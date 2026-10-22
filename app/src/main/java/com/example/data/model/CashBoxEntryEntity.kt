package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cash_box_entries")
data class CashBoxEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,
    val amount: Double,
    val note: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val reconciledDate: Long? = null
)
