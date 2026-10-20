package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    @ColumnInfo(defaultValue = "0")
    val stock: Int = 0,
    @ColumnInfo(defaultValue = "0.0")
    val buyPrice: Double = 0.0,
    @ColumnInfo(defaultValue = "0.0")
    val sellPrice: Double = 0.0,
    @ColumnInfo(name = "expiryDate")
    val expiryDate: Long? = null,
    @ColumnInfo(defaultValue = "সাধারণ")
    val category: String = "সাধারণ",
    val createdAt: Long = System.currentTimeMillis()
)
