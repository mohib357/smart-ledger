package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AuditSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditSnapshotDao {
    @Query("SELECT * FROM audit_snapshots ORDER BY dateMillis DESC, id DESC")
    fun getAllSnapshots(): Flow<List<AuditSnapshotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(snapshot: AuditSnapshotEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(snapshots: List<AuditSnapshotEntity>)

    @Delete
    suspend fun delete(snapshot: AuditSnapshotEntity)

    @Query("DELETE FROM audit_snapshots")
    suspend fun clearAll()
}
