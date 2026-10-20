package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.cloud.CloudBackupManager
import com.example.data.cloud.FirebaseAuthManager
import com.example.data.database.AppDatabase
import com.example.data.repository.AccountRepository

class CloudBackupWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    companion object {
        const val TAG = "cloud_backup"
        private const val LOG_TAG = "CloudBackupWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getDatabase(applicationContext)
            val repository = AccountRepository(db)
            val authManager = FirebaseAuthManager(applicationContext)
            val backupManager = CloudBackupManager(applicationContext, repository, authManager)

            if (!backupManager.isCloudBackupEnabled) {
                Log.i(LOG_TAG, "Cloud backup disabled, skipping")
                return Result.success()
            }
            if (!authManager.isSignedIn()) {
                Log.i(LOG_TAG, "Not signed in, skipping")
                return Result.success()
            }

            val result = backupManager.performBackup()
            if (result.isSuccess) {
                Log.i(LOG_TAG, "Backup successful")
                Result.success()
            } else {
                Log.w(LOG_TAG, "Backup failed: ${result.exceptionOrNull()?.message}")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Worker failed", e)
            Result.failure()
        }
    }
}
