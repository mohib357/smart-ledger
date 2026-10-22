package com.example.data.cloud

import android.content.Context
import android.util.Log
import com.example.data.model.PartyEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.WalletEntity
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
        private const val COLLECTION_WALLETS = "wallets"
        private const val COLLECTION_CASH_BOX_ENTRIES = "cash_box_entries"
        private const val COLLECTION_PRODUCTS = "products"
        private const val COLLECTION_INVOICES = "invoices"
        private const val COLLECTION_NOMINEES = "nominees"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @Suppress("unused")
    private var syncJob: Job? = null

    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private var transactionListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var partyListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var walletListener: com.google.firebase.firestore.ListenerRegistration? = null

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

            // Listen to wallets
            walletListener = userDoc.collection(COLLECTION_WALLETS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Wallet listener error", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        scope.launch { processWalletSnapshot(snapshot) }
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
            walletListener?.remove()
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping listeners", e)
        }
        transactionListener = null
        partyListener = null
        walletListener = null
        _syncStatus.value = SyncStatus.IDLE
    }

    // ── Step 3: pushAllToFirestore with chunked(400) + isDeleted/deletedAt ──

    suspend fun pushAllToFirestore() {
        if (!authManager.isSignedIn()) return
        val uid = authManager.getCurrentUid()
        if (uid.isEmpty()) return

        _syncStatus.value = SyncStatus.SYNCING
        try {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val userDoc = db.collection("users").document(uid)
            val txCollection = userDoc.collection(COLLECTION_TRANSACTIONS)

            val transactions = repository.allTransactions.first()
            // Step 7: include isDeleted and deletedAt in the map
            transactions.chunked(400).forEach { chunk ->
                val batchChunk = db.batch()
                chunk.forEach { tx ->
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
                        "isDeleted" to tx.isDeleted,
                        "deletedAt" to tx.deletedAt,
                        "modifiedAt" to System.currentTimeMillis()
                    )
                    batchChunk.set(txCollection.document(tx.id.toString()), data)
                }
                batchChunk.commit().await()
            }

            _syncStatus.value = SyncStatus.SYNCED
            Log.i(TAG, "pushAllToFirestore completed")
        } catch (e: Exception) {
            Log.w(TAG, "pushAllToFirestore failed", e)
            _syncStatus.value = SyncStatus.ERROR
        }
    }

    // ── Step 4: pushPartiesToFirestore with chunked(400) ──────────────────

    suspend fun pushPartiesToFirestore() {
        if (!authManager.isSignedIn()) return
        val uid = authManager.getCurrentUid()
        if (uid.isEmpty()) return

        try {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val collection = db.collection("users").document(uid).collection(COLLECTION_PARTIES)
            val parties = repository.allParties.first()
            parties.chunked(400).forEach { chunk ->
                val batch = db.batch()
                chunk.forEach { party ->
                    val data = mapOf(
                        "id" to party.id,
                        "name" to party.name,
                        "phone" to party.phone,
                        "partyType" to party.partyType,
                        "currentAmount" to party.currentAmount,
                        "updatedAt" to party.updatedAt,
                        "note" to party.note,
                        "modifiedAt" to System.currentTimeMillis()
                    )
                    batch.set(collection.document(party.id.toString()), data)
                }
                batch.commit().await()
            }
            Log.i(TAG, "pushPartiesToFirestore completed")
        } catch (e: Exception) {
            Log.w(TAG, "pushPartiesToFirestore failed", e)
        }
    }

    // ── Step 5: push methods for wallets, cash_box_entries, products, invoices, nominees ──

    suspend fun pushWalletsToFirestore() {
        if (!authManager.isSignedIn()) return
        val uid = authManager.getCurrentUid()
        if (uid.isEmpty()) return

        try {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val collection = db.collection("users").document(uid).collection(COLLECTION_WALLETS)
            val wallets = repository.allWallets.first()
            wallets.chunked(400).forEach { chunk ->
                val batch = db.batch()
                chunk.forEach { w ->
                    val data = mapOf(
                        "id" to w.id,
                        "name" to w.name,
                        "balance" to w.balance,
                        "type" to w.iconType,
                        "modifiedAt" to System.currentTimeMillis()
                    )
                    batch.set(collection.document(w.id.toString()), data)
                }
                batch.commit().await()
            }
            Log.i(TAG, "pushWalletsToFirestore completed")
        } catch (e: Exception) {
            Log.w(TAG, "pushWalletsToFirestore failed", e)
        }
    }

    suspend fun pushCashBoxEntriesToFirestore() {
        if (!authManager.isSignedIn()) return
        val uid = authManager.getCurrentUid()
        if (uid.isEmpty()) return

        try {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val collection = db.collection("users").document(uid).collection(COLLECTION_CASH_BOX_ENTRIES)
            val entries = repository.getCashBoxEntries().first()
            entries.chunked(400).forEach { chunk ->
                val batch = db.batch()
                chunk.forEach { c ->
                    val data = mapOf(
                        "id" to c.id,
                        "type" to c.type,
                        "amount" to c.amount,
                        "note" to c.note,
                        "dateMillis" to c.dateMillis,
                        "reconciledDate" to c.reconciledDate,
                        "modifiedAt" to System.currentTimeMillis()
                    )
                    batch.set(collection.document(c.id.toString()), data)
                }
                batch.commit().await()
            }
            Log.i(TAG, "pushCashBoxEntriesToFirestore completed")
        } catch (e: Exception) {
            Log.w(TAG, "pushCashBoxEntriesToFirestore failed", e)
        }
    }

    suspend fun pushProductsToFirestore() {
        if (!authManager.isSignedIn()) return
        val uid = authManager.getCurrentUid()
        if (uid.isEmpty()) return

        try {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val collection = db.collection("users").document(uid).collection(COLLECTION_PRODUCTS)
            val products = repository.allProducts.first()
            products.chunked(400).forEach { chunk ->
                val batch = db.batch()
                chunk.forEach { p ->
                    val data = mapOf(
                        "id" to p.id,
                        "name" to p.name,
                        "stock" to p.stock,
                        "buyPrice" to p.buyPrice,
                        "sellPrice" to p.sellPrice,
                        "expiryDate" to p.expiryDate,
                        "category" to p.category,
                        "createdAt" to p.createdAt,
                        "modifiedAt" to System.currentTimeMillis()
                    )
                    batch.set(collection.document(p.id.toString()), data)
                }
                batch.commit().await()
            }
            Log.i(TAG, "pushProductsToFirestore completed")
        } catch (e: Exception) {
            Log.w(TAG, "pushProductsToFirestore failed", e)
        }
    }

    suspend fun pushInvoicesToFirestore() {
        if (!authManager.isSignedIn()) return
        val uid = authManager.getCurrentUid()
        if (uid.isEmpty()) return

        try {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val collection = db.collection("users").document(uid).collection(COLLECTION_INVOICES)
            val invoices = repository.allInvoices.first()
            invoices.chunked(400).forEach { chunk ->
                val batch = db.batch()
                chunk.forEach { inv ->
                    val data = mapOf(
                        "id" to inv.id,
                        "invoiceNumber" to inv.invoiceNumber,
                        "customerName" to inv.customerName,
                        "customerPhone" to inv.customerPhone,
                        "totalAmount" to inv.totalAmount,
                        "dateMillis" to inv.dateMillis,
                        "note" to inv.note,
                        "modifiedAt" to System.currentTimeMillis()
                    )
                    batch.set(collection.document(inv.id.toString()), data)
                }
                batch.commit().await()
            }
            Log.i(TAG, "pushInvoicesToFirestore completed")
        } catch (e: Exception) {
            Log.w(TAG, "pushInvoicesToFirestore failed", e)
        }
    }

    suspend fun pushNomineesToFirestore() {
        if (!authManager.isSignedIn()) return
        val uid = authManager.getCurrentUid()
        if (uid.isEmpty()) return

        try {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val collection = db.collection("users").document(uid).collection(COLLECTION_NOMINEES)
            val nominees = repository.allNominees.first()
            nominees.chunked(400).forEach { chunk ->
                val batch = db.batch()
                chunk.forEach { n ->
                    val data = mapOf(
                        "id" to n.id,
                        "name" to n.name,
                        "relation" to n.relation,
                        "phone" to n.phone,
                        "email" to n.email,
                        "accessScope" to n.accessScope,
                        "canViewCash" to n.canViewCash,
                        "note" to n.note,
                        "modifiedAt" to System.currentTimeMillis()
                    )
                    batch.set(collection.document(n.id.toString()), data)
                }
                batch.commit().await()
            }
            Log.i(TAG, "pushNomineesToFirestore completed")
        } catch (e: Exception) {
            Log.w(TAG, "pushNomineesToFirestore failed", e)
        }
    }

    // ── Step 7: processTransactionSnapshot handles isDeleted from Firestore ──

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
                val remoteIsDeleted = doc.getBoolean("isDeleted") == true

                when (change.type) {
                    com.google.firebase.firestore.DocumentChange.Type.ADDED,
                    com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                        val local = localMap[txId]
                        val localModifiedAt = local?.dateMillis ?: 0L
                        if (local == null || remoteModifiedAt > localModifiedAt) {
                            if (remoteIsDeleted) {
                                // Remote is deleted — soft-delete locally if not already deleted
                                if (local != null && !local.isDeleted) {
                                    repository.softDeleteTransactionById(txId, doc.getLong("deletedAt") ?: System.currentTimeMillis())
                                }
                            } else {
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
                    }
                    com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                        val local = localMap[txId]
                        if (local != null && !local.isDeleted) {
                            repository.softDeleteTransactionById(txId, System.currentTimeMillis())
                        }
                        // If already soft-deleted locally, no-op
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

    // ── Step 6: wallet snapshot listener ─────────────────────────────────

    private suspend fun processWalletSnapshot(
        snapshot: com.google.firebase.firestore.QuerySnapshot
    ) {
        try {
            val localWallets = repository.allWallets.first()
            val localMap = localWallets.associateBy { it.id }

            snapshot.documentChanges.forEach { change ->
                val doc = change.document
                val walletId = doc.getLong("id") ?: return@forEach
                val remoteModifiedAt = doc.getLong("modifiedAt") ?: 0L

                when (change.type) {
                    com.google.firebase.firestore.DocumentChange.Type.ADDED,
                    com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                        val local = localMap[walletId]
                        val localUpdatedAt = local?.updatedAt ?: 0L
                        if (local == null || remoteModifiedAt > localUpdatedAt) {
                            val wallet = WalletEntity(
                                id = walletId,
                                name = doc.getString("name") ?: "",
                                balance = doc.getDouble("balance") ?: 0.0,
                                iconType = doc.getString("type") ?: "CASH",
                                updatedAt = remoteModifiedAt
                            )
                            repository.upsertWallet(wallet)
                        }
                    }
                    com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                        // Wallets are not hard-deleted remotely; no-op on REMOVED
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "processWalletSnapshot failed", e)
        }
    }

    private suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T =
        suspendCancellableCoroutine { cont ->
            addOnSuccessListener { cont.resume(it) }
            addOnFailureListener { cont.resumeWithException(it) }
        }
}
