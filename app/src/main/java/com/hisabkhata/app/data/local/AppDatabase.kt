package com.hisabkhata.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hisabkhata.app.data.local.dao.CashBookDao
import com.hisabkhata.app.data.local.dao.CustomerDao
import com.hisabkhata.app.data.local.dao.FamilyExpenseDao
import com.hisabkhata.app.data.local.dao.TransactionDao
import com.hisabkhata.app.data.local.entity.CashBookEntity
import com.hisabkhata.app.data.local.entity.CustomerEntity
import com.hisabkhata.app.data.local.entity.FamilyExpenseEntity
import com.hisabkhata.app.data.local.entity.TransactionEntity

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        CustomerEntity::class,
        TransactionEntity::class,
        FamilyExpenseEntity::class,
        CashBookEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun customerDao(): CustomerDao
    abstract fun transactionDao(): TransactionDao
    abstract fun familyExpenseDao(): FamilyExpenseDao
    abstract fun cashBookDao(): CashBookDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN currency TEXT NOT NULL DEFAULT 'BDT'")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE cash_book_entries ADD COLUMN currency TEXT NOT NULL DEFAULT 'BDT'")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hisab_khata_database"
                )
                .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
