package com.example.myapplication.ui.registro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.model.EstadoIngreso
import com.example.myapplication.data.local.model.FormularioIngreso
import com.example.myapplication.data.local.model.IngresoResumen
import com.example.myapplication.data.local.model.ReglasIngreso
import com.example.myapplication.data.local.repository.DashboardRepository
import com.example.myapplication.data.repository.RegistroRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class RegistroViewModel(
    private val repo: RegistroRepository,
    private val dashboardRepository: DashboardRepository,
    private val ingresoId: Long? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        RegistroUiState(
            ingresoIdEnEdicion = ingresoId,
            cargandoIngreso = ingresoId != null
        )
    )

    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    // Referencia para detectar cambios reales en el formulario.
    private var formularioInicial = _uiState.value.aFormulario()
    init {
        // Un registro nuevo no necesita consultar un expediente.
        if (ingresoId != null) { cargarIngreso(ingresoId) }
    }

    // Consulta exclusivamente el ingreso solicitado para edición.
    // La pantalla recibe la carga y sus errores mediante uiState.
    private fun cargarIngreso(id: Long) {
        if (id <= 0) {
            _uiState.update {
                it.copy(cargandoIngreso = false, errorCarga = "El identificador del ingreso no es válido.")
            }
            return
        }

        _uiState.update {
            it.copy(cargandoIngreso = true, errorCarga = null, mensajeError = null)
        }

        viewModelScope.launch {
            try {
                // Cargamos una fotografía inicial del expediente.
                // No observamos continuamente para evitar sobrescribir
                // los cambios que el usuario haga en el formulario.
                val ingreso = dashboardRepository.observarIngresoPorId(id).first()

                if (ingreso == null) {
                    _uiState.update {
                        it.copy(cargandoIngreso = false, errorCarga = "El ingreso ya no existe.")
                    }
                    return@launch
                }
                aplicarIngreso(ingreso)
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(cargandoIngreso = false,
                        errorCarga = "No fue posible cargar el ingreso. Intenta nuevamente."
                    )
                }
            }
        }
    }

    // Permite repetir una consulta fallida.
    // Nunca recarga un formulario que ya tiene cambios pendientes.
    fun reintentarCarga() {
        val id = ingresoId ?: return
        val actual = _uiState.value
        if (actual.cargandoIngreso || actual.cargando || actual.registroExitoso ||
            actual.tieneCambiosSinGuardar || actual.errorCarga == null) { return }
        cargarIngreso(id)
    }

    // Prepara los campos y establece la referencia inicial.
    private fun aplicarIngreso(ingreso: IngresoResumen) {
        val estado = EstadoIngreso.desdeValor(ingreso.estado)
        val nuevoEstado = RegistroUiState(
            placa = ingreso.placa, modelo = ingreso.modelo,
            color = ingreso.color.orEmpty(), numeroSerie = ingreso.numeroSerie.orEmpty(),
            nombreCliente = ingreso.nombreCliente, telefonoCliente = ingreso.telefonoCliente,
            emailCliente = ingreso.emailCliente.orEmpty(), fechaEntrada = FormularioIngreso.formatearFecha(
                ingreso.fechaEntrada),
            motivoIngreso = ingreso.motivoIngreso.orEmpty(),
            condicionInicial = ingreso.condicionInicial.orEmpty(),
            ingresoIdEnEdicion = ingreso.ingresoId,
            estadoIngreso = estado,
            mensajeError = if (ReglasIngreso.editar(estado)) { null } else {
                "Este ingreso está cerrado y no permite modificaciones."
            }
        )
        formularioInicial = nuevoEstado.aFormulario()
        _uiState.value = nuevoEstado
    }

    // Centraliza los cambios de campos.
    // Bloquea modificaciones durante la carga y el guardado.
    // Respeta el modo de edición completa o de contacto.
    private fun cambiarCampo(
        campo: String,
        transformar: (RegistroUiState) -> RegistroUiState
    ) {
        _uiState.update { actual ->
            if (!actual.formularioDisponible || actual.cargando || actual.registroExitoso) {
                return@update actual
            }

            if (actual.esEdicion) {
                if (!ReglasIngreso.editar(actual.estadoIngreso)) {
                    return@update actual
                }
                val soloContacto = ReglasIngreso.soloContacto(actual.estadoIngreso)
                if (soloContacto && campo != "telefono" && campo != "email") {
                    return@update actual
                }
            }

            val modificado = transformar(actual)
            modificado.copy(mensajeError = null,
                erroresCampos = actual.erroresCampos - campo,
                tieneCambiosSinGuardar = modificado.aFormulario() != formularioInicial
            )
        }
    }

    fun onPlacaChange(valor: String) {
        cambiarCampo("placa") { it.copy(placa = valor) }
    }

    fun onModeloChange(valor: String) {
        cambiarCampo("modelo") { it.copy(modelo = valor) }
    }

    fun onColorChange(valor: String) {
        cambiarCampo("color") { it.copy(color = valor) }
    }

    fun onNumeroSerieChange(valor: String) {
        cambiarCampo("numeroSerie") { it.copy(numeroSerie = valor) }
    }

    fun onNombreClienteChange(valor: String) {
        cambiarCampo("nombre") { it.copy(nombreCliente = valor) }
    }

    fun onTelefonoClienteChange(valor: String) {
        cambiarCampo("telefono") { it.copy(telefonoCliente = valor) }
    }

    fun onEmailClienteChange(valor: String) {
        cambiarCampo("email") { it.copy(emailCliente = valor) }
    }

    fun onFechaEntradaChange(valor: String) {
        cambiarCampo("fecha") { it.copy(fechaEntrada = valor) }
    }

    fun onMotivoIngresoChange(valor: String) {
        cambiarCampo("motivo") { it.copy(motivoIngreso = valor) }
    }

    fun onCondicionInicialChange(valor: String) {
        cambiarCampo("condicion") { it.copy(condicionInicial = valor) }
    }

    fun guardar(tecnicoId: Long) {
        val actual = _uiState.value
        // Evita guardar antes de cargar el expediente,
        // durante otra operación o después de un resultado exitoso.
        if (!actual.formularioDisponible || actual.cargando || actual.registroExitoso) {
            return
        }

        if (tecnicoId <= 0) {
            _uiState.update { it.copy(mensajeError = "Inicia sesión nuevamente para continuar.") }
            return
        }

        if (actual.esEdicion && !ReglasIngreso.editar(actual.estadoIngreso)) {
            _uiState.update { it.copy(mensajeError = "Este ingreso está cerrado y no permite modificaciones.") }
            return
        }

        val formulario = actual.aFormulario()
        val soloContacto = actual.esEdicion && ReglasIngreso.soloContacto(actual.estadoIngreso)

        val errores = formulario.errores(soloContacto = soloContacto)

        if (errores.isNotEmpty()) {
            _uiState.update { it.copy(erroresCampos = errores, mensajeError = "Revisa los campos señalados.") }
            return
        }

        // Se activa antes de lanzar la corrutina.
        _uiState.update {
            it.copy(cargando = true, mensajeError = null, erroresCampos = emptyMap())
        }

        viewModelScope.launch {
            try {
                val idEnEdicion = actual.ingresoIdEnEdicion
                val idGuardado = if (idEnEdicion != null) {
                    dashboardRepository.editar(
                        ingresoId = idEnEdicion, formulario = formulario, tecnicoId = tecnicoId)
                    idEnEdicion
                } else {
                    repo.registrarIngreso(
                        placa = formulario.placa, nombreCliente = formulario.nombre,
                        modelo = formulario.modelo, telefonoCliente = formulario.telefono,
                        color = formulario.color, emailCliente = formulario.email,
                        numeroSerie = formulario.numeroSerie,
                        fechaEntrada = fechaDeNuevoIngreso(formulario.fecha),
                        tecnicoId = tecnicoId, motivoIngreso = formulario.motivo,
                        condicionInicial = formulario.condicion
                    )
                }
                formularioInicial = formulario
                _uiState.update {
                    it.copy(registroExitoso = true, idRegistrado = idGuardado, tieneCambiosSinGuardar = false)
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(mensajeError = error.message ?: "No fue posible guardar el ingreso.")
                }
            } finally {
                _uiState.update { it.copy(cargando = false) }
            }
        }
    }

    // Para un ingreso nuevo usamos el día seleccionado
    // y la hora local del momento de guardado.
    // En edición, el repositorio conserva la hora original.
    private fun fechaDeNuevoIngreso(texto: String): Long {
        val fecha = requireNotNull(FormularioIngreso.interpretarFecha(texto)) {
            "La fecha de entrada no es válida."
        }

        val diaSeleccionado = Calendar.getInstance().apply { time = fecha }
        val momentoActual = Calendar.getInstance()
        momentoActual.set(
            diaSeleccionado.get(Calendar.YEAR),
            diaSeleccionado.get(Calendar.MONTH),
            diaSeleccionado.get(Calendar.DAY_OF_MONTH)
        )
        return momentoActual.timeInMillis
    }

    fun resetMensajeError() {
        _uiState.update { it.copy(mensajeError = null) }
    }

    // Prepara una captura nueva después de guardar.
    // En edición solo consume la notificación de éxito,
    // conservando el identificador del ingreso.
    fun resetRegistroExitoso() {
        val actual = _uiState.value
        if (actual.cargandoIngreso || actual.cargando || !actual.registroExitoso) {
            return
        }

        if (actual.esEdicion) {
            _uiState.update {
                it.copy(registroExitoso = false, idRegistrado = null)
            }
        } else {
            val nuevoEstado = RegistroUiState()
            formularioInicial = nuevoEstado.aFormulario()
            _uiState.value = nuevoEstado
        }
    }
}