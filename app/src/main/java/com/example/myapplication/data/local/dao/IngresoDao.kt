package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.data.local.entity.EventoIngreso
import com.example.myapplication.data.local.entity.Ingreso
import com.example.myapplication.data.local.model.IngresoResumen
import kotlinx.coroutines.flow.Flow

@Dao
interface IngresoDao {

    // Registra un ingreso y devuelve su identificador.
    @Insert
    suspend fun insertar(ingreso: Ingreso): Long

    /*
     * Observa los ingresos con los datos del vehículo y del cliente.
     * Permite buscar por placa, serie, modelo o cliente.
     *
     * estadoFiltro = null permite mostrar todos los estados.
     * NO_DISPONIBLE incluye valores de estado desconocidos.
     *
     * Las fechas se reciben en milisegundos:
     * - fechaDesde incluye el instante indicado.
     * - fechaHastaExclusiva excluye el instante indicado.
     * - null omite ese límite.
     *
     * Órdenes admitidos:
     * RECIENTES, ANTIGUOS y PLACA_ASCENDENTE.
     * Cualquier otro valor utiliza el orden por fecha reciente.
     */
    @Query("""SELECT
            i.ingresoId AS ingresoId,v.placa AS placa,v.modelo AS modelo,v.color AS color,
            v.numeroSerie AS numeroSerie,c.nombre AS nombreCliente,c.telefono AS telefonoCliente,c.email AS emailCliente,
            i.fecha AS fechaEntrada,i.estado AS estado,i.motivoIngreso AS motivoIngreso,
            i.condicionInicial AS condicionInicial
        FROM ingreso AS i
        INNER JOIN vehiculo AS v ON i.vehiculoId = v.vehiculoId
        INNER JOIN cliente AS c ON v.clienteId = c.clienteId
        WHERE(:textoBusqueda = '' OR INSTR(LOWER(v.placa),LOWER(:textoBusqueda)) > 0
                OR INSTR(LOWER(COALESCE(v.numeroSerie, '')),LOWER(:textoBusqueda)) > 0
                OR INSTR(LOWER(v.modelo),LOWER(:textoBusqueda)) > 0
                OR INSTR(LOWER(c.nombre),LOWER(:textoBusqueda)) > 0)
            AND (:estadoFiltro IS NULL OR i.estado = :estadoFiltro
                OR (:estadoFiltro = 'NO_DISPONIBLE'AND (
                        i.estado IS NULL OR i.estado NOT IN (
                            'REGISTRADO', 'EN_REVISION','EN_REPARACION', 'LISTO_PARA_FIRMA', 'LISTO_PARA_ENTREGA',
                            'ENTREGADO','CANCELADO'))))
            AND (:fechaDesde IS NULL OR i.fecha >= :fechaDesde)
            AND (:fechaHastaExclusiva IS NULL OR i.fecha < :fechaHastaExclusiva)
        ORDER BY CASE
                WHEN :orden = 'PLACA_ASCENDENTE' THEN v.placa END COLLATE NOCASE ASC,
            CASE WHEN :orden = 'ANTIGUOS' THEN i.fecha END ASC,
            CASE WHEN :orden = 'ANTIGUOS' THEN i.ingresoId END ASC,
            i.fecha DESC,i.ingresoId DESC""")
    fun observarIngresos(
        textoBusqueda: String = "",
        estadoFiltro: String? = null,
        fechaDesde: Long? = null,
        fechaHastaExclusiva: Long? = null,
        orden: String = "RECIENTES"
    ): Flow<List<IngresoResumen>>

    // Obtiene el ingreso actual o null si ya no existe.
    @Query("SELECT * FROM ingreso WHERE ingresoId = :id")
    suspend fun obtener(id: Long): Ingreso?

    // Devuelve la cantidad de filas actualizadas.
    @Update
    suspend fun actualizar(ingreso: Ingreso): Int

    // El repositorio debe validar primero si se permite eliminar.
    @Query("DELETE FROM ingreso WHERE ingresoId = :id")
    suspend fun eliminar(id: Long): Int

    // Historial del ingreso.
    // Guarda un evento y devuelve su identificador.
    @Insert
    suspend fun insertarEvento(evento: EventoIngreso): Long

    // Observa el historial, mostrando primero lo más reciente.
    @Query("SELECT * FROM evento_ingreso WHERE ingresoId = :id ORDER BY fecha DESC, eventoId DESC")
    fun observarEventos(id: Long): Flow<List<EventoIngreso>>

    /* Cuenta eventos distintos al registro inicial.
    Una edición o un cambio de estado cuentan como actividad.
    El evento CREADO no impide eliminar una captura. */
    @Query("SELECT COUNT(*) FROM evento_ingreso WHERE ingresoId = :id AND accion != 'CREADO'")
    suspend fun contarActividad(id: Long): Int

    // Observa el expediente de un único ingreso.
    // Se utiliza en detalle y para preparar la edición.
    @Query("""SELECT
            i.ingresoId AS ingresoId,v.placa AS placa, v.modelo AS modelo,
            v.color AS color, v.numeroSerie AS numeroSerie, c.nombre AS nombreCliente,
            c.telefono AS telefonoCliente, c.email AS emailCliente, i.fecha AS fechaEntrada,
            i.estado AS estado, i.motivoIngreso AS motivoIngreso, i.condicionInicial AS condicionInicial
        FROM ingreso AS i
        INNER JOIN vehiculo AS v ON i.vehiculoId = v.vehiculoId
        INNER JOIN cliente AS c ON v.clienteId = c.clienteId
        WHERE i.ingresoId = :id""")
    fun observarIngresoPorId(id: Long): Flow<IngresoResumen?>
}