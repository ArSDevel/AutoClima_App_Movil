package com.example.myapplication.ui.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.repository.HistorialRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

// Consulta el historial con búsqueda, filtros y paginación.
class HistorialViewModel(private val repository: HistorialRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HistorialUiState())
    val uiState = _uiState.asStateFlow()
    private var observacion: Job? = null
    private var limite = 50
    // Impide que una consulta anterior publique sobre criterios más recientes.
    private var solicitud = 0L

    init { observar() }

    // Cambia el texto de búsqueda y espera un momento antes de consultar.
    fun buscar(texto: String) {
        if (texto == _uiState.value.busqueda) return
        _uiState.update { it.copy(busqueda = texto) }
        reiniciar(esperar = true) }

    // Filtra por tipo de actividad.
    fun filtrarAccion(accion: String?) {
        if (accion == _uiState.value.accion) return
        _uiState.update { it.copy(accion = accion) }
        reiniciar() }

    // Filtra por periodo de fechas.
    fun filtrarPeriodo(periodo: PeriodoHistorial) {
        if (periodo == _uiState.value.periodo) return
        _uiState.update { it.copy(periodo = periodo) }
        reiniciar() }

    // Quita búsqueda, tipo de actividad y periodo.
    fun limpiarFiltros() {
        _uiState.update { it.copy(busqueda = "", accion = null, periodo = PeriodoHistorial.TODOS) }
        reiniciar() }

    // Repite la consulta después de un error.
    fun reintentar() = observar()

    // Pide 50 actividades más.
    fun cargarMas() {
        val actual = _uiState.value
        if (actual.cargando || actual.hayError || !actual.hayMas) return
        limite += 50
        observar(conservar = true) }

    // Vuelve a las primeras 50 actividades con los filtros actuales.
    private fun reiniciar(esperar: Boolean = false) {
        limite = 50
        observar(esperar = esperar) }

    // Observa los eventos con los filtros actuales y publica solo el resultado más reciente.
    private fun observar(esperar: Boolean = false, conservar: Boolean = false) {
        observacion?.cancel()
        val turno = ++solicitud
        val criterios = _uiState.value
        val cantidad = limite
        _uiState.update {
            it.copy(cargando = true, hayError = false,
                eventos = if (conservar) it.eventos else emptyList(), hayMas = false) }
        observacion = viewModelScope.launch {
            try {
                if (esperar) delay(300)
                // Fechas del EVENTO en la zona horaria del dispositivo.
                // "7 días" incluye hoy y los seis días calendario anteriores.
                val inicio = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
                val hasta = if (criterios.periodo == PeriodoHistorial.TODOS) null else
                    (inicio.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }.timeInMillis
                val dias = when (criterios.periodo) {
                    PeriodoHistorial.SIETE_DIAS -> 6
                    PeriodoHistorial.TREINTA_DIAS -> 29
                    else -> 0 }
                val desde = if (criterios.periodo == PeriodoHistorial.TODOS) null else
                    inicio.apply { add(Calendar.DAY_OF_YEAR, -dias) }.timeInMillis
                repository.observar(criterios.busqueda, criterios.accion, desde, hasta, cantidad + 1)
                    .collect { filas ->
                        if (turno == solicitud) _uiState.update {
                            it.copy(eventos = filas.take(cantidad), hayMas = filas.size > cantidad,
                                cargando = false, hayError = false) } }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                if (turno == solicitud) _uiState.update {
                    it.copy(cargando = false, hayError = true, hayMas = false) } } } } }
