package com.example.myapplication.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.entity.EventoIngreso
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

// Estado y operaciones del detalle de un ingreso.
class DetalleIngresoViewModel(
    private val ingresoId: Long,
    private val repository: DashboardRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DetalleIngresoUiState())
    val uiState: StateFlow<DetalleIngresoUiState> = _uiState.asStateFlow()
    // Observación activa del expediente; se cancela antes de volver a cargar.
    private var observacionJob: Job? = null
    // Nombres de técnico ya consultados; el historial vuelve a emitir con cada cambio y no hace falta preguntar otra vez.
    private val nombresConsultados = mutableMapOf<Long, String>()
    init { cargarDetalle() }

    // Observa el expediente y su historial.
    // La carga inicial termina cuando ambas consultas han emitido sus resultados.
    private fun cargarDetalle() {
        observacionJob?.cancel()
        if (ingresoId <= 0) {
            _uiState.update {
                it.copy(cargando = false, errorCarga = "El identificador del ingreso no es válido.") }
            return }

        _uiState.update {
            it.copy(cargando = true, errorCarga = null, ingresoNoEncontrado = false) }

        observacionJob = viewModelScope.launch {
            try {
                // Combina el expediente y el historial para emitir un solo estado cuando ambos ya respondieron.
                combine(repository.observarIngresoPorId(ingresoId),
                    repository.observarEventos(ingresoId)) { ingreso, historial ->
                    ingreso to historial
                }.collect { (ingreso, historial) ->
                    val nombres = nombresDeTecnicos(historial)
                    _uiState.update { actual ->
                        actual.copy(cargando = false, ingreso = ingreso,
                            historial = historial, nombresTecnicos = nombres,
                            ingresoNoEncontrado = ingreso == null && !actual.eliminado,
                            errorCarga = null) } }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(cargando = false, errorCarga = "No fue posible cargar el ingreso y su historial.") } } } }

    // Nombre de cada técnico que aparece en el historial. Un técnico que ya no existe queda fuera y se muestra su número.
    private suspend fun nombresDeTecnicos(historial: List<EventoIngreso>): Map<Long, String> {
        historial.map { it.tecnicoId }.distinct().filter { it !in nombresConsultados }.forEach { id ->
            repository.obtenerNombreTecnico(id)?.let { nombresConsultados[id] = it } }
        return nombresConsultados.toMap() }

    // Vuelve a cargar el detalle si no hay una operación en curso.
    fun reintentar() {
        val actual = _uiState.value
        if (actual.procesando || actual.eliminado) { return }
        cargarDetalle() }

    // Pasa el ingreso de registrado a en revisión.
    fun iniciarRevision(tecnicoId: Long) {
        ejecutarOperacion(accion = IngresoAccion.INICIAR_REVISION) {
            repository.cambiarEstado(
                ingresoId = ingresoId, destino = EstadoIngreso.EN_REVISION,
                motivo = "Revisión iniciada.", tecnicoId = tecnicoId) } }

    // Avanza de revisión a reparación; el repositorio exige el checklist guardado.
    fun iniciarReparacion(tecnicoId: Long) {
        ejecutarOperacion(accion = IngresoAccion.INICIAR_REPARACION) {
            repository.cambiarEstado(ingresoId = ingresoId, destino = EstadoIngreso.EN_REPARACION,
                motivo = "Reparación iniciada.", tecnicoId = tecnicoId) } }

    // Termina la reparación; el repositorio exige el diagnóstico guardado.
    fun enviarAFirma(tecnicoId: Long) {
        ejecutarOperacion(accion = IngresoAccion.ENVIAR_A_FIRMA) {
            repository.cambiarEstado(ingresoId = ingresoId, destino = EstadoIngreso.LISTO_PARA_FIRMA,
                motivo = "Reparación terminada; listo para firma.", tecnicoId = tecnicoId) } }

    // Regresa el ingreso de listo para firma a en reparación.
    fun devolverAReparacion(motivo: String, tecnicoId: Long) {
        ejecutarOperacion(accion = IngresoAccion.DEVOLVER_A_REPARACION) {
            repository.cambiarEstado(ingresoId = ingresoId, destino = EstadoIngreso.EN_REPARACION,
                motivo = motivo, tecnicoId = tecnicoId) } }

    // Cancela el ingreso conservando su expediente.
    fun cancelar(motivo: String, tecnicoId: Long) {
        ejecutarOperacion(accion = IngresoAccion.CANCELAR) {
            repository.cancelar(ingresoId = ingresoId, motivo = motivo,
                tecnicoId = tecnicoId) } }

    // Elimina el ingreso si todavía no tiene actividad.
    fun eliminar(tecnicoId: Long) {
        ejecutarOperacion(accion = IngresoAccion.ELIMINAR) {
            repository.eliminar(ingresoId = ingresoId, tecnicoId = tecnicoId) } }

    // Centraliza carga, resultado y errores de las operaciones.
    // La pantalla debe solicitar la confirmación del usuario
    // antes de llamar a cancelar() o eliminar().
    private fun ejecutarOperacion(accion: IngresoAccion, operacion: suspend () -> Unit) {
        val actual = _uiState.value

        if (actual.cargando || actual.procesando || actual.eliminado || actual.operacionExitosa ||
            actual.errorCarga != null || actual.ingreso == null) { return }

        // Se activa antes de lanzar la corrutina.
        _uiState.update {
            it.copy(accionEnCurso = accion, errorOperacion = null, operacionExitosa = false) }

        viewModelScope.launch {
            try {
                operacion()
                _uiState.update {
                    it.copy(operacionExitosa = true, eliminado = accion == IngresoAccion.ELIMINAR,
                        ingresoNoEncontrado = false) }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(errorOperacion = error.message ?: "No fue posible completar la operación.") }
            } finally {
                _uiState.update { it.copy(accionEnCurso = null) } } } }

    // La pantalla lo llama después de cerrar la confirmación
    // de una operación exitosa.
    // Si se eliminó el ingreso, corresponde salir del detalle.
    fun consumirOperacionExitosa() {
        val actual = _uiState.value
        if (actual.procesando || actual.eliminado) { return }
        _uiState.update { it.copy(operacionExitosa = false) } }

    // Quita el mensaje de error de la última operación.
    fun limpiarErrorOperacion() {
        if (_uiState.value.procesando) { return }
        _uiState.update { it.copy(errorOperacion = null) } } }