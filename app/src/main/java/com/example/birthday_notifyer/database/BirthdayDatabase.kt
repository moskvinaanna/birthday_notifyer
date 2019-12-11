package com.example.birthday_notifyer.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PersonBirthday::class], version = 1, exportSchema = false)
abstract class BirthdayDatabase : RoomDatabase() {
    abstract val birthdayDatabaseDao: BirthdayDatabaseDao
    companion object {
        @Volatile
        private var INSTANCE: BirthdayDatabase? = null
        fun getInstance(context: Context): BirthdayDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        BirthdayDatabase::class.java,
                        "birthday_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}