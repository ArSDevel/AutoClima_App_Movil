package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.myapplication.data.local.entity.Evidencia
import kotlinx.coroutines.flow.Flow

@Dao
interface EvidenciaDao {

    // Guarda una evidencia y devuelve su identificador.
    @Insert
    suspend fun insertarEvidencia(evidencia: Evidencia): Long

    // Conservamos esta consulta para la galería actual
    // mientras completamos su integración por ingreso.
    @Query(
        """
        SELECT *
        FROM tabla_evidencias
        ORDER BY fecha DESC, id DESC
        """
    )
    fun obtenerTodas(): Flow<List<Evidencia>>

    // Observa las fotografías del ingreso seleccionado.
    @Query(
        """
        SELECT *
        FROM tabla_evidencias
        WHERE ingresoId = :ingresoId
        ORDER BY fecha DESC, id DESC
        """
    )
    fun observarPorIngreso(
        ingresoId: Long
    ): Flow<List<Evidencia>>

    // Fotografías antiguas sin ingreso asociado.
    @Query(
        """
        SELECT *
        FROM tabla_evidencias
        WHERE ingresoId IS NULL
        ORDER BY id DESC
        """
    )
    fun observarSinAsignar(): Flow<List<Evidencia>>

    // Obtiene una evidencia por su identificador.
    @Query(
        """
        SELECT *
        FROM tabla_evidencias
        WHERE id = :evidenciaId
        """
    )
    suspend fun obtener(evidenciaId: Int): Evidencia?

    // Permite reconocer un guardado ya realizado.
    @Query(
        """
        SELECT *
        FROM tabla_evidencias
        WHERE rutaImagen = :ruta
        ORDER BY id ASC
        LIMIT 1
        """
    )
    suspend fun buscarPorRuta(ruta: String): Evidencia?

    // Comprueba si el ingreso tiene fotografías asociadas.
    @Query(
        """
        SELECT COUNT(*)
        FROM tabla_evidencias
        WHERE ingresoId = :ingresoId
        """
    )
    suspend fun contarPorIngreso(ingresoId: Long): Int

    @Query("SELECT * FROM tabla_evidencias WHERE ingresoId = :ingresoId ORDER BY fecha DESC, id DESC")
    suspend fun obtenerPorIngreso(ingresoId: Long): List<Evidencia>

    @Query("DELETE FROM tabla_evidencias WHERE id = :id AND ingresoId = :ingresoId")
    suspend fun eliminar(id: Int, ingresoId: Long): Int

    @Query("""SELECT
        (SELECT COUNT(*) FROM tabla_evidencias WHERE rutaImagen = :ruta) +
        (SELECT COUNT(*) FROM ingreso WHERE fotoPath = :ruta)
    """)
    suspend fun contarReferenciasRuta(ruta: String): Int
}