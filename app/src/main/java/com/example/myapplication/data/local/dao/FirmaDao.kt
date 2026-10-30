package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.data.local.entity.Firma
import kotlinx.coroutines.flow.Flow

// Consultas de la firma de cada ingreso.
@Dao
interface FirmaDao {

    // Devuelve la firma vigente o null si todavía no se firma.
    @Query("SELECT * FROM firma WHERE ingresoId = :ingresoId LIMIT 1")
    suspend fun obtenerPorIngreso(ingresoId: Long): Firma?

    // Observa la firma para reflejar el guardado en la pantalla.
    @Query("SELECT * FROM firma WHERE ingresoId = :ingresoId LIMIT 1")
    fun observarPorIngreso(ingresoId: Long): Flow<Firma?>

    // Reemplaza la firma anterior del mismo ingreso, si existe.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(firma: Firma): Long }
