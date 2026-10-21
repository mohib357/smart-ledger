package com.example.data.repository

import com.example.data.dao.AuditSnapshotDao
import com.example.data.dao.ChatMessageDao
import com.example.data.dao.NomineeDao
import com.example.data.dao.PartyDao
import com.example.data.dao.PartyLedgerEntryDao
import com.example.data.dao.ProductDao
import com.example.data.dao.TransactionDao
import com.example.data.dao.WalletDao
import com.example.data.database.AppDatabase
import com.example.data.model.AuditSnapshotEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.NomineeEntity
import com.example.data.model.PartyEntity
import com.example.data.model.PartyLedgerEntryEntity
import com.example.data.model.ProductEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.WalletEntity
import com.example.util.Formatters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class AccountRepository(private val database: AppDatabase) {

    private val transactionDao: TransactionDao = database.transactionDao()
    private val partyDao: PartyDao = database.partyDao()
    private val partyLedgerEntryDao: PartyLedgerEntryDao = database.partyLedgerEntryDao()
    private val walletDao: WalletDao = database.walletDao()
    private val auditSnapshotDao: AuditSnapshotDao = database.auditSnapshotDao()
    private val nomineeDao: NomineeDao = database.nomineeDao()
    private val chatMessageDao: ChatMessageDao = database.chatMessageDao()
    private val productDao: ProductDao = database.productDao()
    private val invoiceDao: com.example.data.dao.InvoiceDao = database.invoiceDao()
    private val coinDao: com.example.data.dao.CoinDao = database.coinDao()

    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allParties: Flow<List<PartyEntity>> = partyDao.getAllParties()
    val allWallets: Flow<List<WalletEntity>> = walletDao.getAllWallets()
    val allSnapshots: Flow<List<AuditSnapshotEntity>> = auditSnapshotDao.getAllSnapshots()
    val allNominees: Flow<List<NomineeEntity>> = nomineeDao.getAllNominees()
    val allChatMessages: Flow<List<ChatMessageEntity>> = chatMessageDao.getAllMessages()

    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val allInvoices: Flow<List<com.example.data.model.InvoiceEntity>> = invoiceDao.getAllInvoices()
    val allCoins: Flow<List<com.example.data.model.CoinEntity>> = coinDao.getAllCoins()

    suspend fun insertChatMessage(content: String, isFromUser: Boolean) {
        chatMessageDao.insertMessage(ChatMessageEntity(content = content, isFromUser = isFromUser))
    }

    suspend fun clearChatMessages() {
        chatMessageDao.deleteAllMessages()
    }

    // Products / Stock Management
    suspend fun addProduct(
        name: String,
        stock: Int,
        buyPrice: Double,
        sellPrice: Double,
        expiryDate: Long?,
        category: String
    ) {
        productDao.insert(
            ProductEntity(
                name = name,
                stock = stock,
                buyPrice = buyPrice,
                sellPrice = sellPrice,
                expiryDate = expiryDate,
                category = category,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateProduct(product: ProductEntity) {
        productDao.update(product)
    }

    suspend fun deleteProduct(product: ProductEntity) {
        productDao.delete(product)
    }

    fun getExpiringProducts(beforeMillis: Long): Flow<List<ProductEntity>> {
        return productDao.getExpiringProducts(beforeMillis)
    }

    val totalIncome: Flow<Double?> = transactionDao.getTotalIncome()
    val totalExpense: Flow<Double?> = transactionDao.getTotalExpense()
    val totalWalletBalance: Flow<Double?> = walletDao.getTotalWalletBalance()
    val totalPayable: Flow<Double?> = partyDao.getTotalPayables()
    val totalReceivable: Flow<Double?> = partyDao.getTotalReceivables()

    // Combined Dashboard Stats
    data class DashboardStats(
        val totalWalletBalance: Double = 0.0,
        val totalReceivable: Double = 0.0,
        val totalPayable: Double = 0.0,
        val totalAssets: Double = 0.0, // Wallets + Receivable
        val netBalance: Double = 0.0, // Assets - Payable (স্থিতি)
        val totalIncome: Double = 0.0,
        val totalExpense: Double = 0.0,
        val surplus: Double = 0.0 // Income - Expense (উদ্বৃত্ত)
    )

    val dashboardStats: Flow<DashboardStats> = combine(
        totalWalletBalance,
        totalReceivable,
        totalPayable,
        totalIncome,
        totalExpense
    ) { wBal, rec, pay, inc, exp ->
        val wallet = wBal ?: 0.0
        val receivable = rec ?: 0.0
        val payable = pay ?: 0.0
        val income = inc ?: 0.0
        val expense = exp ?: 0.0
        val assets = wallet + receivable
        val net = assets - payable
        val sur = income - expense
        DashboardStats(
            totalWalletBalance = wallet,
            totalReceivable = receivable,
            totalPayable = payable,
            totalAssets = assets,
            netBalance = net,
            totalIncome = income,
            totalExpense = expense,
            surplus = sur
        )
    }

    suspend fun addTransaction(
        type: String,
        title: String,
        amount: Double,
        category: String,
        walletId: Long,
        dateMillis: Long,
        note: String
    ) {
        val wallet = walletDao.getWalletById(walletId)
        val walletName = wallet?.name ?: "ক্যাশ"

        val transaction = TransactionEntity(
            type = type,
            title = title,
            amount = amount,
            category = category,
            walletId = walletId,
            walletName = walletName,
            dateMillis = dateMillis,
            note = note
        )
        transactionDao.insert(transaction)

        // Adjust wallet balance
        val delta = if (type == "INCOME") amount else -amount
        walletDao.adjustBalance(walletId, delta, System.currentTimeMillis())
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        val reverseDelta = if (transaction.type == "INCOME") -transaction.amount else transaction.amount
        walletDao.adjustBalance(transaction.walletId, reverseDelta, System.currentTimeMillis())
        transactionDao.delete(transaction)
    }

    // Phase 5 — Sync: upsert without wallet balance side-effects (remote data already applied)
    suspend fun upsertTransaction(tx: TransactionEntity) {
        transactionDao.insert(tx) // OnConflictStrategy.REPLACE
    }

    suspend fun upsertParty(party: PartyEntity) {
        partyDao.insert(party) // OnConflictStrategy.REPLACE
    }

    suspend fun addParty(
        name: String,
        phone: String,
        partyType: String,
        initialAmount: Double,
        note: String
    ): Long {
        val party = PartyEntity(
            name = name,
            phone = phone,
            partyType = partyType,
            currentAmount = initialAmount,
            updatedAt = System.currentTimeMillis(),
            note = note
        )
        val partyId = partyDao.insert(party)
        if (initialAmount > 0) {
            partyLedgerEntryDao.insert(
                PartyLedgerEntryEntity(
                    partyId = partyId,
                    entryType = "TAKE",
                    amount = initialAmount,
                    dateMillis = System.currentTimeMillis(),
                    note = "প্রারম্ভিক ব্যালেন্স"
                )
            )
        }
        return partyId
    }

    suspend fun updateParty(party: PartyEntity) {
        partyDao.update(party)
    }

    suspend fun deleteParty(party: PartyEntity) {
        partyLedgerEntryDao.deleteByPartyId(party.id)
        partyDao.delete(party)
    }

    fun getPartyEntries(partyId: Long): Flow<List<PartyLedgerEntryEntity>> {
        return partyLedgerEntryDao.getEntriesForParty(partyId)
    }

    suspend fun recordPartyTransaction(
        party: PartyEntity,
        entryType: String,
        amount: Double,
        walletId: Long?,
        note: String
    ) {
        val entry = PartyLedgerEntryEntity(
            partyId = party.id,
            entryType = entryType,
            amount = amount,
            dateMillis = System.currentTimeMillis(),
            note = note
        )
        partyLedgerEntryDao.insert(entry)

        val newAmount = if (party.partyType == "PAYABLE") {
            if (entryType == "GIVE") (party.currentAmount - amount).coerceAtLeast(0.0)
            else party.currentAmount + amount
        } else {
            if (entryType == "GIVE") (party.currentAmount - amount).coerceAtLeast(0.0)
            else party.currentAmount + amount
        }

        partyDao.update(party.copy(currentAmount = newAmount, updatedAt = System.currentTimeMillis()))

        if (walletId != null) {
            val walletDelta = if (party.partyType == "PAYABLE") {
                if (entryType == "GIVE") -amount else amount
            } else {
                if (entryType == "GIVE") amount else -amount
            }
            walletDao.adjustBalance(walletId, walletDelta, System.currentTimeMillis())
        }
    }

    // Nominee / Legacy Sharing Management
    suspend fun addNominee(
        name: String,
        relation: String,
        phone: String,
        email: String,
        accessScope: String,
        canViewCash: Boolean,
        note: String
    ): Long {
        val rawKey = UUID.randomUUID().toString().replace("-", "").take(8).uppercase()
        val key = "HSL-$rawKey"
        val nominee = NomineeEntity(
            name = name,
            relation = relation,
            phone = phone,
            email = email,
            accessScope = accessScope,
            canViewCash = canViewCash,
            accessKey = key,
            note = note,
            createdAt = System.currentTimeMillis()
        )
        return nomineeDao.insert(nominee)
    }

    suspend fun updateNominee(nominee: NomineeEntity) {
        nomineeDao.update(nominee)
    }

    suspend fun deleteNominee(nominee: NomineeEntity) {
        nomineeDao.delete(nominee)
    }

    suspend fun getNomineeByKey(key: String): NomineeEntity? {
        return nomineeDao.getNomineeByKey(key.trim().uppercase())
    }

    suspend fun uploadNomineeAccessKeys(uid: String) {
        try {
            val nominees = nomineeDao.getAllNominees().first()
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            nominees.forEach { nominee ->
                if (nominee.accessKey.isNotBlank()) {
                    val data = mapOf(
                        "ownerUid" to uid,
                        "accessScope" to nominee.accessScope,
                        "canViewCash" to nominee.canViewCash,
                        "name" to nominee.name
                    )
                    db.collection("nominees_public")
                      .document(nominee.accessKey)
                      .set(data)
                      .await()
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("AccountRepository", "uploadNomineeAccessKeys failed", e)
        }
    }

    // Wallets
    suspend fun addWallet(name: String, initialBalance: Double, iconType: String): Long {
        return walletDao.insert(
            WalletEntity(
                name = name,
                balance = initialBalance,
                iconType = iconType,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateWallet(wallet: WalletEntity) {
        walletDao.update(wallet)
    }

    suspend fun deleteWallet(wallet: WalletEntity) {
        walletDao.delete(wallet)
    }

    suspend fun transferBetweenWallets(fromWalletId: Long, toWalletId: Long, amount: Double) {
        walletDao.adjustBalance(fromWalletId, -amount, System.currentTimeMillis())
        walletDao.adjustBalance(toWalletId, amount, System.currentTimeMillis())
    }

    suspend fun recordAuditSnapshot(note: String = "") {
        val stats = dashboardStats.first()
        val latest = auditSnapshotDao.getAllSnapshots().first().firstOrNull()
        val change = if (latest != null) stats.netBalance - latest.netBalance else 0.0
        val snapshot = AuditSnapshotEntity(
            dateMillis = System.currentTimeMillis(),
            dateDisplay = Formatters.formatDate(System.currentTimeMillis()),
            totalAssets = stats.totalAssets,
            totalLiabilities = stats.totalPayable,
            netBalance = stats.netBalance,
            changeAmount = change,
            note = note
        )
        auditSnapshotDao.insert(snapshot)
    }

    suspend fun clearAllData() {
        transactionDao.clearAll()
        partyDao.clearAll()
        partyLedgerEntryDao.clearAll()
        walletDao.clearAll()
        auditSnapshotDao.clearAll()
        nomineeDao.clearAll()
    }

    // ── Coin CRUD ───────────────────────────────────────────────────────────
    suspend fun addCoins(count: Int) {
        val now = System.currentTimeMillis()
        coinDao.insert(
            com.example.data.model.CoinEntity(
                coinsEarned = count,
                earnedAt = now,
                expiresAt = now + 5L * 24 * 60 * 60 * 1000L  // 5 days per coin
            )
        )
    }

    suspend fun cleanExpiredCoins() {
        coinDao.deleteExpiredCoins(System.currentTimeMillis())
    }

    suspend fun getTotalActiveCoins(): Int {
        return coinDao.getTotalActiveCoins(System.currentTimeMillis())
    }

    // ── Invoice CRUD ────────────────────────────────────────────────────────
    suspend fun createInvoice(
        customerName: String,
        customerPhone: String,
        items: List<com.example.data.model.InvoiceItemEntity>,
        note: String
    ): Long {
        val total = items.sumOf { it.totalPrice }
        val dateStr = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US)
            .format(java.util.Date())
        val suffix = (1000..9999).random()
        val invoiceNumber = "INV-$dateStr-$suffix"
        val invoice = com.example.data.model.InvoiceEntity(
            invoiceNumber = invoiceNumber,
            customerName = customerName,
            customerPhone = customerPhone,
            totalAmount = total,
            dateMillis = System.currentTimeMillis(),
            note = note
        )
        invoiceDao.insertInvoiceWithItems(invoice, items)
        return invoiceDao.getInvoiceCount().toLong()
    }

    suspend fun deleteInvoice(invoice: com.example.data.model.InvoiceEntity) {
        invoiceDao.deleteInvoice(invoice)
    }

    suspend fun getItemsForInvoice(invoiceId: Long): List<com.example.data.model.InvoiceItemEntity> {
        return invoiceDao.getItemsForInvoice(invoiceId)
    }

    suspend fun initializeDefaultWalletsIfEmpty() {
        val currentParties = partyDao.getAllParties().first()
        val currentWallets = walletDao.getAllWallets().first()
        if (currentParties.isEmpty() && currentWallets.isEmpty()) {
            prepopulateSampleData()
        } else if (currentWallets.isEmpty()) {
            walletDao.insertAll(
                listOf(
                    WalletEntity(name = "ক্যাশ টাকা", balance = 0.0, iconType = "CASH"),
                    WalletEntity(name = "বিকাশ", balance = 0.0, iconType = "BKASH"),
                    WalletEntity(name = "নগদ", balance = 0.0, iconType = "NAGAD"),
                    WalletEntity(name = "রকেট", balance = 0.0, iconType = "ROCKET"),
                    WalletEntity(name = "ব্যাংক একাউন্ট", balance = 0.0, iconType = "BANK")
                )
            )
        }
    }

    // Export / Import JSON Backup
    suspend fun exportDatabaseToJson(): String {
        val transactions = allTransactions.first()
        val parties = allParties.first()
        val wallets = allWallets.first()
        val snapshots = allSnapshots.first()
        val nominees = allNominees.first()

        val root = JSONObject()
        root.put("version", 2)
        root.put("exportTime", System.currentTimeMillis())
        root.put("appName", "Hisab - Smart Ledger")

        val txArray = JSONArray()
        transactions.forEach { tx ->
            val obj = JSONObject()
            obj.put("type", tx.type)
            obj.put("title", tx.title)
            obj.put("amount", tx.amount)
            obj.put("category", tx.category)
            obj.put("walletId", tx.walletId)
            obj.put("walletName", tx.walletName)
            obj.put("dateMillis", tx.dateMillis)
            obj.put("note", tx.note)
            txArray.put(obj)
        }
        root.put("transactions", txArray)

        val partyArray = JSONArray()
        parties.forEach { p ->
            val obj = JSONObject()
            obj.put("name", p.name)
            obj.put("phone", p.phone)
            obj.put("partyType", p.partyType)
            obj.put("currentAmount", p.currentAmount)
            obj.put("note", p.note)
            partyArray.put(obj)
        }
        root.put("parties", partyArray)

        val walletArray = JSONArray()
        wallets.forEach { w ->
            val obj = JSONObject()
            obj.put("name", w.name)
            obj.put("balance", w.balance)
            obj.put("iconType", w.iconType)
            walletArray.put(obj)
        }
        root.put("wallets", walletArray)

        val snapshotArray = JSONArray()
        snapshots.forEach { s ->
            val obj = JSONObject()
            obj.put("dateMillis", s.dateMillis)
            obj.put("dateDisplay", s.dateDisplay)
            obj.put("totalAssets", s.totalAssets)
            obj.put("totalLiabilities", s.totalLiabilities)
            obj.put("netBalance", s.netBalance)
            obj.put("changeAmount", s.changeAmount)
            obj.put("note", s.note)
            snapshotArray.put(obj)
        }
        root.put("snapshots", snapshotArray)

        val nomineeArray = JSONArray()
        nominees.forEach { n ->
            val obj = JSONObject()
            obj.put("name", n.name)
            obj.put("relation", n.relation)
            obj.put("phone", n.phone)
            obj.put("email", n.email)
            obj.put("accessScope", n.accessScope)
            obj.put("canViewCash", n.canViewCash)
            obj.put("accessKey", n.accessKey)
            obj.put("note", n.note)
            nomineeArray.put(obj)
        }
        root.put("nominees", nomineeArray)

        return root.toString(2)
    }

    suspend fun restoreDatabaseFromJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            clearAllData()

            val walletArray = root.optJSONArray("wallets")
            if (walletArray != null) {
                for (i in 0 until walletArray.length()) {
                    val obj = walletArray.getJSONObject(i)
                    walletDao.insert(
                        WalletEntity(
                            name = obj.getString("name"),
                            balance = obj.getDouble("balance"),
                            iconType = obj.optString("iconType", "CASH")
                        )
                    )
                }
            } else {
                initializeDefaultWalletsIfEmpty()
            }

            val txArray = root.optJSONArray("transactions")
            if (txArray != null) {
                for (i in 0 until txArray.length()) {
                    val obj = txArray.getJSONObject(i)
                    transactionDao.insert(
                        TransactionEntity(
                            type = obj.getString("type"),
                            title = obj.getString("title"),
                            amount = obj.getDouble("amount"),
                            category = obj.optString("category", "সাধারণ"),
                            walletId = obj.optLong("walletId", 1L),
                            walletName = obj.optString("walletName", "ক্যাশ"),
                            dateMillis = obj.optLong("dateMillis", System.currentTimeMillis()),
                            note = obj.optString("note", "")
                        )
                    )
                }
            }

            val partyArray = root.optJSONArray("parties")
            if (partyArray != null) {
                for (i in 0 until partyArray.length()) {
                    val obj = partyArray.getJSONObject(i)
                    partyDao.insert(
                        PartyEntity(
                            name = obj.getString("name"),
                            phone = obj.optString("phone", ""),
                            partyType = obj.getString("partyType"),
                            currentAmount = obj.getDouble("currentAmount"),
                            note = obj.optString("note", "")
                        )
                    )
                }
            }

            val snapshotArray = root.optJSONArray("snapshots")
            if (snapshotArray != null) {
                for (i in 0 until snapshotArray.length()) {
                    val obj = snapshotArray.getJSONObject(i)
                    auditSnapshotDao.insert(
                        AuditSnapshotEntity(
                            dateMillis = obj.optLong("dateMillis", System.currentTimeMillis()),
                            dateDisplay = obj.getString("dateDisplay"),
                            totalAssets = obj.getDouble("totalAssets"),
                            totalLiabilities = obj.getDouble("totalLiabilities"),
                            netBalance = obj.getDouble("netBalance"),
                            changeAmount = obj.optDouble("changeAmount", 0.0),
                            note = obj.optString("note", "")
                        )
                    )
                }
            }

            val nomineeArray = root.optJSONArray("nominees")
            if (nomineeArray != null) {
                for (i in 0 until nomineeArray.length()) {
                    val obj = nomineeArray.getJSONObject(i)
                    nomineeDao.insert(
                        NomineeEntity(
                            name = obj.getString("name"),
                            relation = obj.optString("relation", ""),
                            phone = obj.optString("phone", ""),
                            email = obj.optString("email", ""),
                            accessScope = obj.optString("accessScope", "ALL"),
                            canViewCash = obj.optBoolean("canViewCash", true),
                            accessKey = obj.optString("accessKey", "HSL-" + UUID.randomUUID().toString().take(6).uppercase()),
                            note = obj.optString("note", "")
                        )
                    )
                }
            }

            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun exportToExcel(context: android.content.Context): android.net.Uri {
        val txList = allTransactions.first()
        val partyList = allParties.first()
        val productList = allProducts.first()
        return com.example.util.ExcelExporter.exportToXlsx(context, txList, partyList, productList)
    }

    suspend fun exportTransactionsToCsv(): String {
        val list = allTransactions.first()
        val sb = StringBuilder()
        sb.append("ID,Type,Title,Category,Amount,Wallet,Date,Note\n")
        list.forEach { tx ->
            val dateStr = Formatters.formatDate(tx.dateMillis, useBengali = false)
            sb.append(
                listOf(
                    tx.id.toString(),
                    tx.type,
                    tx.title,
                    tx.category,
                    tx.amount.toString(),
                    tx.walletName,
                    dateStr,
                    tx.note
                ).joinToString(",") { Formatters.escapeCsvField(it) } + "\n"
            )
        }
        return sb.toString()
    }

    suspend fun exportPartiesToCsv(): String {
        val list = allParties.first()
        val sb = StringBuilder()
        sb.append("ID,Name,Type,CurrentAmount,Phone,Note\n")
        list.forEach { p ->
            sb.append(
                listOf(
                    p.id.toString(),
                    p.name,
                    p.partyType,
                    p.currentAmount.toString(),
                    p.phone,
                    p.note
                ).joinToString(",") { Formatters.escapeCsvField(it) } + "\n"
            )
        }
        return sb.toString()
    }

    suspend fun prepopulateSampleData() {
        clearAllData()

        // 1. Wallets from User's Sheet
        val cashId = walletDao.insert(WalletEntity(name = "ক্যাশ টাকা", balance = 3000.0, iconType = "CASH"))
        val luxivaId = walletDao.insert(WalletEntity(name = "লাক্সিভা", balance = 30080.0, iconType = "OTHER"))
        val mamstarId = walletDao.insert(WalletEntity(name = "ম্যামস্টার", balance = 13356.0, iconType = "OTHER"))
        val bkashId = walletDao.insert(WalletEntity(name = "বিকাশ", balance = 841.0, iconType = "BKASH"))
        val nagadId = walletDao.insert(WalletEntity(name = "নগদ", balance = 174.0, iconType = "NAGAD"))
        val rocketId = walletDao.insert(WalletEntity(name = "রকেট", balance = 47.0, iconType = "ROCKET"))
        val bankId = walletDao.insert(WalletEntity(name = "ব্যাংক একাউন্ট", balance = 99.0, iconType = "BANK"))

        // 2. Income & Expense entries (Page 1)
        val now = System.currentTimeMillis()
        val oneDay = 86400000L
        transactionDao.insertAll(
            listOf(
                TransactionEntity(type = "INCOME", title = "অক্টোবর ২৫", amount = 330375.0, category = "ব্যবসা আয়", walletId = cashId, walletName = "ক্যাশ", dateMillis = now - 120 * oneDay),
                TransactionEntity(type = "INCOME", title = "নভেম্বর ২৫", amount = 254344.0, category = "ব্যবসা আয়", walletId = cashId, walletName = "ক্যাশ", dateMillis = now - 90 * oneDay),
                TransactionEntity(type = "INCOME", title = "ডিসেম্বর ২৫", amount = 269069.0, category = "ব্যবসা আয়", walletId = cashId, walletName = "ক্যাশ", dateMillis = now - 60 * oneDay),
                TransactionEntity(type = "INCOME", title = "জানুয়ারি ২৬", amount = 359795.0, category = "ব্যবসা আয়", walletId = cashId, walletName = "ক্যাশ", dateMillis = now - 30 * oneDay),
                TransactionEntity(type = "INCOME", title = "ফেব্রুয়ারি ২৬", amount = 162597.0, category = "ব্যবসা আয়", walletId = cashId, walletName = "ক্যাশ", dateMillis = now - 10 * oneDay),
                TransactionEntity(type = "EXPENSE", title = "সেপ্টেম্বর ঘাটতি ২৫", amount = 13963.0, category = "ঘাটতি", walletId = cashId, walletName = "ক্যাশ", dateMillis = now - 130 * oneDay),
                TransactionEntity(type = "EXPENSE", title = "সাপ্লায়ার (অক্টোবর) ২৫", amount = 230064.0, category = "সাপ্লায়ার", walletId = cashId, walletName = "ক্যাশ", dateMillis = now - 110 * oneDay),
                TransactionEntity(type = "EXPENSE", title = "সাপ্লায়ার (নভেম্বর) ২৫", amount = 206669.0, category = "সাপ্লায়ার", walletId = cashId, walletName = "ক্যাশ", dateMillis = now - 85 * oneDay),
                TransactionEntity(type = "EXPENSE", title = "সাপ্লায়ার (ডিসেম্বর) ২৫", amount = 223174.0, category = "সাপ্লায়ার", walletId = cashId, walletName = "ক্যাশ", dateMillis = now - 55 * oneDay),
                TransactionEntity(type = "EXPENSE", title = "সাপ্লায়ার (জানুয়ারি) ২৬", amount = 247494.0, category = "সাপ্লায়ার", walletId = cashId, walletName = "ক্যাশ", dateMillis = now - 25 * oneDay),
                TransactionEntity(type = "EXPENSE", title = "সাপ্লায়ার ও ক্যাশ জমা (ফেব্রু)", amount = 367849.0, category = "সাপ্লায়ার ও জমা", walletId = cashId, walletName = "ক্যাশ", dateMillis = now - 5 * oneDay),
                TransactionEntity(type = "INCOME", title = "টাকা ফেরত", amount = 35450.0, category = "ফেরত", walletId = cashId, walletName = "ক্যাশ", dateMillis = now - 2 * oneDay)
            )
        )

        // 3. Parties (দেনা / পাবে ও পাবো) - Pages 1 & 2
        val partiesList = listOf(
            PartyEntity(name = "ক্যান্টিন পাবে", partyType = "PAYABLE", currentAmount = 122417.0, note = "ক্যান্টিন বকেয়া"),
            PartyEntity(name = "মাসুদ হুজুর পাবে", partyType = "PAYABLE", currentAmount = 40000.0, note = "ব্যক্তিগত ধার"),
            PartyEntity(name = "হা. সাইফুল পাবে", partyType = "PAYABLE", currentAmount = 90000.0),
            PartyEntity(name = "বারাকাহ পাবে", partyType = "PAYABLE", currentAmount = 30450.0),
            PartyEntity(name = "রায়হান হুজুর", partyType = "PAYABLE", currentAmount = 66079.0),
            PartyEntity(name = "আন নুমা", partyType = "PAYABLE", currentAmount = 27900.0),
            PartyEntity(name = "হেমায়েত দোলাভাই", partyType = "PAYABLE", currentAmount = 20000.0),
            PartyEntity(name = "দোলাভাই", partyType = "PAYABLE", currentAmount = 15000.0),
            PartyEntity(name = "এডুকেশন ওয়েব", partyType = "PAYABLE", currentAmount = 10132.0),
            PartyEntity(name = "মাদ্রাসা", partyType = "PAYABLE", currentAmount = 9364.0),
            PartyEntity(name = "নুরসাদ (ঢাকা)", partyType = "PAYABLE", currentAmount = 8000.0),
            PartyEntity(name = "বড় আপা (বারাকাহ)", partyType = "PAYABLE", currentAmount = 6000.0),
            PartyEntity(name = "কবির মামা", partyType = "PAYABLE", currentAmount = 5000.0),
            PartyEntity(name = "আ. সামাদ হুজুর পাবে", partyType = "PAYABLE", currentAmount = 4520.0),
            PartyEntity(name = "সাপ্লায়ার পাবে (নভেম্বর ২৫)", partyType = "PAYABLE", currentAmount = 11064.0),
            PartyEntity(name = "সাপ্লায়ার পাবে (জানুয়ারি ২৬)", partyType = "PAYABLE", currentAmount = 7680.0),
            PartyEntity(name = "সাপ্লায়ার পাবে (অক্টোবর ২৫)", partyType = "PAYABLE", currentAmount = 4436.0),
            PartyEntity(name = "আশা বেকারী পাবে", partyType = "PAYABLE", currentAmount = 4370.0),
            PartyEntity(name = "আবুল হুজুর", partyType = "PAYABLE", currentAmount = 4000.0),
            PartyEntity(name = "ওবাইদুল্লাহ হুজুর", partyType = "PAYABLE", currentAmount = 4000.0),
            PartyEntity(name = "ঘোন্ড কিনেন", partyType = "PAYABLE", currentAmount = 2500.0),
            PartyEntity(name = "তাজাহাট", partyType = "PAYABLE", currentAmount = 2130.0),
            PartyEntity(name = "মদিনা বেকারী পাবে", partyType = "PAYABLE", currentAmount = 2000.0),
            PartyEntity(name = "আব্দুর রশিদ", partyType = "PAYABLE", currentAmount = 2000.0),
            PartyEntity(name = "এস.বি", partyType = "PAYABLE", currentAmount = 1610.0),
            PartyEntity(name = "সাপ্লায়ার পাবে (ডিসেম্বর ২৫)", partyType = "PAYABLE", currentAmount = 1300.0),
            PartyEntity(name = "বাবু", partyType = "PAYABLE", currentAmount = 1000.0),
            PartyEntity(name = "জমি", partyType = "PAYABLE", currentAmount = 400.0),
            PartyEntity(name = "ইমরান হুজুর", partyType = "PAYABLE", currentAmount = 330.0),
            PartyEntity(name = "মা পাবে", partyType = "PAYABLE", currentAmount = 100.0),
            PartyEntity(name = "১৩৮৭", partyType = "PAYABLE", currentAmount = 100.0),
            PartyEntity(name = "হা. মাহমুদ", partyType = "PAYABLE", currentAmount = 18.0),
            PartyEntity(name = "আমার আছে / পাওনা", partyType = "RECEIVABLE", currentAmount = 228608.0, note = "অন্যান্য পাওনা সম্পদ")
        )
        partyDao.insertAll(partiesList)

        // 4. Snapshots from Page 3
        auditSnapshotDao.insertAll(
            listOf(
                AuditSnapshotEntity(dateMillis = now - 180 * oneDay, dateDisplay = "১৭-০৪-২৬", totalAssets = 276164.0, totalLiabilities = 157690.0, netBalance = 18564.0, changeAmount = 212.0),
                AuditSnapshotEntity(dateMillis = now - 170 * oneDay, dateDisplay = "২৮-০৪-২৬", totalAssets = 276164.0, totalLiabilities = 157690.0, netBalance = 18880.0, changeAmount = 316.0),
                AuditSnapshotEntity(dateMillis = now - 145 * oneDay, dateDisplay = "২৬-০৫-২৬", totalAssets = 276164.0, totalLiabilities = 157690.0, netBalance = 21084.0, changeAmount = 2204.0),
                AuditSnapshotEntity(dateMillis = now - 105 * oneDay, dateDisplay = "০৫-০৭-২৬", totalAssets = 276164.0, totalLiabilities = 157690.0, netBalance = 23956.0, changeAmount = 2872.0),
                AuditSnapshotEntity(dateMillis = now - 98 * oneDay, dateDisplay = "১২-০৭-২৬", totalAssets = 276164.0, totalLiabilities = 157690.0, netBalance = 22399.0, changeAmount = -1557.0),
                AuditSnapshotEntity(dateMillis = now - 97 * oneDay, dateDisplay = "১৩-০৭-২৬", totalAssets = 276164.0, totalLiabilities = 157690.0, netBalance = 15625.0, changeAmount = -6774.0),
                AuditSnapshotEntity(dateMillis = now - 91 * oneDay, dateDisplay = "১৯-০৭-২৬", totalAssets = 276164.0, totalLiabilities = 157690.0, netBalance = 19116.0, changeAmount = 3491.0),
                AuditSnapshotEntity(dateMillis = now - 15 * oneDay, dateDisplay = "০৪-১০-২৬", totalAssets = 276164.0, totalLiabilities = 157690.0, netBalance = 20952.0, changeAmount = 1836.0),
                AuditSnapshotEntity(dateMillis = now, dateDisplay = "১৯-১০-২৬", totalAssets = 276164.0, totalLiabilities = 157690.0, netBalance = 27252.0, changeAmount = 6300.0, note = "আজকের হিসাব সমাপ্ত")
            )
        )

        // 5. Pre-seeded Sample Nominees (The user's requested feature)
        nomineeDao.insertAll(
            listOf(
                NomineeEntity(
                    name = "আব্দুর রহমান",
                    relation = "বড় ভাই",
                    phone = "01711000000",
                    email = "rahman@example.com",
                    accessScope = "PAYABLE_ONLY",
                    canViewCash = false,
                    accessKey = "HSL-BR7890",
                    note = "জরুরি পরিস্থিতিতে আমার দেনা ও বকেয়া ঋণগুলো দ্রুত পরিশোধে দায়িত্ব পালন করবেন।"
                ),
                NomineeEntity(
                    name = "ফাতেমা বেগম",
                    relation = "স্ত্রী",
                    phone = "01811000000",
                    email = "fatema@example.com",
                    accessScope = "ALL",
                    canViewCash = true,
                    accessKey = "HSL-FM4321",
                    note = "আমার সম্পূর্ণ পারিবারিক হিসাব, পাওনা ও সম্পদের পূর্ণ তত্ত্বাবধান করবেন।"
                )
            )
        )
    }
}
