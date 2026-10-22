package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.database.AppDatabase
import com.example.data.repository.AccountRepository

class RecycleBinPurgeWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    companion object {
        const val TAG = "recycle_bin_purge"
        private const val LOG_TAG = "RecycleBinPurgeWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getDatabase(applicationContext)
            val repository = AccountRepository(db)
            repository.purgeOldDeletedTransactions()
            Log.i(LOG_TAG, "Purge completed")
            Result.success()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Purge failed", e)
            Result.failure()
        }
    }
}
