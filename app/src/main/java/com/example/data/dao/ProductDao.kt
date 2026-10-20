package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(p: ProductEntity): Long

    @Update
    suspend fun update(p: ProductEntity)

    @Delete
    suspend fun delete(p: ProductEntity)

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE expiryDate IS NOT NULL AND expiryDate <= :beforeMillis AND expiryDate > 0")
    fun getExpiringProducts(beforeMillis: Long): Flow<List<ProductEntity>>
}
