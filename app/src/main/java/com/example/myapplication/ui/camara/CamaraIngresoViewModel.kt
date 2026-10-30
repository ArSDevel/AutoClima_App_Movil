package com.example.myapplication.ui.camara

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.repository.EvidenciaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// Estado y operaciones de las fotografías de un ingreso.
class CamaraIngresoViewModel(
    private val ingresoId: Long,
    private val repository: EvidenciaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CamaraUiState())
    val uiState = _uiState.asStateFlow()

    // Observación activa del ingreso y sus fotografías; se cancela antes de volver a cargar.
    private var observacion: Job? = null

    init {
        observarIngreso() }

    // La carga termina cuando tenemos tanto el expediente
    // como su lista de fotografías.
    private fun observarIngreso() {
        observacion?.cancel()

        if (ingresoId <= 0) {
            _uiState.update {
                it.copy(
                    cargando = false,
                    errorCarga = "El identificador del ingreso no es válido.") }
            return }

        _uiState.update {
            it.copy(
                cargando = true,
                errorCarga = null,
                ingresoNoEncontrado = false) }

        observacion = viewModelScope.launch {
            try {
                combine(
                    repository.observarIngreso(ingresoId),
                    repository.observarEvidencias(ingresoId)
                ) { ingreso, evidencias ->
                    ingreso to evidencias
                }.collect { (ingreso, evidencias) ->
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            ingreso = ingreso,
                            evidencias = evidencias,
                            ingresoNoEncontrado = ingreso == null,
                            errorCarga = null) } }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        errorCarga = "No fue posible cargar las evidencias.") } } } }

    // Vuelve a consultar el ingreso y sus fotografías.
    fun reintentarCarga() {
        if (_uiState.value.guardando) return
        observarIngreso() }

    // Recibe la captura para que el usuario pueda revisarla.
    // Todavía no se inserta en Room.
    fun recibirFoto(archivo: File) {
        val actual = _uiState.value

        if (actual.guardando || actual.rutaPendiente != null) return

        _uiState.update {
            it.copy(
                rutaPendiente = archivo.absolutePath,
                descripcion = "",
                errorGuardado = null,
                guardadoExitoso = false,
                evidenciaGuardadaId = null) } }

    // Actualiza la descripción de la foto que se va a guardar.
    fun cambiarDescripcion(valor: String) {
        val actual = _uiState.value

        if (actual.guardando || actual.rutaPendiente == null) return

        _uiState.update {
            it.copy(
                descripcion = valor,
                errorGuardado = null) } }

    // Conserva la captura si falla el guardado,
    // de modo que pueda reintentarse.
    fun guardar(tecnicoId: Long) {
        val actual = _uiState.value

        if (!actual.puedeGuardar) return

        val ruta = actual.rutaPendiente ?: return

        if (tecnicoId <= 0) {
            _uiState.update {
                it.copy(
                    errorGuardado = "Inicia sesión nuevamente para continuar.") }
            return }

        if (actual.descripcion.trim().length > 300) {
            _uiState.update {
                it.copy(
                    errorGuardado =
                        "La descripción debe tener como máximo 300 caracteres.") }
            return }

        // Se activa antes de lanzar la operación para evitar duplicados.
        _uiState.update {
            it.copy(
                guardando = true,
                errorGuardado = null,
                guardadoExitoso = false) }

        viewModelScope.launch {
            try {
                val evidenciaId = repository.guardar(
                    ingresoId = ingresoId,
                    tecnicoId = tecnicoId,
                    archivo = File(ruta),
                    descripcion = actual.descripcion)

                _uiState.update {
                    it.copy(
                        rutaPendiente = null,
                        descripcion = "",
                        evidenciaGuardadaId = evidenciaId,
                        guardadoExitoso = true) }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        errorGuardado = error.message
                            ?: "No fue posible guardar la evidencia.") }
            } finally {
                _uiState.update {
                    it.copy(guardando = false) } } } }

    // Solo descarta la captura pendiente.
    // No elimina fotografías que ya están registradas en Room.
    fun descartarFoto() {
        val actual = _uiState.value
        val ruta = actual.rutaPendiente ?: return

        if (actual.guardando) return

        _uiState.update {
            it.copy(
                guardando = true,
                errorGuardado = null) }

        viewModelScope.launch {
            try {
                // El archivo se borra en un hilo de entrada y salida; si ya no existe se considera descartado.
                val eliminada = withContext(Dispatchers.IO) {
                    val archivo = File(ruta)
                    !archivo.exists() || archivo.delete() }

                check(eliminada) {
                    "No fue posible descartar la fotografía. Inténtalo nuevamente." }

                _uiState.update {
                    it.copy(
                        rutaPendiente = null,
                        descripcion = "",
                        errorGuardado = null) }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        errorGuardado = error.message
                            ?: "No fue posible descartar la fotografía.") }
            } finally {
                _uiState.update {
                    it.copy(guardando = false) } } } }

    // Consume la notificación después de mostrar la confirmación.
    fun consumirGuardadoExitoso() {
        _uiState.update {
            it.copy(guardadoExitoso = false) } }

    // Marca una fotografía como principal del ingreso.
    fun seleccionarPrincipal(evidenciaId: Int, tecnicoId: Long) {
        ejecutarCambio("Fotografía principal actualizada.") {
            repository.seleccionarPrincipal(ingresoId, evidenciaId, tecnicoId) } }

    // Elimina una fotografía guardada.
    fun eliminarEvidencia(evidenciaId: Int, tecnicoId: Long) {
        ejecutarCambio("Fotografía eliminada.") {
            repository.eliminar(ingresoId, evidenciaId, tecnicoId) } }

    // Ejecuta un cambio en la galería y muestra su resultado.
    private fun ejecutarCambio(mensaje: String, operacion: suspend () -> Unit) {
        val actual = _uiState.value
        if (!actual.permiteAgregar || actual.guardando || actual.rutaPendiente != null) return
        _uiState.update { it.copy(guardando = true, errorGaleria = null, mensajeGaleria = null) }
        viewModelScope.launch {
            try {
                operacion()
                _uiState.update { it.copy(mensajeGaleria = mensaje) }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                _uiState.update { it.copy(errorGaleria = error.message ?: "No fue posible completar la operación.") }
            } finally {
                _uiState.update { it.copy(guardando = false) } } } }

    // Oculta el mensaje de resultado de la galería.
    fun limpiarMensajeGaleria() {
        _uiState.update { it.copy(mensajeGaleria = null, errorGaleria = null) } } }