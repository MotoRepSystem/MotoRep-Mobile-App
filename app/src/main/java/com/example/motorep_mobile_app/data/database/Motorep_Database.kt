
package com.example.motorep_mobile_app.data.database


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.motorep_mobile_app.data.dao.Cliente_Dao
import com.example.motorep_mobile_app.data.dao.Empleado_Dao
import com.example.motorep_mobile_app.data.entity.Cliente
import com.example.motorep_mobile_app.data.entity.Empleado

@Database(
    entities = [Cliente::class, Empleado::class],
    version = 5
)
abstract class Motorep_Database :
    RoomDatabase() {
    abstract fun clienteDao():
            Cliente_Dao
    abstract fun empleadoDao():
            Empleado_Dao

    companion object {
        @Volatile
        private var INSTANCE:
                Motorep_Database? = null

        // Migración de la versión 1 a la 2: agrega las columnas nuevas
        // sin borrar los datos existentes.
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE cliente ADD COLUMN sincronizado INTEGER NOT NULL DEFAULT 1" +
                            "ALTER TABLE empleado ADD COLUMN sincronizado INTEGER NOT NULL DEFAULT 1"
                )
                db.execSQL(
                    "ALTER TABLE cliente ADD COLUMN pendienteEliminar INTEGER NOT NULL DEFAULT 0" +
                            "ALTER TABLE empleado ADD COLUMN pendienteEliminar INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    "ALTER TABLE cliente ADD COLUMN syncId TEXT NOT NULL DEFAULT ''"
                )

                db.execSQL(
                    "ALTER TABLE cliente ADD COLUMN serverId INTEGER"
                )

                db.execSQL(
                    "ALTER TABLE empleado ADD COLUMN syncId TEXT NOT NULL DEFAULT ''"
                )

                db.execSQL(
                    "ALTER TABLE empleado ADD COLUMN serverId INTEGER"
                )
            }
        }

        fun getDatabase(
            context: Context
        ): Motorep_Database {
            return INSTANCE ?: synchronized(this){
                val instance =
                    Room.databaseBuilder(
                        context.applicationContext,
                        Motorep_Database::class.java,
                        "motorepdatabase"
                    )
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                        .fallbackToDestructiveMigration(true)
                        .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
