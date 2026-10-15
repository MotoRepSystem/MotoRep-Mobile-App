package com.example.motorep_mobile_app.data.database
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.motorep_mobile_app.data.dao.ClienteDao
import com.example.motorep_mobile_app.data.entity.Cliente
@Database(
    entities = [Cliente::class], version = 1
)
abstract class MotorepDatabase : RoomDatabase() {
    abstract fun clienteDao(): ClienteDao

    companion object {
        @Volatile
        private var INSTANCE:
                MotorepDatabase? = null

        // Migración de la versión 1 a la 2: agrega las columnas nuevas
        // sin borrar los datos existentes.
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE clientes ADD COLUMN sincronizado INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE clientes ADD COLUMN pendienteEliminar INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE clientes ADD COLUMN syncId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE clientes ADD COLUMN serverId INTEGER")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DELETE FROM clientes")
            }
        }

        fun getDatabase(
            context: Context
        ): MotorepDatabase {
            return INSTANCE ?: synchronized(this){
                val instance =
                    Room.databaseBuilder(
                        context.applicationContext,
                        MotorepDatabase::class.java,
                        "motorepdatabase"
                    )
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                        .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
