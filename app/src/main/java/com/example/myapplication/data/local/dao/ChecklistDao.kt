package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.data.local.entity.ChecklistDiagnostico

// Consultas del checklist de revisión de cada ingreso.
@Dao
interface ChecklistDao {
    // Crea el checklist y devuelve su identificador; el índice único impide un segundo checklist por ingreso.
    @Insert
    suspend fun insertar(checklist: ChecklistDiagnostico): Long
    // Devuelve el checklist del ingreso o null si todavía no se guarda.
    @Query(
        """
        SELECT *
        FROM checklist_diagnostico
        WHERE ingresoId = :ingresoId
        LIMIT 1
        """)
    suspend fun obtenerPorIngreso(ingresoId: Long): ChecklistDiagnostico?

    // Devuelve la cantidad de filas actualizadas.
    @Update
    suspend fun actualizar(checklist: ChecklistDiagnostico): Int }