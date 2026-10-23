package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    // FEAT-001: WHERE isDeleted=0 on all live queries
    @Query("SELECT * FROM transactions WHERE isDeleted=0 ORDER BY dateMillis DESC, id DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = :type AND isDeleted=0 ORDER BY dateMillis DESC, id DESC")
    fun getTransactionsByType(type: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isDeleted=0 ORDER BY dateMillis DESC, id DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME' AND isDeleted=0")
    fun getTotalIncome(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE' AND isDeleted=0")
    fun getTotalExpense(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    /**
     * Hard-deletes a transaction row by ID. DEPRECATED — bypasses the recycle bin.
     * Use [softDeleteById] for all user-initiated deletes so the 30-day recovery
     * window is preserved. This method is kept only as a fallback for internal
     * maintenance (e.g., purging already-soft-deleted rows by ID). Do not call
     * it from UI or repository code paths that handle user transactions.
     */
    @Deprecated(
        message = "Bypasses the recycle bin. Use softDeleteById() for user-initiated deletes.",
        replaceWith = ReplaceWith("softDeleteById(id, System.currentTimeMillis())")
    )
    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun clearAll()

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE type = 'INCOME' AND isDeleted=0 AND dateMillis >= :fromMillis AND dateMillis <= :toMillis")
    suspend fun getTotalIncomeInRange(fromMillis: Long, toMillis: Long): Double

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE type = 'EXPENSE' AND isDeleted=0 AND dateMillis >= :fromMillis AND dateMillis <= :toMillis")
    suspend fun getTotalExpenseInRange(fromMillis: Long, toMillis: Long): Double

    // FEAT-001: Soft-delete queries
    @Query("UPDATE transactions SET isDeleted=1, deletedAt=:deletedAt WHERE id=:id")
    suspend fun softDeleteById(id: Long, deletedAt: Long)

    @Query("SELECT * FROM transactions WHERE isDeleted=1 ORDER BY deletedAt DESC")
    fun getDeletedTransactions(): Flow<List<TransactionEntity>>

    @Query("DELETE FROM transactions WHERE isDeleted=1 AND deletedAt < :before")
    suspend fun purgeOldDeleted(before: Long)

    @Query("UPDATE transactions SET isDeleted=0, deletedAt=NULL WHERE id=:id")
    suspend fun restoreTransaction(id: Long)
}
