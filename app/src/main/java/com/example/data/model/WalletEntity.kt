package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // e.g. "ক্যাশ টাকা", "বিকাশ", "নগদ", "রকেট", "ব্যাংক একাউন্ট"
    val balance: Double = 0.0,
    val iconType: String = "CASH", // "CASH", "BKASH", "NAGAD", "ROCKET", "BANK", "OTHER"
    val updatedAt: Long = System.currentTimeMillis()
)
