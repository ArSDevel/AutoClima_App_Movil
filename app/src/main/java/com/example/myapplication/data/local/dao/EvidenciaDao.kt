package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.myapplication.data.local.entity.Evidencia
import kotlinx.coroutines.flow.Flow

// Consultas y cambios de las fotografías de los ingresos.
@Dao
interface EvidenciaDao {

    // Guarda una evidencia y devuelve su identificador.
    @Insert
    suspend fun insertarEvidencia(evidencia: Evidencia): Long

    // Observa las fotografías del ingreso seleccionado.
    @Query(
        """
        SELECT *
        FROM tabla_evidencias
        WHERE ingresoId = :ingresoId
        ORDER BY fecha DESC, id DESC
        """)
    fun observarPorIngreso(
        ingresoId: Long
    ): Flow<List<Evidencia>>

    // Obtiene una evidencia por su identificador.
    @Query(
        """
        SELECT *
        FROM tabla_evidencias
        WHERE id = :evidenciaId
        """)
    suspend fun obtener(evidenciaId: Int): Evidencia?

    // Permite reconocer un guardado ya realizado.
    @Query(
        """
        SELECT *
        FROM tabla_evidencias
        WHERE rutaImagen = :ruta
        ORDER BY id ASC
        LIMIT 1
        """)
    suspend fun buscarPorRuta(ruta: String): Evidencia?

    // Cuenta las fotografías del ingreso; sirve para impedir que se elimine la última.
    @Query(
        """
        SELECT COUNT(*)
        FROM tabla_evidencias
        WHERE ingresoId = :ingresoId
        """)
    suspend fun contarPorIngreso(ingresoId: Long): Int

    // Lista las fotografías de un ingreso.
    @Query("SELECT * FROM tabla_evidencias WHERE ingresoId = :ingresoId ORDER BY fecha DESC, id DESC")
    suspend fun obtenerPorIngreso(ingresoId: Long): List<Evidencia>

    // Elimina una fotografía de un ingreso y devuelve las filas afectadas.
    @Query("DELETE FROM tabla_evidencias WHERE id = :id AND ingresoId = :ingresoId")
    suspend fun eliminar(id: Int, ingresoId: Long): Int

    // Cuenta cuántos registros usan una misma ruta de archivo.
    @Query("""SELECT
        (SELECT COUNT(*) FROM tabla_evidencias WHERE rutaImagen = :ruta) +
        (SELECT COUNT(*) FROM ingreso WHERE fotoPath = :ruta)
    """)
    suspend fun contarReferenciasRuta(ruta: String): Int }