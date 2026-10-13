package com.example.motorep_mobile_app.data.database
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.motorep_mobile_app.data.dao.ClienteDao
import com.example.motorep_mobile_app.data.entity.Cliente
@Database(
    entities = [Cliente::class], version = 1
)
abstract class MotorepDatabase : RoomDatabase() {
    abstract fun clienteDao(): ClienteDao

    companion object {
        @Volatile
        private var INSTANCE: MotorepDatabase? = null

        fun getDatabase(context: Context): MotorepDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MotorepDatabase::class.java,
                    "motorepdatabase"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
