package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AuditSnapshotDao
import com.example.data.dao.ChatMessageDao
import com.example.data.dao.NomineeDao
import com.example.data.dao.PartyDao
import com.example.data.dao.PartyLedgerEntryDao
import com.example.data.dao.TransactionDao
import com.example.data.dao.WalletDao
import com.example.data.model.AuditSnapshotEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.NomineeEntity
import com.example.data.model.PartyEntity
import com.example.data.model.PartyLedgerEntryEntity
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
        ChatMessageEntity::class
    ],
    version = 3,
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

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shohoj_hisab_database"
                )
                    .addMigrations(MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
