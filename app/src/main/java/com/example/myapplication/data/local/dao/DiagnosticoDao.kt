package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.data.local.entity.ConceptoCotizacion
import com.example.myapplication.data.local.entity.Diagnostico

@Dao
interface DiagnosticoDao {

    // Obtiene el diagnóstico asociado al ingreso.
    // Devuelve null si todavía no se ha guardado uno.
    @Query(
        """
        SELECT *
        FROM diagnostico
        WHERE ingresoId = :ingresoId
        LIMIT 1
        """
    )
    suspend fun obtenerPorIngreso(
        ingresoId: Long
    ): Diagnostico?

    // Guarda el primer diagnóstico y devuelve su identificador.
    @Insert
    suspend fun insertar(
        diagnostico: Diagnostico
    ): Long

    // Actualiza el diagnóstico conservando su identificador.
    // Devuelve la cantidad de filas actualizadas.
    @Update
    suspend fun actualizar(
        diagnostico: Diagnostico
    ): Int

    // Recupera los conceptos en el orden definido por el mecánico.
    @Query(
        """
        SELECT *
        FROM concepto_cotizacion
        WHERE diagnosticoId = :diagnosticoId
        ORDER BY orden ASC, conceptoId ASC
        """
    )
    suspend fun obtenerConceptos(
        diagnosticoId: Long
    ): List<ConceptoCotizacion>

    // Guarda las filas de la cotización.
    // Cada concepto debe contener el identificador del diagnóstico.
    @Insert
    suspend fun insertarConceptos(
        conceptos: List<ConceptoCotizacion>
    ): List<Long>

    // Retira las filas anteriores para guardar la cotización editada.
    // No elimina el diagnóstico.
    // El repositorio ejecutará el reemplazo dentro de una transacción,
    // para conservar los datos anteriores si el guardado falla.
    @Query(
        """
        DELETE FROM concepto_cotizacion
        WHERE diagnosticoId = :diagnosticoId
        """
    )
    suspend fun eliminarConceptos(
        diagnosticoId: Long
    ): Int
}