package com.example.myapplication.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.model.EstadoIngreso
import com.example.myapplication.data.local.repository.DashboardRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetalleIngresoViewModel(
    private val ingresoId: Long,
    private val repository: DashboardRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DetalleIngresoUiState())
    val uiState: StateFlow<DetalleIngresoUiState> = _uiState.asStateFlow()
    private var observacionJob: Job? = null
    init { cargarDetalle() }

    // Observa el expediente y su historial.
    // La carga inicial termina cuando ambas consultas han emitido sus resultados.
    private fun cargarDetalle() {
        observacionJob?.cancel()
        if (ingresoId <= 0) {
            _uiState.update {
                it.copy(cargando = false, errorCarga = "El identificador del ingreso no es válido.")
            }
            return
        }

        _uiState.update {
            it.copy(cargando = true, errorCarga = null, ingresoNoEncontrado = false)
        }

        observacionJob = viewModelScope.launch {
            try {
                combine(repository.observarIngresoPorId(ingresoId),
                    repository.observarEventos(ingresoId)) { ingreso, historial ->
                    ingreso to historial
                }.collect { (ingreso, historial) ->
                    _uiState.update { actual ->
                        actual.copy(cargando = false, ingreso = ingreso,
                            historial = historial,
                            ingresoNoEncontrado = ingreso == null && !actual.eliminado,
                            errorCarga = null
                        )
                    }
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(cargando = false, errorCarga = "No fue posible cargar el ingreso y su historial.")
                }
            }
        }
    }

    fun reintentar() {
        val actual = _uiState.value
        if (actual.procesando || actual.eliminado) { return }
        cargarDetalle()
    }

    fun iniciarRevision(motivo: String, tecnicoId: Long) {
        ejecutarOperacion(accion = IngresoAccion.INICIAR_REVISION) {
            repository.cambiarEstado(ingresoId = ingresoId, destino = EstadoIngreso.EN_REVISION,
                motivo = motivo, tecnicoId = tecnicoId
            )
        }
    }

    fun devolverAReparacion(motivo: String, tecnicoId: Long) {
        ejecutarOperacion(accion = IngresoAccion.DEVOLVER_A_REPARACION) {
            repository.cambiarEstado(ingresoId = ingresoId, destino = EstadoIngreso.EN_REPARACION,
                motivo = motivo, tecnicoId = tecnicoId
            )
        }
    }

    fun cancelar(motivo: String, tecnicoId: Long) {
        ejecutarOperacion(accion = IngresoAccion.CANCELAR) {
            repository.cancelar(ingresoId = ingresoId, motivo = motivo,
                tecnicoId = tecnicoId
            )
        }
    }

    //Permite corregir un estado desconocido.
    // El repositorio comprueba también el estado actual
    // y que la transición esté permitida.
    fun regularizarEstado(destino: EstadoIngreso, motivo: String, tecnicoId: Long) {
        ejecutarOperacion(accion = IngresoAccion.REGULARIZAR_ESTADO) {
            require(destino == EstadoIngreso.REGISTRADO || destino == EstadoIngreso.EN_REVISION) {
                "Selecciona Registrado o En revisión."
            }

            repository.cambiarEstado(ingresoId = ingresoId, destino = destino,
                motivo = motivo, tecnicoId = tecnicoId
            )
        }
    }

    fun eliminar(tecnicoId: Long) {
        ejecutarOperacion(accion = IngresoAccion.ELIMINAR) {
            repository.eliminar(ingresoId = ingresoId, tecnicoId = tecnicoId)
        }
    }

    // Centraliza carga, resultado y errores de las operaciones.
    // La pantalla debe solicitar la confirmación del usuario
    // antes de llamar a cancelar() o eliminar().
    private fun ejecutarOperacion(accion: IngresoAccion, operacion: suspend () -> Unit) {
        val actual = _uiState.value

        if (actual.cargando || actual.procesando || actual.eliminado || actual.operacionExitosa ||
            actual.errorCarga != null || actual.ingreso == null) { return }

        // Se activa antes de lanzar la corrutina.
        _uiState.update {
            it.copy(accionEnCurso = accion, errorOperacion = null, operacionExitosa = false)
        }

        viewModelScope.launch {
            try {
                operacion()
                _uiState.update {
                    it.copy(operacionExitosa = true, eliminado = accion == IngresoAccion.ELIMINAR,
                        ingresoNoEncontrado = false)
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(errorOperacion = error.message ?: "No fue posible completar la operación.")
                }
            } finally {
                _uiState.update { it.copy(accionEnCurso = null) }
            }
        }
    }

    // La pantalla lo llama después de cerrar la confirmación
    // de una operación exitosa.
    // Si se eliminó el ingreso, corresponde salir del detalle.
    fun consumirOperacionExitosa() {
        val actual = _uiState.value
        if (actual.procesando || actual.eliminado) { return }
        _uiState.update { it.copy(operacionExitosa = false) }
    }

    fun limpiarErrorOperacion() {
        if (_uiState.value.procesando) { return }
        _uiState.update { it.copy(errorOperacion = null) }
    }
}