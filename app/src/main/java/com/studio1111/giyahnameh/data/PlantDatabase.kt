package com.studio1111.giyahnameh.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Plant::class], version = 2, exportSchema = false)
abstract class PlantDatabase : RoomDatabase() {
    abstract fun plantDao(): PlantDao
    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE plants ADD COLUMN imageAttribution TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE plants ADD COLUMN imageLicense TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE plants ADD COLUMN imageSourceUrl TEXT NOT NULL DEFAULT ''")
            }
        }
        @Volatile private var instance: PlantDatabase? = null
        fun get(context: Context): PlantDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, PlantDatabase::class.java, "giyahnameh.db")
                .build().also { instance = it }
        }
    }
}
