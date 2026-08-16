package com.example

import androidx.room.*
import android.content.Context
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey val udcin: String,
    val displayName: String,
    val profilePictureUrl: String?
)

@Entity(tableName = "call_history")
data class CallHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remoteUdcin: String,
    val timestamp: Long,
    val type: String // "INCOMING", "OUTGOING", "MISSED"
)

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts")
    fun getAllContacts(): Flow<List<Contact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: Contact)

    @Query("SELECT * FROM contacts WHERE udcin = :udcin")
    suspend fun getContact(udcin: String): Contact?
}

@Dao
interface CallHistoryDao {
    @Query("SELECT * FROM call_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<CallHistory>>

    @Insert
    suspend fun insertCall(callHistory: CallHistory)
}

@Database(entities = [Contact::class, CallHistory::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun callHistoryDao(): CallHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "voip_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
