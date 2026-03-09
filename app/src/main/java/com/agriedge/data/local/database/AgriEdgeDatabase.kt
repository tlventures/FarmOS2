package com.agriedge.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.agriedge.data.local.database.converter.Converters
import com.agriedge.data.local.database.dao.DiagnosisDao
import com.agriedge.data.local.database.dao.ProviderRatingDao
import com.agriedge.data.local.database.dao.SyncQueueDao
import com.agriedge.data.local.database.dao.TransactionDao
import com.agriedge.data.local.database.dao.TreatmentDao
import com.agriedge.data.local.database.dao.UserProfileDao
import com.agriedge.data.local.database.entity.DiagnosisEntity
import com.agriedge.data.local.database.entity.ProviderRatingEntity
import com.agriedge.data.local.database.entity.SyncQueueItem
import com.agriedge.data.local.database.entity.TransactionEntity
import com.agriedge.data.local.database.entity.TreatmentEntity
import com.agriedge.data.local.database.entity.UserProfileEntity
import net.sqlcipher.database.SupportFactory

/**
 * Main Room database for AgriEdge-Link application
 * Configured with SQLCipher encryption for data security
 * 
 * Requirements: 5.1, 34.3, 43.1, 43.2, 43.3
 */
@Database(
    entities = [
        DiagnosisEntity::class,
        UserProfileEntity::class,
        TreatmentEntity::class,
        TransactionEntity::class,
        ProviderRatingEntity::class,
        SyncQueueItem::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AgriEdgeDatabase : RoomDatabase() {
    
    abstract fun diagnosisDao(): DiagnosisDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun treatmentDao(): TreatmentDao
    abstract fun transactionDao(): TransactionDao
    abstract fun ratingDao(): ProviderRatingDao
    abstract fun syncQueueDao(): SyncQueueDao
    
    companion object {
        private const val DATABASE_NAME = "agriedge_database"
        
        @Volatile
        private var INSTANCE: AgriEdgeDatabase? = null
        
        /**
         * Get database instance with encryption enabled
         * Uses SQLCipher for AES-256 encryption
         * 
         * @param context Application context
         * @param passphrase Database encryption passphrase (device-specific)
         * @return Encrypted database instance
         */
        fun getInstance(context: Context, passphrase: ByteArray): AgriEdgeDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context, passphrase).also { INSTANCE = it }
            }
        }
        
        private fun buildDatabase(context: Context, passphrase: ByteArray): AgriEdgeDatabase {
            // Create encrypted database using SQLCipher
            val factory = SupportFactory(passphrase)
            
            return Room.databaseBuilder(
                context.applicationContext,
                AgriEdgeDatabase::class.java,
                DATABASE_NAME
            )
                .openHelperFactory(factory)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Database created - can add initial data here if needed
                    }
                    
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        // Enable foreign key constraints
                        db.execSQL("PRAGMA foreign_keys=ON")
                    }
                })
                .fallbackToDestructiveMigration() // For development - remove in production
                .build()
        }
        
        /**
         * Clear database instance (for testing or logout)
         */
        fun clearInstance() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
