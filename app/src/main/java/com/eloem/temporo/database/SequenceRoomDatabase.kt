package com.eloem.temporo.database


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [

], version = 2, exportSchema = false)
abstract class SequenceRoomDatabase : RoomDatabase() {
    abstract fun sequenceDao(): SequenceDao

    companion object {
        @Volatile
        private var INSTANCE: SequenceRoomDatabase? = null

        fun getDatabase(context: Context): SequenceRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SequenceRoomDatabase::class.java,
                    "sequence_database")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}