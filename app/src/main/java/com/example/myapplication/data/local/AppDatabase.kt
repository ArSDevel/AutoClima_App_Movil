package com.example.myapplication.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.data.local.dao.ClienteDao
import com.example.myapplication.data.local.dao.IngresoDao
import com.example.myapplication.data.local.dao.TecnicoDao
import com.example.myapplication.data.local.dao.VehiculoDao
import com.example.myapplication.data.local.entity.Cliente
import com.example.myapplication.data.local.entity.Ingreso
import com.example.myapplication.data.local.entity.Tecnico
import com.example.myapplication.data.local.entity.Vehiculo
import com.example.myapplication.util.PasswordHasher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log
import kotlinx.coroutines.CancellationException

@Database(
    entities = [Tecnico::class, Cliente::class, Vehiculo::class, Ingreso::class],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun tecnicoDao(): TecnicoDao
    abstract fun clienteDao(): ClienteDao
    abstract fun vehiculoDao(): VehiculoDao
    abstract fun ingresoDao(): IngresoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "autoclima_diag.db"
                )
                    .addMigrations(MIGRATION_2_3)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)

                            Log.d("CargaInicial", "Se ejecutó onCreate")

                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val dao = getInstance(context).tecnicoDao()

                                    val id = dao.insertar(
                                        Tecnico(
                                            nombre = "Erik",
                                            usuario = "erik",
                                            passwordHash = PasswordHasher.hash("1234")
                                        )
                                    )

                                    Log.d("CargaInicial", "Técnico insertado con ID: $id")
                                } catch (cancelacion: CancellationException) {
                                    throw cancelacion
                                } catch (error: Exception) {
                                    Log.e("CargaInicial", "Falló la inserción del técnico", error)
                                }
                            }
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}