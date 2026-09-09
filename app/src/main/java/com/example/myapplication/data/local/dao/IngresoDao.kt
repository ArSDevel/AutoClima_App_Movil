package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.myapplication.data.local.entity.Ingreso
import com.example.myapplication.data.local.model.IngresoResumen
import kotlinx.coroutines.flow.Flow

@Dao
interface IngresoDao {
    @Insert
    suspend fun insertar(ingreso: Ingreso): Long

    @Query(
        """
        SELECT
            i.ingresoId AS ingresoId, v.placa AS placa, v.modelo AS modelo, v.color AS color, v.numeroSerie AS numeroSerie,
            c.nombre AS nombreCliente, c.telefono AS telefonoCliente, c.email AS emailCliente,i.fecha AS fechaEntrada
        FROM ingreso AS i
        INNER JOIN vehiculo AS v ON i.vehiculoId = v.vehiculoId
        INNER JOIN cliente AS c ON v.clienteId = c.clienteId
        WHERE
            :textoBusqueda = ''
            OR INSTR(LOWER(v.placa),LOWER(:textoBusqueda)) > 0
            OR INSTR(LOWER(COALESCE(v.numeroSerie, '')),LOWER(:textoBusqueda)) > 0
        ORDER BY i.fecha DESC, i.ingresoId DESC
        """
    )
    fun observarIngresos(textoBusqueda: String): Flow<List<IngresoResumen>>
}