package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "INCOME" or "EXPENSE"
    val title: String,
    val amount: Double,
    val category: String = "সাধারণ",
    val walletId: Long = 1,
    val walletName: String = "ক্যাশ",
    val dateMillis: Long = System.currentTimeMillis(),
    val note: String = "",
    // FEAT-001: Soft delete fields
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)
