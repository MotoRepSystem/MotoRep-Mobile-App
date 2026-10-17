
package com.example.motorep_mobile_app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.motorep_mobile_app.data.dao.Cliente_Dao
import com.example.motorep_mobile_app.data.dao.EmpleadoDao
import com.example.motorep_mobile_app.data.entity.Cliente
import com.example.motorep_mobile_app.data.entity.Empleado

@Database(
    entities = [Cliente::class, Empleado::class],
    version = 3
)
abstract class MotorepDatabase : RoomDatabase() {

    abstract fun clienteDao(): Cliente_Dao

    abstract fun empleado_Dao(): EmpleadoDao

    companion object {

        @Volatile
        private var INSTANCE:
                MotorepDatabase? = null

        fun getDatabase(context: Context): MotorepDatabase {
            return INSTANCE ?: synchronized(this) {

                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MotorepDatabase::class.java,
                    "motorepdatabase"
                )
                    .fallbackToDestructiveMigration()
                    .build().also { database ->
                        INSTANCE = database
                    }
            }
        }
    }
}
