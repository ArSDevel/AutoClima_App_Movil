package com.example.myapplication.ui.checklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.repository.DashboardRepository
import com.example.myapplication.data.repository.ChecklistRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChecklistViewModel(
    private val ingresoId: Long,
    private val repo: ChecklistRepository,
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChecklistUiState())
    val uiState = _uiState.asStateFlow()

    private var cargaJob: Job? = null

    // Conserva las respuestas iniciales para identificar
    // si el usuario realmente modificó el formulario.
    private var formularioInicial = _uiState.value.datosFormulario()

    init {
        cargarChecklist()
    }

    // Carga el ingreso y el checklist asociado.
    // Cuando no existe un checklist, las preguntas quedan pendientes.
    private fun cargarChecklist() {
        cargaJob?.cancel()

        if (ingresoId <= 0) {
            _uiState.value = ChecklistUiState(
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
                    _uiState.value = ChecklistUiState(
                        cargando = false,
                        ingresoNoEncontrado = true
                    )
                    return@launch
                }

                val checklist = repo.obtenerPorIngreso(ingresoId)

                val nuevoEstado = ChecklistUiState(
                    ingreso = ingreso,
                    checklistId = checklist?.checklistId,

                    faltaGasRefrigerante =
                        checklist?.faltaGasRefrigerante,
                    fugaVisibleMangueras =
                        checklist?.fugaVisibleMangueras,
                    fugasDetectadasLuzUV =
                        checklist?.fugasDetectadasLuzUV,

                    compresorEmbragaAlEncender =
                        checklist?.compresorEmbragaAlEncender,
                    compresorDanadoOAmarrado =
                        checklist?.compresorDanadoOAmarrado,
                    bandaCompresorDesgastada =
                        checklist?.bandaCompresorDesgastada,

                    funcionanAbanicosRadiador =
                        checklist?.funcionanAbanicosRadiador,
                    filtroCabinaSucioUObstruido =
                        checklist?.filtroCabinaSucioUObstruido,
                    condensadorObstruido =
                        checklist?.condensadorObstruido,

                    comentarios = checklist?.comentarios.orEmpty(),
                    fechaUltimoGuardado = checklist?.fecha,
                    cargando = false
                )

                formularioInicial = nuevoEstado.datosFormulario()
                _uiState.value = nuevoEstado
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        errorCarga = error.message
                            ?: "No fue posible cargar el checklist."
                    )
                }
            }
        }
    }

    // Reintenta una carga fallida sin sobrescribir
    // las respuestas que el usuario ya está modificando.
    fun reintentarCarga() {
        val actual = _uiState.value

        if (
            actual.cargando ||
            actual.guardando ||
            actual.tieneCambiosSinGuardar ||
            actual.guardadoExitoso
        ) {
            return
        }

        if (actual.errorCarga == null && !actual.ingresoNoEncontrado) {
            return
        }

        cargarChecklist()
    }

    // Centraliza las modificaciones del formulario.
    // Respeta el estado del ingreso y bloquea cambios al guardar.
    private fun cambiarCampo(
        transformar: (ChecklistUiState) -> ChecklistUiState
    ) {
        _uiState.update { actual ->
            if (!actual.puedeModificar) {
                return@update actual
            }

            val modificado = transformar(actual)

            modificado.copy(
                errorGuardado = null,
                tieneCambiosSinGuardar =
                    modificado.datosFormulario() != formularioInicial
            )
        }
    }

    // Refrigerante y fugas.
    fun onFaltaGasRefrigeranteChange(valor: Boolean) {
        cambiarCampo { it.copy(faltaGasRefrigerante = valor) }
    }

    fun onFugaVisibleManguerasChange(valor: Boolean) {
        cambiarCampo { it.copy(fugaVisibleMangueras = valor) }
    }

    fun onFugasDetectadasLuzUVChange(valor: Boolean) {
        cambiarCampo { it.copy(fugasDetectadasLuzUV = valor) }
    }

    // Compresor y transmisión.
    fun onCompresorEmbragaAlEncenderChange(valor: Boolean) {
        cambiarCampo { it.copy(compresorEmbragaAlEncender = valor) }
    }

    fun onCompresorDanadoOAmarradoChange(valor: Boolean) {
        cambiarCampo { it.copy(compresorDanadoOAmarrado = valor) }
    }

    fun onBandaCompresorDesgastadaChange(valor: Boolean) {
        cambiarCampo { it.copy(bandaCompresorDesgastada = valor) }
    }

    // Ventilación y limpieza.
    fun onFuncionanAbanicosRadiadorChange(valor: Boolean) {
        cambiarCampo { it.copy(funcionanAbanicosRadiador = valor) }
    }

    fun onFiltroCabinaSucioUObstruidoChange(valor: Boolean) {
        cambiarCampo { it.copy(filtroCabinaSucioUObstruido = valor) }
    }

    fun onCondensadorObstruidoChange(valor: Boolean) {
        cambiarCampo { it.copy(condensadorObstruido = valor) }
    }

    fun onComentariosChange(valor: String) {
        cambiarCampo { it.copy(comentarios = valor) }
    }

    // Comprueba las respuestas y guarda con el técnico autenticado.
    // No utiliza un identificador de técnico predeterminado.
    fun guardar(tecnicoId: Long) {
        val actual = _uiState.value

        if (
            !actual.formularioDisponible ||
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

        if (!actual.permiteEdicion) {
            _uiState.update {
                it.copy(
                    errorGuardado = "El checklist solo se puede modificar " +
                            "durante la revisión o la reparación."
                )
            }
            return
        }

        // No convierte respuestas pendientes en "No".
        if (!actual.estaCompleto) {
            _uiState.update {
                it.copy(
                    mostrarPreguntasPendientes = true,
                    errorGuardado = "Responde las nueve preguntas antes de guardar."
                )
            }
            return
        }

        // Evita operaciones innecesarias si ya está guardado
        // y el usuario no modificó ninguna respuesta.
        if (actual.checklistId != null && !actual.tieneCambiosSinGuardar) {
            return
        }

        // Se activa antes de lanzar la corrutina para evitar
        // que dos pulsaciones inicien guardados simultáneos.
        _uiState.update {
            it.copy(
                guardando = true,
                errorGuardado = null,
                mostrarPreguntasPendientes = false
            )
        }

        viewModelScope.launch {
            try {
                val checklistId = repo.guardarChecklist(
                    ingresoId = ingresoId,
                    faltaGasRefrigerante =
                        requireNotNull(actual.faltaGasRefrigerante),
                    fugaVisibleMangueras =
                        requireNotNull(actual.fugaVisibleMangueras),
                    fugasDetectadasLuzUV =
                        requireNotNull(actual.fugasDetectadasLuzUV),
                    compresorEmbragaAlEncender =
                        requireNotNull(actual.compresorEmbragaAlEncender),
                    compresorDanadoOAmarrado =
                        requireNotNull(actual.compresorDanadoOAmarrado),
                    bandaCompresorDesgastada =
                        requireNotNull(actual.bandaCompresorDesgastada),
                    funcionanAbanicosRadiador =
                        requireNotNull(actual.funcionanAbanicosRadiador),
                    filtroCabinaSucioUObstruido =
                        requireNotNull(actual.filtroCabinaSucioUObstruido),
                    condensadorObstruido =
                        requireNotNull(actual.condensadorObstruido),
                    comentarios = actual.comentarios,
                    tecnicoId = tecnicoId
                )

                formularioInicial = actual.datosFormulario()

                // El guardado ya terminó correctamente.
                // La fecha se consulta después sin convertir un fallo
                // de lectura en un supuesto fallo del guardado.
                _uiState.update {
                    it.copy(
                        checklistId = checklistId,
                        guardadoExitoso = true,
                        tieneCambiosSinGuardar = false
                    )
                }

                try {
                    val guardado = repo.obtenerPorIngreso(ingresoId)

                    _uiState.update {
                        it.copy(fechaUltimoGuardado = guardado?.fecha)
                    }
                } catch (cancelacion: CancellationException) {
                    throw cancelacion
                } catch (_: Exception) {
                    // No muestra una fecha anterior como si
                    // correspondiera al último guardado.
                    _uiState.update {
                        it.copy(fechaUltimoGuardado = null)
                    }
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        errorGuardado = error.message
                            ?: "No fue posible guardar el checklist."
                    )
                }
            } finally {
                _uiState.update { it.copy(guardando = false) }
            }
        }
    }

    // La pantalla consume el resultado después de mostrar
    // la confirmación. Conserva las respuestas guardadas.
    fun consumirGuardadoExitoso() {
        if (_uiState.value.guardando) {
            return
        }

        _uiState.update { it.copy(guardadoExitoso = false) }
    }

    fun limpiarErrorGuardado() {
        if (_uiState.value.guardando) {
            return
        }

        _uiState.update { it.copy(errorGuardado = null) }
    }

    // Compara únicamente las respuestas y los comentarios.
    // Los cambios de carga, fecha o mensajes no ensucian el formulario.
    private fun ChecklistUiState.datosFormulario(): List<Any?> {
        return listOf(
            faltaGasRefrigerante,
            fugaVisibleMangueras,
            fugasDetectadasLuzUV,
            compresorEmbragaAlEncender,
            compresorDanadoOAmarrado,
            bandaCompresorDesgastada,
            funcionanAbanicosRadiador,
            filtroCabinaSucioUObstruido,
            condensadorObstruido,
            comentarios.trim()
        )
    }
}