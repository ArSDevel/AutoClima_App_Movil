package com.example.myapplication.ui.diagnostico

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.model.ConceptoCotizacionFormulario
import com.example.myapplication.data.local.model.FormularioDiagnostico
import com.example.myapplication.data.local.repository.DashboardRepository
import com.example.myapplication.data.local.repository.DiagnosticoRepository
import com.example.myapplication.data.repository.ChecklistRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal

class DiagnosticoViewModel(
    private val ingresoId: Long,
    private val repository: DiagnosticoRepository,
    private val dashboardRepository: DashboardRepository,
    private val checklistRepository: ChecklistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiagnosticoUiState())
    val uiState = _uiState.asStateFlow()

    private var cargaJob: Job? = null

    // Referencia para detectar cambios reales en las notas
    // y en los conceptos de la cotización.
    private var formularioInicial = FormularioDiagnostico()

    init {
        cargarDiagnostico()
    }

    // Recupera la información necesaria para presentar el reporte.
    // Si todavía no existe un diagnóstico, prepara un formulario vacío.
    private fun cargarDiagnostico() {
        cargaJob?.cancel()

        if (ingresoId <= 0) {
            _uiState.value = DiagnosticoUiState(
                cargando = false,
                errorCarga = "El identificador del ingreso no es válido."
            )
            return
        }

        _uiState.update {
            it.copy(
                cargando = true,
                errorCarga = null,
                ingresoNoEncontrado = false
            )
        }

        cargaJob = viewModelScope.launch {
            try {
                val ingreso = dashboardRepository
                    .observarIngresoPorId(ingresoId)
                    .first()

                if (ingreso == null) {
                    _uiState.value = DiagnosticoUiState(
                        cargando = false,
                        ingresoNoEncontrado = true
                    )
                    return@launch
                }

                val checklist = checklistRepository.obtenerPorIngreso(
                    ingresoId
                )

                val guardado = repository.obtenerPorIngreso(ingresoId)

                val formulario = FormularioDiagnostico(
                    observaciones = guardado
                        ?.diagnostico
                        ?.observaciones
                        .orEmpty(),

                    conceptos = guardado?.conceptos?.map { concepto ->
                        ConceptoCotizacionFormulario(
                            descripcion = concepto.descripcion,
                            importe = BigDecimal.valueOf(
                                concepto.importeCentavos,
                                2
                            ).toPlainString()
                        )
                    }.orEmpty()
                )

                formularioInicial = formulario.normalizado()

                _uiState.value = DiagnosticoUiState(
                    ingreso = ingreso,
                    checklist = checklist,
                    diagnosticoId = guardado?.diagnostico?.diagnosticoId,
                    formulario = formulario,
                    fechaUltimoGuardado =
                        guardado?.diagnostico?.fechaActualizacion,
                    tecnicoUltimoGuardadoId =
                        guardado?.diagnostico?.tecnicoId,
                    cargando = false
                )
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        errorCarga = error.message
                            ?: "No fue posible cargar el diagnóstico."
                    )
                }
            }
        }
    }

    // Permite repetir una carga fallida.
    // No sobrescribe un formulario con cambios pendientes.
    fun reintentarCarga() {
        val actual = _uiState.value

        if (
            actual.cargando ||
            actual.guardando ||
            actual.guardadoExitoso ||
            actual.tieneCambiosSinGuardar
        ) {
            return
        }

        if (actual.errorCarga == null && !actual.ingresoNoEncontrado) {
            return
        }

        cargarDiagnostico()
    }

    // Centraliza los cambios y comprueba si el formulario
    // sigue siendo diferente de su última versión guardada.
    private fun cambiarFormulario(
        transformar: (FormularioDiagnostico) -> FormularioDiagnostico
    ) {
        _uiState.update { actual ->
            if (!actual.puedeModificar) {
                return@update actual
            }

            val modificado = transformar(actual.formulario)

            // Después del primer intento de guardado, mantiene
            // actualizados los errores mientras se corrigen los campos.
            val erroresActualizados = if (actual.erroresCampos.isNotEmpty()) {
                modificado.errores()
            } else {
                emptyMap()
            }

            actual.copy(
                formulario = modificado,
                erroresCampos = erroresActualizados,
                errorGuardado = null,
                tieneCambiosSinGuardar =
                    modificado.normalizado() != formularioInicial
            )
        }
    }

    // Modifica únicamente las notas del diagnóstico.
    // Los comentarios del checklist se mantienen separados.
    fun onObservacionesChange(valor: String) {
        cambiarFormulario {
            it.copy(observaciones = valor)
        }
    }

    // Agrega una fila vacía para que el mecánico capture
    // el concepto y su importe.
    fun agregarConcepto() {
        cambiarFormulario {
            it.copy(
                conceptos = it.conceptos + ConceptoCotizacionFormulario()
            )
        }
    }

    fun onDescripcionConceptoChange(
        indice: Int,
        valor: String
    ) {
        cambiarFormulario { formulario ->
            if (indice !in formulario.conceptos.indices) {
                return@cambiarFormulario formulario
            }

            formulario.copy(
                conceptos = formulario.conceptos.mapIndexed { posicion, concepto ->
                    if (posicion == indice) {
                        concepto.copy(descripcion = valor)
                    } else {
                        concepto
                    }
                }
            )
        }
    }

    // Conserva el texto tal como se escribe.
    // La validación y conversión a centavos pertenecen al formulario.
    fun onImporteConceptoChange(
        indice: Int,
        valor: String
    ) {
        cambiarFormulario { formulario ->
            if (indice !in formulario.conceptos.indices) {
                return@cambiarFormulario formulario
            }

            formulario.copy(
                conceptos = formulario.conceptos.mapIndexed { posicion, concepto ->
                    if (posicion == indice) {
                        concepto.copy(importe = valor)
                    } else {
                        concepto
                    }
                }
            )
        }
    }

    // Retira una fila del formulario.
    // La base de datos solo cambia cuando se pulsa Guardar.
    fun eliminarConcepto(indice: Int) {
        cambiarFormulario { formulario ->
            if (indice !in formulario.conceptos.indices) {
                return@cambiarFormulario formulario
            }

            formulario.copy(
                conceptos = formulario.conceptos.filterIndexed { posicion, _ ->
                    posicion != indice
                }
            )
        }
    }

    // Guarda con el identificador real del técnico autenticado.
    // No cambia el estado del ingreso ni envía el reporte.
    fun guardar(tecnicoId: Long) {
        val actual = _uiState.value

        if (
            !actual.reporteDisponible ||
            actual.guardando ||
            actual.guardadoExitoso
        ) {
            return
        }

        if (tecnicoId <= 0) {
            _uiState.update {
                it.copy(
                    errorGuardado = "Inicia sesión nuevamente para continuar."
                )
            }
            return
        }

        if (!actual.tieneChecklist) {
            _uiState.update {
                it.copy(
                    errorGuardado =
                        "Guarda primero el checklist para elaborar el diagnóstico."
                )
            }
            return
        }

        if (!actual.permiteEdicion) {
            _uiState.update {
                it.copy(
                    errorGuardado = "Solo puedes modificar el diagnóstico " +
                            "durante la revisión o la reparación."
                )
            }
            return
        }

        val errores = actual.formulario.errores()

        if (errores.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    erroresCampos = errores,
                    errorGuardado = "Revisa los campos de la cotización."
                )
            }
            return
        }

        // No repite el guardado cuando el diagnóstico ya existe
        // y no se modificó su contenido.
        if (
            actual.diagnosticoId != null &&
            !actual.tieneCambiosSinGuardar
        ) {
            return
        }

        val formulario = actual.formulario.normalizado()

        // Bloquea nuevas pulsaciones antes de lanzar la corrutina.
        _uiState.update {
            it.copy(
                guardando = true,
                errorGuardado = null,
                erroresCampos = emptyMap()
            )
        }

        viewModelScope.launch {
            try {
                val diagnosticoId = repository.guardar(
                    ingresoId = ingresoId,
                    formulario = formulario,
                    tecnicoId = tecnicoId
                )

                formularioInicial = formulario

                // El guardado ya fue confirmado por el repositorio.
                // Actualiza la pantalla sin perder los datos del reporte.
                _uiState.update {
                    it.copy(
                        diagnosticoId = diagnosticoId,
                        formulario = formulario,
                        guardadoExitoso = true,
                        tieneCambiosSinGuardar = false,
                        fechaUltimoGuardado = null,
                        tecnicoUltimoGuardadoId = null
                    )
                }

                // Recupera la fecha y el autor realmente persistidos.
                // Un fallo de esta consulta no convierte el guardado
                // exitoso en un error de escritura.
                try {
                    val guardado = repository.obtenerPorIngreso(ingresoId)

                    _uiState.update {
                        it.copy(
                            fechaUltimoGuardado =
                                guardado?.diagnostico?.fechaActualizacion,
                            tecnicoUltimoGuardadoId =
                                guardado?.diagnostico?.tecnicoId
                        )
                    }
                } catch (cancelacion: CancellationException) {
                    throw cancelacion
                } catch (_: Exception) {
                    // Las notas y la cotización ya quedaron guardadas.
                    // Los metadatos se recuperarán al volver a abrir.
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        errorGuardado = error.message
                            ?: "No fue posible guardar el diagnóstico."
                    )
                }
            } finally {
                _uiState.update {
                    it.copy(guardando = false)
                }
            }
        }
    }

    // Consume la confirmación sin borrar el formulario guardado.
    fun consumirGuardadoExitoso() {
        if (_uiState.value.guardando) {
            return
        }

        _uiState.update {
            it.copy(guardadoExitoso = false)
        }
    }

    fun limpiarErrorGuardado() {
        if (_uiState.value.guardando) {
            return
        }

        _uiState.update {
            it.copy(errorGuardado = null)
        }
    }
}