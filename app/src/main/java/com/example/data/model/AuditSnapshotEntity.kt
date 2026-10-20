package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_snapshots")
data class AuditSnapshotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateMillis: Long = System.currentTimeMillis(),
    val dateDisplay: String, // e.g. "১৯-১০-২০২৬"
    val totalAssets: Double, // আছে
    val totalLiabilities: Double, // পাবে
    val netBalance: Double, // স্থিতি
    val changeAmount: Double = 0.0, // আগের দিনের চেয়ে পরিবর্তন (+/-)
    val note: String = ""
)
