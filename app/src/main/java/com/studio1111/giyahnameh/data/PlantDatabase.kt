package com.studio1111.giyahnameh.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Plant::class], version = 1, exportSchema = false)
abstract class PlantDatabase : RoomDatabase() {
    abstract fun plantDao(): PlantDao
    companion object {
        @Volatile private var instance: PlantDatabase? = null
        fun get(context: Context): PlantDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, PlantDatabase::class.java, "giyahnameh.db")
                .build().also { instance = it }
        }
    }
}
