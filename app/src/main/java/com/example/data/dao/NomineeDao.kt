package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.NomineeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NomineeDao {
    @Query("SELECT * FROM nominees ORDER BY id DESC")
    fun getAllNominees(): Flow<List<NomineeEntity>>

    @Query("SELECT * FROM nominees WHERE id = :id")
    fun getNomineeById(id: Long): Flow<NomineeEntity?>

    @Query("SELECT * FROM nominees WHERE accessKey = :key LIMIT 1")
    suspend fun getNomineeByKey(key: String): NomineeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(nominee: NomineeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(nominees: List<NomineeEntity>)

    @Update
    suspend fun update(nominee: NomineeEntity)

    @Delete
    suspend fun delete(nominee: NomineeEntity)

    @Query("DELETE FROM nominees WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM nominees")
    suspend fun clearAll()
}
