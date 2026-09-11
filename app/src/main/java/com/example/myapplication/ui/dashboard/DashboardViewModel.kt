package com.example.myapplication.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.repository.DashboardRepository
import com.example.myapplication.data.local.model.EstadoIngreso
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class DashboardViewModel(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // Cada consulta tiene su propia observación.
    // Cambiar filtros no reinicia la consulta del resumen.
    private var observacion: Job? = null

    // Límites utilizados por la consulta actual.
    private var rangoEnConsulta: RangoFecha? = null
    init { observarIngresos() }

    fun cambiarBusqueda(nuevoTexto: String) {
        if (nuevoTexto == _uiState.value.textoBusqueda) return
        _uiState.update { it.copy(textoBusqueda = nuevoTexto) }
        observarIngresos()
    }

    fun cambiarEstado(nuevoEstado: EstadoIngreso?) {
        if (nuevoEstado == _uiState.value.estadoSeleccionado) return
        _uiState.update { it.copy(estadoSeleccionado = nuevoEstado) }
        observarIngresos()
    }

    fun cambiarFecha(nuevaFecha: FiltroFechaIngreso) {
        if (nuevaFecha == _uiState.value.fechaSeleccionada) {
            actualizarPeriodoFecha()
            return
        }
        _uiState.update { it.copy(fechaSeleccionada = nuevaFecha) }
        observarIngresos()
    }

    fun cambiarOrden(nuevoOrden: OrdenIngreso) {
        if (nuevoOrden == _uiState.value.ordenSeleccionado) return
        _uiState.update { it.copy(ordenSeleccionado = nuevoOrden) }
        observarIngresos()
    }

    // Limpia todos los filtros con una sola actualización.
    // Conserva el orden elegido por el usuario.
    fun limpiarFiltros() {
        if (!_uiState.value.hayFiltrosActivos) return
        _uiState.update {
            it.copy(textoBusqueda = "", estadoSeleccionado = null,
                fechaSeleccionada = FiltroFechaIngreso.TODAS
            )
        }
        observarIngresos()
    }

    // Reintenta exclusivamente la consulta del listado.
    fun reintentar() {
        observarIngresos()
    }

    // La pantalla llamará a este método al volver al primer plano.
    //  Si cambió el día o la zona horaria, recalcula los límites
    //  del periodo relativo seleccionado.
    //  Solo reinicia la consulta si los límites son diferentes
    fun actualizarPeriodoFecha() {
        val nuevoRango = calcularRangoFecha(
            _uiState.value.fechaSeleccionada
        )

        if (nuevoRango != rangoEnConsulta) {
            observarIngresos()
        }
    }

    private fun observarIngresos() {
        observacion?.cancel()

        // Capturamos todos los criterios para esta consulta.
        val criterios = _uiState.value
        val rango = calcularRangoFecha(criterios.fechaSeleccionada)

        rangoEnConsulta = rango

        // Retira resultados anteriores para no mostrarlos
        // como si pertenecieran a los filtros recién seleccionados.
        _uiState.update {
            it.copy(ingresos = emptyList(), cargando = true, hayError = false)
        }

        observacion = viewModelScope.launch {
            try {
                repository.observarIngresos(textoBusqueda = criterios.textoBusqueda,
                    estadoFiltro = criterios.estadoSeleccionado, fechaDesde = rango.desde,
                    fechaHastaExclusiva = rango.hastaExclusiva, orden = valorOrden(criterios.ordenSeleccionado)
                ).collect { ingresos ->
                    _uiState.update { it.copy(ingresos = ingresos, cargando = false, hayError = false) }
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _uiState.update { it.copy(ingresos = emptyList(), cargando = false, hayError = true) }
            }
        }
    }

    // Traduce las opciones de la interfaz al contrato del repositorio.
    private fun valorOrden(orden: OrdenIngreso): String {
        return when (orden) {
            OrdenIngreso.RECIENTES -> "RECIENTES"
            OrdenIngreso.ANTIGUOS -> "ANTIGUOS"
            OrdenIngreso.PLACA_ASCENDENTE -> "PLACA_ASCENDENTE"
        }
    }

    // Convierte el periodo seleccionado en límites de tiempo.
    // Usa el calendario y la zona horaria del dispositivo.
    // Avanza por días de calendario, no sumando 24 horas fijas.
    private fun calcularRangoFecha(
        filtro: FiltroFechaIngreso
    ): RangoFecha {
        if (filtro == FiltroFechaIngreso.TODAS) {
            return RangoFecha(
                desde = null,
                hastaExclusiva = null
            )
        }

        val inicioHoy = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return when (filtro) {
            FiltroFechaIngreso.TODAS -> {
                RangoFecha(desde = null, hastaExclusiva = null)
            }

            FiltroFechaIngreso.HOY -> {
                val inicioManana = copiaCalendario(inicioHoy).apply {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
                RangoFecha(desde = inicioHoy.timeInMillis, hastaExclusiva = inicioManana.timeInMillis)
            }

            FiltroFechaIngreso.ULTIMOS_SIETE_DIAS -> {
                // Incluye hoy y los seis días anteriores.
                val inicioPeriodo = copiaCalendario(inicioHoy).apply {
                    add(Calendar.DAY_OF_MONTH, -6)
                }
                val inicioManana = copiaCalendario(inicioHoy).apply {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
                RangoFecha(desde = inicioPeriodo.timeInMillis, hastaExclusiva = inicioManana.timeInMillis)
            }

            FiltroFechaIngreso.ESTE_MES -> {
                val inicioMes = copiaCalendario(inicioHoy).apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                }

                val inicioSiguienteMes = copiaCalendario(inicioMes).apply {
                    add(Calendar.MONTH, 1)
                }

                RangoFecha(
                    desde = inicioMes.timeInMillis,
                    hastaExclusiva = inicioSiguienteMes.timeInMillis
                )
            }
        }
    }

    // Conserva la zona horaria y los valores del calendario original.
    private fun copiaCalendario(original: Calendar): Calendar {
        return original.clone() as Calendar
    }
    // Modelo interno para los límites de la consulta.
    private data class RangoFecha(val desde: Long?, val hastaExclusiva: Long?)
}