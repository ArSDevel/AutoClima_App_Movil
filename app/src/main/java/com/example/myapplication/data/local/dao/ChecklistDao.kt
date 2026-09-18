package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.data.local.entity.ChecklistDiagnostico

@Dao
interface ChecklistDao {
    @Insert
    suspend fun insertar(checklist: ChecklistDiagnostico): Long
    @Query(
        """
        SELECT *
        FROM checklist_diagnostico
        WHERE ingresoId = :ingresoId
        LIMIT 1
        """)
    suspend fun obtenerPorIngreso(ingresoId: Long): ChecklistDiagnostico?

    @Update
    suspend fun actualizar(checklist: ChecklistDiagnostico): Int
}