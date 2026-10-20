package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coins")
data class CoinEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val coinsEarned: Int,
    val earnedAt: Long,
    val expiresAt: Long   // earnedAt + 5 * 24 * 60 * 60 * 1000L
)
