package com.example.rinconsur.repository.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.rinconsur.repository.model.Registro
import com.example.rinconsur.repository.model.TipoUsuario
import com.example.rinconsur.repository.model.Turno
import com.example.rinconsur.repository.model.Usuario

@Database(
    entities = [TipoUsuario::class, Usuario::class, Turno::class, Registro::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun tipoUsuarioDao(): TipoUsuarioDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun turnoDao(): TurnoDao
    abstract fun registroDao(): RegistroDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rinconsur.db"
                )
                    // Proyecto académico: al cambiar las tablas se recrea la BD (borra los datos)
                    .fallbackToDestructiveMigration()
                    .addCallback(seedCallback)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Datos iniciales ficticios. Se ejecuta en cada apertura y no duplica nada,
        // así también se cargan después de recrear la base de datos.
        private val seedCallback = object : Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                db.execSQL(
                    "INSERT OR IGNORE INTO tipos_usuario (id, nombre) VALUES " +
                            "(1, 'Operador'), (2, 'Supervisor')"
                )
                sembrarUsuario(db, "Operador Demo", "operador@duoc.cl", "operador123", 1)
                sembrarUsuario(db, "Supervisor Demo", "supervisor@duoc.cl", "supervisor123", 2)
            }
        }

        // La clave se guarda con hash, por eso se inserta desde Kotlin y no con SQL fijo
        private fun sembrarUsuario(
            db: SupportSQLiteDatabase,
            nombre: String,
            email: String,
            clave: String,
            tipoUsuarioId: Long
        ) {
            val existe = db.query(
                "SELECT COUNT(*) FROM usuarios WHERE email = ?",
                arrayOf<Any?>(email)
            ).use { it.moveToFirst() && it.getInt(0) > 0 }
            if (existe) return

            val sal = Seguridad.nuevaSal()
            db.execSQL(
                "INSERT INTO usuarios (nombre, email, claveHash, sal, tipoUsuarioId, activo) " +
                        "VALUES (?, ?, ?, ?, ?, 1)",
                arrayOf<Any?>(nombre, email, Seguridad.hash(clave, sal), sal, tipoUsuarioId)
            )
        }
    }
}