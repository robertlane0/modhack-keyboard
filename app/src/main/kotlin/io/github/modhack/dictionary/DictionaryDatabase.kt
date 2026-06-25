package io.github.modhack.dictionary

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room database for learned words and bigrams.
 */
@Database(entities = [Unigram::class, Bigram::class], version = 1, exportSchema = false)
abstract class DictionaryDatabase : RoomDatabase() {
    abstract fun userDictionaryDao(): UserDictionaryDao
    abstract fun autoDictionaryDao(): AutoDictionaryDao

    companion object {
        @Volatile
        private var INSTANCE: DictionaryDatabase? = null

        fun getInstance(context: Context): DictionaryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DictionaryDatabase::class.java,
                    "dictionary_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
