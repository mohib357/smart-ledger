package com.example.data.cloud

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

enum class BackupStatus { IDLE, SYNCING, SYNCED, ERROR }

class CloudBackupManager(
    private val context: Context,
    private val repository: AccountRepository,
    private val authManager: FirebaseAuthManager
) {
    companion object {
        private const val TAG = "CloudBackupManager"
        private const val PREFS = "app_settings"
        private const val KEY_CLOUD_BACKUP_ENABLED = "cloud_backup_enabled"
        private const val KEY_LAST_BACKUP_TIME = "last_backup_time"
        const val FIRESTORE_COLLECTION = "user_backups"
        const val FIRESTORE_DOCUMENT = "backup_latest"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private val _backupStatus = MutableStateFlow(BackupStatus.IDLE)
    val backupStatus: StateFlow<BackupStatus> = _backupStatus.asStateFlow()

    private val _lastBackupTime = MutableStateFlow(prefs.getLong(KEY_LAST_BACKUP_TIME, 0L))
    val lastBackupTime: StateFlow<Long> = _lastBackupTime.asStateFlow()

    var isCloudBackupEnabled: Boolean
        get() = prefs.getBoolean(KEY_CLOUD_BACKUP_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_CLOUD_BACKUP_ENABLED, value).apply()

    suspend fun performBackup(): Result<Unit> {
        if (!authManager.isSignedIn()) return Result.failure(Exception("Not signed in"))
        val uid = authManager.getCurrentUid()
        if (uid.isEmpty()) return Result.failure(Exception("No UID"))

        _backupStatus.value = BackupStatus.SYNCING
        return try {
            val backupJson = repository.exportDatabaseToJson()

            // Use Firestore: store JSON string in users/{uid}/user_backups/backup_latest
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val docRef = firestore
                .collection("users")
                .document(uid)
                .collection(FIRESTORE_COLLECTION)
                .document(FIRESTORE_DOCUMENT)

            val data = mapOf(
                "json" to backupJson,
                "backupAt" to System.currentTimeMillis(),
                "uid" to uid
            )
            docRef.set(data).await()

            val now = System.currentTimeMillis()
            prefs.edit().putLong(KEY_LAST_BACKUP_TIME, now).apply()
            _lastBackupTime.value = now
            _backupStatus.value = BackupStatus.SYNCED
            Log.i(TAG, "Backup uploaded to Firestore for uid=$uid")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Backup failed", e)
            _backupStatus.value = BackupStatus.ERROR
            Result.failure(e)
        }
    }

    suspend fun performRestore(): Result<Unit> {
        if (!authManager.isSignedIn()) return Result.failure(Exception("Not signed in"))
        val uid = authManager.getCurrentUid()
        if (uid.isEmpty()) return Result.failure(Exception("No UID"))

        _backupStatus.value = BackupStatus.SYNCING
        return try {
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val docRef = firestore
                .collection("users")
                .document(uid)
                .collection(FIRESTORE_COLLECTION)
                .document(FIRESTORE_DOCUMENT)

            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                _backupStatus.value = BackupStatus.ERROR
                return Result.failure(Exception("No backup found"))
            }

            val jsonString = snapshot.getString("json")
                ?: return Result.failure(Exception("Backup data is empty"))

            repository.restoreDatabaseFromJson(jsonString)
            _backupStatus.value = BackupStatus.SYNCED
            Log.i(TAG, "Restore completed from Firestore for uid=$uid")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Restore failed", e)
            _backupStatus.value = BackupStatus.ERROR
            Result.failure(e)
        }
    }
}
