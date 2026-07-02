package com.example.nlcpapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.nlcpapp.data.local.dao.ObservationDao
import com.example.nlcpapp.data.local.entity.ObservationEntity

@Database(entities = [ObservationEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun observationDao(): ObservationDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nlc_app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}