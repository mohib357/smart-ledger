package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AuditSnapshotDao
import com.example.data.dao.ChatMessageDao
import com.example.data.dao.InvoiceDao
import com.example.data.dao.NomineeDao
import com.example.data.dao.PartyDao
import com.example.data.dao.PartyLedgerEntryDao
import com.example.data.dao.ProductDao
import com.example.data.dao.TransactionDao
import com.example.data.dao.WalletDao
import com.example.data.model.AuditSnapshotEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.InvoiceEntity
import com.example.data.model.InvoiceItemEntity
import com.example.data.model.NomineeEntity
import com.example.data.model.PartyEntity
import com.example.data.model.PartyLedgerEntryEntity
import com.example.data.model.ProductEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.WalletEntity

@Database(
    entities = [
        TransactionEntity::class,
        PartyEntity::class,
        PartyLedgerEntryEntity::class,
        WalletEntity::class,
        AuditSnapshotEntity::class,
        NomineeEntity::class,
        ChatMessageEntity::class,
        ProductEntity::class,
        InvoiceEntity::class,
        InvoiceItemEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun partyDao(): PartyDao
    abstract fun partyLedgerEntryDao(): PartyLedgerEntryDao
    abstract fun walletDao(): WalletDao
    abstract fun auditSnapshotDao(): AuditSnapshotDao
    abstract fun nomineeDao(): NomineeDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun productDao(): ProductDao
    abstract fun invoiceDao(): InvoiceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `chat_messages` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `content` TEXT NOT NULL, `isFromUser` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL)"
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `products` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `stock` INTEGER NOT NULL DEFAULT 0,
                        `buyPrice` REAL NOT NULL DEFAULT 0.0,
                        `sellPrice` REAL NOT NULL DEFAULT 0.0,
                        `expiryDate` INTEGER,
                        `category` TEXT NOT NULL DEFAULT 'সাধারণ',
                        `createdAt` INTEGER NOT NULL
                    )"""
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `invoices` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `invoiceNumber` TEXT NOT NULL,
                        `customerName` TEXT NOT NULL,
                        `customerPhone` TEXT NOT NULL DEFAULT '',
                        `totalAmount` REAL NOT NULL,
                        `dateMillis` INTEGER NOT NULL,
                        `note` TEXT NOT NULL DEFAULT ''
                    )"""
                )
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `invoice_items` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `invoiceId` INTEGER NOT NULL,
                        `productId` INTEGER NOT NULL,
                        `productName` TEXT NOT NULL,
                        `quantity` INTEGER NOT NULL,
                        `unitPrice` REAL NOT NULL,
                        `totalPrice` REAL NOT NULL,
                        FOREIGN KEY(`invoiceId`) REFERENCES `invoices`(`id`) ON DELETE CASCADE
                    )"""
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shohoj_hisab_database"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
