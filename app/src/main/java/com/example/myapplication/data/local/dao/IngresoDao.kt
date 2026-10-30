package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.data.local.entity.EventoIngreso
import com.example.myapplication.data.local.entity.Ingreso
import com.example.myapplication.data.local.model.IngresoResumen
import com.example.myapplication.data.local.model.EventoHistorial
import kotlinx.coroutines.flow.Flow

// Consultas y cambios de los ingresos y de su historial.
@Dao
interface IngresoDao {

    // Registra un ingreso y devuelve su identificador.
    @Insert
    suspend fun insertar(ingreso: Ingreso): Long

    // Observa los ingresos con los datos del vehículo y del cliente.
    // Busca por placa, serie, modelo o cliente. Con estadoFiltro en null muestra todos los estados,
    // y NO_DISPONIBLE incluye los valores de estado desconocidos.
    // Las fechas van en milisegundos: fechaDesde incluye el instante y fechaHastaExclusiva lo excluye;
    // null omite ese límite. Los órdenes son RECIENTES, ANTIGUOS y PLACA_ASCENDENTE,
    // y cualquier otro valor usa el orden por fecha reciente.
    // Room reemite la lista cada vez que cambian las tablas consultadas.
    @Query("""SELECT
            i.ingresoId AS ingresoId,v.placa AS placa,v.modelo AS modelo,v.color AS color,
            v.numeroSerie AS numeroSerie,c.nombre AS nombreCliente,c.telefono AS telefonoCliente,c.email AS emailCliente,
            i.fecha AS fechaEntrada,i.estado AS estado,i.motivoIngreso AS motivoIngreso,
            i.condicionInicial AS condicionInicial, i.fotoPath AS fotoPath
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
    // La eliminación arrastra el historial, el checklist, el diagnóstico y la firma; las evidencias la impiden (RESTRICT).
    @Query("DELETE FROM ingreso WHERE ingresoId = :id")
        suspend fun eliminar(id: Long): Int

    // Historial del ingreso.
    // Guarda un evento y devuelve su identificador.
    @Insert
    suspend fun insertarEvento(evento: EventoIngreso): Long

    // Último cambio de estado hacia un estado dado, por ejemplo la entrega.
    @Query("""SELECT * FROM evento_ingreso WHERE ingresoId = :id AND accion = 'ESTADO'
        AND estadoNuevo = :estado ORDER BY fecha DESC, eventoId DESC LIMIT 1""")
    suspend fun ultimoCambioHacia(id: Long, estado: String): EventoIngreso?

    // Cuenta los ingresos de un vehículo que todavía no se entregan ni se cancelan.
    @Query("SELECT COUNT(*) FROM ingreso WHERE vehiculoId = :vehiculoId AND estado NOT IN ('ENTREGADO', 'CANCELADO')")
    suspend fun contarAbiertosDelVehiculo(vehiculoId: Long): Int

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
            i.estado AS estado, i.motivoIngreso AS motivoIngreso, i.condicionInicial AS condicionInicial, i.fotoPath AS fotoPath
        FROM ingreso AS i
        INNER JOIN vehiculo AS v ON i.vehiculoId = v.vehiculoId
        INNER JOIN cliente AS c ON v.clienteId = c.clienteId
        WHERE i.ingresoId = :id""")
    fun observarIngresoPorId(id: Long): Flow<IngresoResumen?>

    // Bitácora general: cada fila representa una acción, no un ingreso.
    // LEFT JOIN conserva eventos cuyo técnico ya no está en el catálogo.
    @Query("""
        SELECT e.eventoId, e.ingresoId, e.fecha, e.tecnicoId,
            t.nombre AS nombreTecnico, v.placa, v.modelo,
            c.nombre AS nombreCliente, e.accion, e.estadoAnterior,
            e.estadoNuevo, e.motivo
        FROM evento_ingreso AS e
        INNER JOIN ingreso AS i ON i.ingresoId = e.ingresoId
        INNER JOIN vehiculo AS v ON v.vehiculoId = i.vehiculoId
        INNER JOIN cliente AS c ON c.clienteId = v.clienteId
        LEFT JOIN tecnico AS t ON t.tecnicoId = e.tecnicoId
        WHERE (:busqueda = ''
            OR INSTR(LOWER(v.placa), LOWER(:busqueda)) > 0
            OR INSTR(LOWER(v.modelo), LOWER(:busqueda)) > 0
            OR INSTR(LOWER(c.nombre), LOWER(:busqueda)) > 0
            OR INSTR(LOWER(COALESCE(t.nombre, '')), LOWER(:busqueda)) > 0
            OR INSTR(LOWER(e.motivo), LOWER(:busqueda)) > 0
            OR CAST(e.ingresoId AS TEXT) = :busqueda)
        AND (:accion IS NULL OR e.accion = :accion
            OR (:accion = 'FOTOS' AND e.accion IN ('EVIDENCIA', 'FOTO_PRINCIPAL', 'EVIDENCIA_ELIMINADA')))
        AND (:desde IS NULL OR e.fecha >= :desde)
        AND (:hasta IS NULL OR e.fecha < :hasta)
        ORDER BY e.fecha DESC, e.eventoId DESC
        LIMIT :limite
    """)
    fun observarHistorial(
        busqueda: String,
        accion: String?,
        desde: Long?,
        hasta: Long?,
        limite: Int
    ): Flow<List<EventoHistorial>> }
