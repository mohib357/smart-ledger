package com.example.data.cloud

import android.content.Context
import android.util.Log
import com.example.data.model.PartyEntity
import com.example.data.model.TransactionEntity
import com.example.data.repository.AccountRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

enum class SyncStatus { IDLE, SYNCING, SYNCED, OFFLINE, ERROR }

class SyncManager(
    private val context: Context,
    private val repository: AccountRepository,
    private val authManager: FirebaseAuthManager
) {
    companion object {
        private const val TAG = "SyncManager"
        private const val COLLECTION_TRANSACTIONS = "transactions"
        private const val COLLECTION_PARTIES = "parties"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @Suppress("unused")
    private var syncJob: Job? = null

    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private var transactionListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var partyListener: com.google.firebase.firestore.ListenerRegistration? = null

    fun startSync() {
        if (!authManager.isSignedIn()) {
            _syncStatus.value = SyncStatus.OFFLINE
            return
        }
        val uid = authManager.getCurrentUid()
        if (uid.isEmpty()) {
            _syncStatus.value = SyncStatus.OFFLINE
            return
        }

        try {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val userDoc = db.collection("users").document(uid)

            // Listen to transactions
            transactionListener = userDoc.collection(COLLECTION_TRANSACTIONS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Transaction listener error", error)
                        _syncStatus.value = SyncStatus.ERROR
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        scope.launch { processTransactionSnapshot(snapshot) }
                    }
                }

            // Listen to parties
            partyListener = userDoc.collection(COLLECTION_PARTIES)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Party listener error", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        scope.launch { processPartySnapshot(snapshot) }
                    }
                }

            _syncStatus.value = SyncStatus.SYNCED
            Log.i(TAG, "Sync listeners started for uid=$uid")
        } catch (e: Exception) {
            Log.w(TAG, "Firestore not available, sync disabled", e)
            _syncStatus.value = SyncStatus.OFFLINE
        }
    }

    fun stopSync() {
        try {
            transactionListener?.remove()
            partyListener?.remove()
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping listeners", e)
        }
        transactionListener = null
        partyListener = null
        _syncStatus.value = SyncStatus.IDLE
    }

    suspend fun pushAllToFirestore() {
        if (!authManager.isSignedIn()) return
        val uid = authManager.getCurrentUid()
        if (uid.isEmpty()) return

        _syncStatus.value = SyncStatus.SYNCING
        try {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val userDoc = db.collection("users").document(uid)

            val transactions = repository.allTransactions.first()
            val batch = db.batch()
            val txCollection = userDoc.collection(COLLECTION_TRANSACTIONS)

            // Firestore batch limit is 500
            transactions.take(400).forEach { tx ->
                val data = mapOf(
                    "id" to tx.id,
                    "type" to tx.type,
                    "amount" to tx.amount,
                    "title" to tx.title,
                    "note" to tx.note,
                    "dateMillis" to tx.dateMillis,
                    "category" to tx.category,
                    "walletId" to tx.walletId,
                    "walletName" to tx.walletName,
                    "modifiedAt" to System.currentTimeMillis()
                )
                batch.set(txCollection.document(tx.id.toString()), data)
            }
            batch.commit().await()

            _syncStatus.value = SyncStatus.SYNCED
            Log.i(TAG, "Push completed")
        } catch (e: Exception) {
            Log.w(TAG, "Push failed", e)
            _syncStatus.value = SyncStatus.ERROR
        }
    }

    private suspend fun processTransactionSnapshot(
        snapshot: com.google.firebase.firestore.QuerySnapshot
    ) {
        try {
            _syncStatus.value = SyncStatus.SYNCING
            val localTransactions = repository.allTransactions.first()
            val localMap = localTransactions.associateBy { it.id }

            snapshot.documentChanges.forEach { change ->
                val doc = change.document
                val remoteModifiedAt = doc.getLong("modifiedAt") ?: 0L
                val txId = doc.getLong("id") ?: return@forEach

                when (change.type) {
                    com.google.firebase.firestore.DocumentChange.Type.ADDED,
                    com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                        val local = localMap[txId]
                        // Last-write-wins: only update if remote is newer
                        val localModifiedAt = local?.dateMillis ?: 0L
                        if (local == null || remoteModifiedAt > localModifiedAt) {
                            val tx = TransactionEntity(
                                id = txId,
                                type = doc.getString("type") ?: "EXPENSE",
                                amount = doc.getDouble("amount") ?: 0.0,
                                title = doc.getString("title") ?: "",
                                note = doc.getString("note") ?: "",
                                dateMillis = doc.getLong("dateMillis") ?: System.currentTimeMillis(),
                                category = doc.getString("category") ?: "সাধারণ",
                                walletId = doc.getLong("walletId") ?: 1L,
                                walletName = doc.getString("walletName") ?: "ক্যাশ"
                            )
                            repository.upsertTransaction(tx)
                        }
                    }
                    com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                        localMap[txId]?.let { repository.deleteTransaction(it) }
                    }
                }
            }
            _syncStatus.value = SyncStatus.SYNCED
        } catch (e: Exception) {
            Log.e(TAG, "processTransactionSnapshot failed", e)
            _syncStatus.value = SyncStatus.ERROR
        }
    }

    private suspend fun processPartySnapshot(
        snapshot: com.google.firebase.firestore.QuerySnapshot
    ) {
        try {
            val localParties = repository.allParties.first()
            val localMap = localParties.associateBy { it.id }

            snapshot.documentChanges.forEach { change ->
                val doc = change.document
                val partyId = doc.getLong("id") ?: return@forEach
                val remoteModifiedAt = doc.getLong("modifiedAt") ?: 0L

                when (change.type) {
                    com.google.firebase.firestore.DocumentChange.Type.ADDED,
                    com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                        val local = localMap[partyId]
                        // Last-write-wins: compare modifiedAt vs local updatedAt
                        val localUpdatedAt = local?.updatedAt ?: 0L
                        if (local == null || remoteModifiedAt > localUpdatedAt) {
                            val party = PartyEntity(
                                id = partyId,
                                name = doc.getString("name") ?: "",
                                phone = doc.getString("phone") ?: "",
                                partyType = doc.getString("partyType") ?: "PAYABLE",
                                currentAmount = doc.getDouble("currentAmount") ?: 0.0,
                                updatedAt = remoteModifiedAt,
                                note = doc.getString("note") ?: ""
                            )
                            repository.upsertParty(party)
                        }
                    }
                    com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                        localMap[partyId]?.let { repository.deleteParty(it) }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "processPartySnapshot failed", e)
        }
    }

    private suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T =
        kotlinx.coroutines.suspendCancellableCoroutine { cont ->
            addOnSuccessListener { cont.resume(it) }
            addOnFailureListener { cont.resumeWithException(it) }
        }
}
