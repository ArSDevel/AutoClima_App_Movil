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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

// Estado y operaciones del registro y la edición de un ingreso.
class RegistroViewModel(
    private val repo: RegistroRepository,
    private val dashboardRepository: DashboardRepository,
    private val ingresoId: Long? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        RegistroUiState(
            ingresoIdEnEdicion = ingresoId,
            cargandoIngreso = ingresoId != null))

    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    // Referencia para detectar cambios reales en el formulario.
    private var formularioInicial = _uiState.value.aFormulario()
    // Observación de las fotografías del ingreso en edición.
    private var fotosJob: Job? = null

    init {
        // Un registro nuevo no necesita consultar un expediente.
        if (ingresoId != null) { cargarIngreso(ingresoId) } }

    // Consulta exclusivamente el ingreso solicitado para edición.
    // La pantalla recibe la carga y sus errores mediante uiState.
    private fun cargarIngreso(id: Long) {
        if (id <= 0) {
            _uiState.update {
                it.copy(cargandoIngreso = false, errorCarga = "El identificador del ingreso no es válido.") }
            return }

        _uiState.update {
            it.copy(cargandoIngreso = true, errorCarga = null, mensajeError = null) }

        viewModelScope.launch {
            try {
                // Cargamos una copia inicial de los datos del expediente.
                // No observamos continuamente para evitar sobrescribir
                // los cambios que el usuario haga en el formulario.
                val ingreso = dashboardRepository.observarIngresoPorId(id).first()

                if (ingreso == null) {
                    _uiState.update {
                        it.copy(cargandoIngreso = false, errorCarga = "El ingreso ya no existe.") }
                    return@launch }
                aplicarIngreso(ingreso, repo.obtenerFotos(id).map { it.rutaImagen })
                observarFotosGuardadas(id)
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(cargandoIngreso = false,
                        errorCarga = "No fue posible cargar el ingreso. Intenta nuevamente.") } } } }

    // Actualiza únicamente las fotos al volver de Evidencias.
    // Los campos que el usuario está escribiendo se conservan.
    private fun observarFotosGuardadas(id: Long) {
        fotosJob?.cancel()
        fotosJob = viewModelScope.launch {
            try {
                combine(repo.observarFotos(id), dashboardRepository.observarIngresoPorId(id)) { fotos, ingreso ->
                    fotos to ingreso
                }.collect { (fotos, ingreso) ->
                    if (ingreso != null) {
                        _uiState.update { actual ->
                            // El guardado actual actualizará su propia referencia al terminar.
                            if (actual.cargando || actual.registroExitoso) return@update actual
                            val guardadas = (fotos.map { it.rutaImagen } + listOfNotNull(ingreso.fotoPath)).distinct()
                            // Conserva la foto principal que el técnico eligió sin guardar; si no hay una elegida,
                            // usa la del ingreso.
                            val elegida = if (actual.fotoPath != actual.fotoPathOriginal &&
                                (actual.fotoPath in guardadas || actual.fotoPath in actual.fotosNuevas)) {
                                actual.fotoPath
                            } else ingreso.fotoPath ?: actual.fotosNuevas.firstOrNull()
                            actual.copy(
                                fotosGuardadas = guardadas,
                                fotoPathOriginal = ingreso.fotoPath,
                                fotoPath = elegida,
                                estadoIngreso = EstadoIngreso.desdeValor(ingreso.estado),
                                tieneCambiosSinGuardar = actual.aFormulario() != formularioInicial ||
                                        actual.fotosNuevas.isNotEmpty() || elegida != ingreso.fotoPath) } } }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _uiState.update { it.copy(mensajeError = "No fue posible actualizar las fotografías. Vuelve a abrir el ingreso.") } } } }

    // Paso del formulario al que pertenece cada campo: 0 cliente, 1 vehículo, 2 recepción.
    private fun pasoDeCampo(campo: String): Int = when (campo) {
        "nombre", "telefono", "email" -> 0
        "placa", "modelo", "color", "numeroSerie" -> 1
        else -> 2 }

    // Comprueba solo los campos del paso indicado. Si hay errores los marca y devuelve false.
    fun validarPaso(paso: Int): Boolean {
        val actual = _uiState.value
        val soloContacto = actual.esEdicion && ReglasIngreso.soloContacto(actual.estadoIngreso)
        val errores = actual.aFormulario().errores(soloContacto = soloContacto)
            .filterKeys { pasoDeCampo(it) == paso }
        if (errores.isEmpty()) return true
        _uiState.update { it.copy(erroresCampos = it.erroresCampos + errores, mensajeError = null) }
        return false }

    // Cambia el paso del formulario sin perder lo capturado.
    fun cambiarPaso(valor: Int) {
        _uiState.update { it.copy(paso = valor.coerceIn(0, 2)) } }

    // Vuelve a cargar el ingreso que se está editando tras una consulta fallida.
    // Nunca recarga un formulario que ya tiene cambios pendientes.
    fun reintentarCarga() {
        val id = ingresoId ?: return
        val actual = _uiState.value
        if (actual.cargandoIngreso || actual.cargando || actual.registroExitoso ||
            actual.tieneCambiosSinGuardar || actual.errorCarga == null) { return }
        cargarIngreso(id) }

    // Prepara los campos y establece la referencia inicial.
    private fun aplicarIngreso(ingreso: IngresoResumen, fotos: List<String>) {
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
            fotoPath = ingreso.fotoPath,
            fotoPathOriginal = ingreso.fotoPath,
            fotosGuardadas = (fotos + listOfNotNull(ingreso.fotoPath)).distinct(),
            mensajeError = if (ReglasIngreso.editar(estado)) { null } else {
                "Este ingreso está cerrado y no permite modificaciones." })
        formularioInicial = nuevoEstado.aFormulario()
        _uiState.value = nuevoEstado }

    // Centraliza los cambios de campos.
    // Bloquea modificaciones durante la carga y el guardado.
    // Respeta el modo de edición completa o de contacto.
    private fun cambiarCampo(
        campo: String,
        transformar: (RegistroUiState) -> RegistroUiState) {
        _uiState.update { actual ->
            if (!actual.formularioDisponible || actual.cargando || actual.registroExitoso) {
                return@update actual }

            if (actual.esEdicion) {
                if (!ReglasIngreso.editar(actual.estadoIngreso)) {
                    return@update actual }
                val soloContacto = ReglasIngreso.soloContacto(actual.estadoIngreso)
                if (soloContacto && campo != "telefono" && campo != "email") {
                    return@update actual } }

            val modificado = transformar(actual)
            modificado.copy(mensajeError = null,
                erroresCampos = actual.erroresCampos - campo,
                tieneCambiosSinGuardar = modificado.aFormulario() != formularioInicial ||
                        modificado.fotoPath != modificado.fotoPathOriginal || modificado.fotosNuevas.isNotEmpty()) } }

    // Actualiza la placa, ya normalizada.
    fun onPlacaChange(valor: String) {
        cambiarCampo("placa") { it.copy(placa = FormularioIngreso.normalizarPlaca(valor)) } }

    // Actualiza el modelo del vehículo.
    fun onModeloChange(valor: String) {
        cambiarCampo("modelo") { it.copy(modelo = valor.take(FormularioIngreso.MAXIMO_MODELO)) } }

    // Actualiza el color del vehículo.
    fun onColorChange(valor: String) {
        cambiarCampo("color") { it.copy(color = valor.take(FormularioIngreso.MAXIMO_COLOR)) } }

    // Actualiza el número de serie.
    fun onNumeroSerieChange(valor: String) {
        cambiarCampo("numeroSerie") { it.copy(numeroSerie = FormularioIngreso.normalizarSerie(valor)) } }

    // Actualiza el nombre del cliente.
    fun onNombreClienteChange(valor: String) {
        cambiarCampo("nombre") { it.copy(nombreCliente = valor.take(FormularioIngreso.MAXIMO_NOMBRE)) } }

    // Actualiza el teléfono del cliente.
    fun onTelefonoClienteChange(valor: String) {
        cambiarCampo("telefono") { it.copy(telefonoCliente = valor.take(FormularioIngreso.MAXIMO_TELEFONO)) } }

    // Actualiza el correo del cliente.
    fun onEmailClienteChange(valor: String) {
        cambiarCampo("email") { it.copy(emailCliente = valor.take(FormularioIngreso.MAXIMO_EMAIL)) } }

    // Actualiza la fecha de entrada.
    fun onFechaEntradaChange(valor: String) {
        cambiarCampo("fecha") { it.copy(fechaEntrada = valor) } }

    // Actualiza el motivo del ingreso.
    fun onMotivoIngresoChange(valor: String) {
        cambiarCampo("motivo") { it.copy(motivoIngreso = valor.take(FormularioIngreso.MAXIMO_MOTIVO)) } }

    // Actualiza la condición inicial del vehículo.
    fun onCondicionInicialChange(valor: String) {
        cambiarCampo("condicion") { it.copy(condicionInicial = valor.take(FormularioIngreso.MAXIMO_CONDICION)) } }

    // La fotografía respeta las mismas restricciones que los datos del vehículo.
    fun onFotoTomada(path: String) {
        if (path.isBlank()) return
        cambiarCampo("foto") {
            it.copy(
                fotosNuevas = (it.fotosNuevas + path).distinct(),
                fotoPath = it.fotoPath ?: path) } }

    // Elige cuál de las fotos será la principal.
    fun seleccionarFotoPrincipal(ruta: String) {
        val actual = _uiState.value
        if (ruta !in actual.fotosNuevas && ruta !in actual.fotosGuardadas) return
        cambiarCampo("foto") { it.copy(fotoPath = ruta) } }

    // Las fotos guardadas se administran desde Evidencias.
    // Un borrador nuevo sí puede quedarse sin fotografías.
    fun quitarFotoPendiente(ruta: String) {
        if (ruta !in _uiState.value.fotosNuevas) return
        cambiarCampo("foto") {
            val restantes = it.fotosNuevas - ruta
            it.copy(
                fotosNuevas = restantes,
                fotoPath = if (it.fotoPath == ruta) {
                    it.fotosGuardadas.firstOrNull() ?: restantes.firstOrNull()
                } else it.fotoPath) } }

    // Valida el formulario y guarda el ingreso nuevo o los cambios.
    fun guardar(tecnicoId: Long) {
        val actual = _uiState.value
        // Evita guardar antes de cargar el expediente,
        // durante otra operación o después de un resultado exitoso.
        if (!actual.formularioDisponible || actual.cargando || actual.registroExitoso) {
            return }

        if (tecnicoId <= 0) {
            _uiState.update { it.copy(mensajeError = "Inicia sesión nuevamente para continuar.") }
            return }

        if (actual.esEdicion && !ReglasIngreso.editar(actual.estadoIngreso)) {
            _uiState.update { it.copy(mensajeError = "Este ingreso está cerrado y no permite modificaciones.") }
            return }

        val formulario = actual.aFormulario()
        val soloContacto = actual.esEdicion && ReglasIngreso.soloContacto(actual.estadoIngreso)

        val errores = formulario.errores(soloContacto = soloContacto)

        if (errores.isNotEmpty()) {
            // Lleva al técnico al primer paso que tiene algo por corregir.
            val primerPaso = errores.keys.minOf { pasoDeCampo(it) }
            _uiState.update {
                it.copy(erroresCampos = errores, mensajeError = "Revisa los campos señalados.", paso = primerPaso) }
            return }

        // Se activa antes de lanzar la corrutina.
        _uiState.update {
            it.copy(cargando = true, mensajeError = null, erroresCampos = emptyMap()) }

        viewModelScope.launch {
            try {
                val idEnEdicion = actual.ingresoIdEnEdicion
                val idGuardado = if (idEnEdicion != null) {
                    repo.editarIngreso(
                        ingresoId = idEnEdicion,
                        formulario = formulario,
                        tecnicoId = tecnicoId,
                        nuevaFotoPath = actual.fotoPath.takeIf {
                            it != actual.fotoPathOriginal },
                        nuevasFotosPaths = actual.fotosNuevas)
                    idEnEdicion
                } else {
                    repo.registrarIngreso(
                        placa = formulario.placa, nombreCliente = formulario.nombre,
                        modelo = formulario.modelo, telefonoCliente = formulario.telefono,
                        color = formulario.color, emailCliente = formulario.email,
                        numeroSerie = formulario.numeroSerie,
                        fechaEntrada = fechaDeNuevoIngreso(formulario.fecha),
                        tecnicoId = tecnicoId, motivoIngreso = formulario.motivo,
                        condicionInicial = formulario.condicion,
                        fotoPath = actual.fotoPath,
                        fotosPaths = actual.fotosNuevas) }
                formularioInicial = formulario
                _uiState.update {
                    it.copy(
                        registroExitoso = true,
                        idRegistrado = idGuardado,
                        tieneCambiosSinGuardar = false,
                        fotoPathOriginal = actual.fotoPath,
                        fotosGuardadas = (actual.fotosGuardadas + actual.fotosNuevas).distinct(),
                        fotosNuevas = emptyList()) }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(mensajeError = error.message ?: "No fue posible guardar el ingreso.") }
            } finally {
                _uiState.update { it.copy(cargando = false) } } } }

    // Para un ingreso nuevo usamos el día seleccionado
    // y la hora local del momento de guardado.
    // En edición, el repositorio conserva la hora original.
    private fun fechaDeNuevoIngreso(texto: String): Long {
        val fecha = requireNotNull(FormularioIngreso.interpretarFecha(texto)) {
            "La fecha de entrada no es válida." }

        val diaSeleccionado = Calendar.getInstance().apply { time = fecha }
        val momentoActual = Calendar.getInstance()
        momentoActual.set(
            diaSeleccionado.get(Calendar.YEAR),
            diaSeleccionado.get(Calendar.MONTH),
            diaSeleccionado.get(Calendar.DAY_OF_MONTH))
        return momentoActual.timeInMillis }

    // Prepara una captura nueva después de guardar.
    // En edición solo consume la notificación de éxito,
    // conservando el identificador del ingreso.
    fun resetRegistroExitoso() {
        val actual = _uiState.value
        if (actual.cargandoIngreso || actual.cargando || !actual.registroExitoso) {
            return }

        if (actual.esEdicion) {
            _uiState.update {
                it.copy(registroExitoso = false, idRegistrado = null) }
        } else {
            val nuevoEstado = RegistroUiState()
            formularioInicial = nuevoEstado.aFormulario()
            _uiState.value = nuevoEstado } } }