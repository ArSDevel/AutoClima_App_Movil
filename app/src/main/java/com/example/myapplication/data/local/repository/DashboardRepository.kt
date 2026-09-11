package com.example.myapplication.data.local.repository

import androidx.room.withTransaction
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.entity.EventoIngreso
import com.example.myapplication.data.local.entity.Ingreso
import com.example.myapplication.data.local.model.EstadoIngreso
import com.example.myapplication.data.local.model.FormularioIngreso
import com.example.myapplication.data.local.model.IngresoResumen
import com.example.myapplication.data.local.model.ReglasIngreso
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.Locale

class DashboardRepository(
    private val baseDeDatos: AppDatabase
) {
    private val ingresoDao = baseDeDatos.ingresoDao()
    private val tecnicoDao = baseDeDatos.tecnicoDao()
    private val clienteDao = baseDeDatos.clienteDao()
    private val vehiculoDao = baseDeDatos.vehiculoDao()

    // Observa el listado con búsqueda, estado, fechas y ordenamiento.
    fun observarIngresos(
        textoBusqueda: String = "",
        estadoFiltro: EstadoIngreso? = null,
        fechaDesde: Long? = null,
        fechaHastaExclusiva: Long? = null,
        orden: String = "RECIENTES"
    ): Flow<List<IngresoResumen>> {
        if (fechaDesde != null && fechaHastaExclusiva != null) {
            require(fechaDesde < fechaHastaExclusiva) {
                "El inicio del periodo debe ser anterior a su límite final."
            }
        }

        require(orden == "RECIENTES" || orden == "ANTIGUOS" || orden == "PLACA_ASCENDENTE") {
            "El orden seleccionado no es válido."
        }

        return ingresoDao.observarIngresos(
            textoBusqueda = textoBusqueda.trim(), estadoFiltro = estadoFiltro?.name,
            fechaDesde = fechaDesde, fechaHastaExclusiva = fechaHastaExclusiva,
            orden = orden
        )
    }

    // Observa el expediente de un ingreso específico.
    fun observarIngresoPorId(ingresoId: Long): Flow<IngresoResumen?> {
        require(ingresoId > 0) {
            "El identificador del ingreso no es válido."
        }
        return ingresoDao.observarIngresoPorId(ingresoId)
    }

    // Observa el historial de un ingreso.
    fun observarEventos(ingresoId: Long): Flow<List<EventoIngreso>> {
        require(ingresoId > 0) {
            "El identificador del ingreso no es válido."
        }
        return ingresoDao.observarEventos(ingresoId)
    }

    // Edita las fichas relacionadas y los datos del ingreso.
    // El estado actual determina si la edición es completa
    // o si únicamente se permite modificar teléfono y correo.
    suspend fun editar(ingresoId: Long, formulario: FormularioIngreso, tecnicoId: Long) {
        baseDeDatos.withTransaction {
            validarTecnico(tecnicoId)
            val ingreso = obtenerIngresoExistente(ingresoId)
            val estadoActual = EstadoIngreso.desdeValor(ingreso.estado)

            require(ReglasIngreso.editar(estadoActual)) {
                "Este ingreso está cerrado y no permite modificaciones." }

            val edicionCompleta = ReglasIngreso.edicionCompleta(estadoActual)
            val errores = formulario.errores(soloContacto = !edicionCompleta)
            require(errores.isEmpty()) { errores.values.firstOrNull() ?: "Revisa los campos del formulario." }

            val vehiculo = requireNotNull(vehiculoDao.obtener(ingreso.vehiculoId)) {
                "No se encontró el vehículo del ingreso."
            }

            val cliente = requireNotNull(clienteDao.obtener(vehiculo.clienteId)) {
                "No se encontró el cliente del vehículo." }

            val telefono = formulario.telefono.trim()
            val email = formulario.email.trim().ifEmpty { null }

            // Comprobamos el teléfono cuando se está modificando.
            if (telefono != cliente.telefono) {
                val otroCliente = clienteDao.buscarPorTelefono(telefono)
                require(otroCliente == null || otroCliente.clienteId == cliente.clienteId) {
                    "El teléfono ya está asociado a otro cliente."
                }
            }

            val clienteActualizado = cliente.copy(
                nombre = if (edicionCompleta) { formulario.nombre.trim()
                } else { cliente.nombre },
                telefono = telefono, email = email
            )

            // Conservamos los campos que no modifica el formulario.
            var vehiculoActualizado = vehiculo
            var ingresoActualizado = ingreso

            if (edicionCompleta) {
                val placa = formulario.placa.trim().uppercase(Locale.ROOT)
                val otroVehiculo = vehiculoDao.buscarPorPlaca(placa)
                require(otroVehiculo == null || otroVehiculo.vehiculoId == vehiculo.vehiculoId) {
                    "La placa ya está asociada a otro vehículo."
                }

                vehiculoActualizado = vehiculo.copy(
                    placa = placa, modelo = formulario.modelo.trim(),
                    color = formulario.color.trim().ifEmpty { null },
                    numeroSerie = formulario.numeroSerie.trim().ifEmpty { null })

                ingresoActualizado = ingreso.copy(
                    fecha = actualizarDiaConservandoHora(
                        fechaOriginal = ingreso.fecha,
                        nuevaFecha = formulario.fecha),
                    motivoIngreso = formulario.motivo.trim().ifEmpty { null },
                    condicionInicial = formulario.condicion.trim().ifEmpty { null }
                )
            }

            val cambioCliente = clienteActualizado != cliente
            val cambioVehiculo = vehiculoActualizado != vehiculo
            val cambioIngreso = ingresoActualizado != ingreso

            // Guardar sin cambios no genera un evento de edición.
            if (!cambioCliente && !cambioVehiculo && !cambioIngreso) {
                return@withTransaction
            }

            if (cambioCliente) {
                check(clienteDao.actualizar(clienteActualizado) == 1) {
                    "No fue posible actualizar el cliente."
                }
            }

            if (cambioVehiculo) {
                check(vehiculoDao.actualizar(vehiculoActualizado) == 1) {
                    "No fue posible actualizar el vehículo."
                }
            }

            if (cambioIngreso) {
                check(ingresoDao.actualizar(ingresoActualizado) == 1) {
                    "No fue posible actualizar el ingreso."
                }
            }

            ingresoDao.insertarEvento(
                EventoIngreso(
                    ingresoId = ingreso.ingresoId, fecha = System.currentTimeMillis(),
                    tecnicoId = tecnicoId, accion = "EDITADO",
                    estadoAnterior = ingreso.estado, estadoNuevo = ingreso.estado,
                    motivo = if (edicionCompleta) {
                        "Se actualizaron los datos del ingreso o sus fichas de cliente y vehículo."
                    } else {
                        "Se actualizaron los datos de contacto."
                    }
                )
            )
        }
    }

    // Cambia el estado y registra el evento en una transacción.
    suspend fun cambiarEstado(ingresoId: Long, destino: EstadoIngreso, motivo: String, tecnicoId: Long) {
        baseDeDatos.withTransaction {
            validarTecnico(tecnicoId)
            val ingreso = obtenerIngresoExistente(ingresoId)
            val estadoActual = EstadoIngreso.desdeValor(ingreso.estado)
            val motivoLimpio = motivo.trim()

            ReglasIngreso.validarTransicion(origen = estadoActual, destino = destino, motivo = motivoLimpio)
            val filasActualizadas = ingresoDao.actualizar(ingreso.copy(estado = destino.name))
            check(filasActualizadas == 1) { "No fue posible actualizar el ingreso." }

            ingresoDao.insertarEvento(
                EventoIngreso(ingresoId = ingreso.ingresoId,
                    fecha = System.currentTimeMillis(), tecnicoId = tecnicoId,
                    accion = "ESTADO", estadoAnterior = ingreso.estado,
                    estadoNuevo = destino.name, motivo = motivoLimpio
                )
            )
        }
    }

    // Cancela conservando el expediente y el motivo.
    suspend fun cancelar(ingresoId: Long, motivo: String, tecnicoId: Long
    ) { cambiarEstado(ingresoId = ingresoId, destino = EstadoIngreso.CANCELADO,
            motivo = motivo, tecnicoId = tecnicoId)
    }

    // Elimina únicamente una captura registrada sin actividad posterior.
    suspend fun eliminar(ingresoId: Long, tecnicoId: Long
    ) {
        baseDeDatos.withTransaction {
            validarTecnico(tecnicoId)

            val ingreso = obtenerIngresoExistente(ingresoId)
            val estadoActual = EstadoIngreso.desdeValor(ingreso.estado)
            val tieneActividadPosterior = ingresoDao.contarActividad(ingresoId) > 0

            require(ReglasIngreso.eliminar(estado = estadoActual, tieneActividadPosterior = tieneActividadPosterior)
            ) { "Solo puedes eliminar una captura registrada sin actividad posterior a su creación." }
            check(ingresoDao.eliminar(ingresoId) == 1) {
                "No fue posible eliminar el ingreso."
            }
        }
    }

    // Cambia el día conservando la hora local de entrada.
    // Si se selecciona el mismo día, conserva exactamente
    // el valor original en milisegundos.
    private fun actualizarDiaConservandoHora(fechaOriginal: Long, nuevaFecha: String): Long {
        val fechaInterpretada = requireNotNull(FormularioIngreso.interpretarFecha(nuevaFecha)) {
            "La fecha de entrada no es válida."
        }

        val original = Calendar.getInstance().apply { timeInMillis = fechaOriginal }
        val seleccionada = Calendar.getInstance().apply { time = fechaInterpretada }

        val mismoDia = original.get(Calendar.YEAR) == seleccionada.get(Calendar.YEAR) &&
                    original.get(Calendar.DAY_OF_YEAR) == seleccionada.get(Calendar.DAY_OF_YEAR)

        if (mismoDia) { return fechaOriginal }

        original.set(seleccionada.get(Calendar.YEAR),
            seleccionada.get(Calendar.MONTH),
            seleccionada.get(Calendar.DAY_OF_MONTH)
        )

        return original.timeInMillis
    }

    private suspend fun obtenerIngresoExistente(ingresoId: Long): Ingreso {
        require(ingresoId > 0) { "El identificador del ingreso no es válido." }
        return requireNotNull(ingresoDao.obtener(ingresoId)) { "El ingreso ya no existe." }
    }

    private suspend fun validarTecnico(tecnicoId: Long) {
        require(tecnicoId > 0) { "Inicia sesión nuevamente para continuar." }
        requireNotNull(tecnicoDao.obtener(tecnicoId)) { "El técnico ya no existe. Inicia sesión nuevamente." }
    }
}