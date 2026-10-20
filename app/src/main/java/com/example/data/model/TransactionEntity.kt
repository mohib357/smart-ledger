package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "INCOME" or "EXPENSE"
    val title: String, // খাত / বিবরণ (e.g. "বেতন", "দোকান ভাড়া", "সাপ্লায়ার বিল")
    val amount: Double,
    val category: String = "সাধারণ",
    val walletId: Long = 1,
    val walletName: String = "ক্যাশ",
    val dateMillis: Long = System.currentTimeMillis(),
    val note: String = ""
)
