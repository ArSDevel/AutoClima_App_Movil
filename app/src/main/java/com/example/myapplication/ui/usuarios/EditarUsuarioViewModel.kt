package com.example.myapplication.ui.usuarios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.R
import com.example.myapplication.data.local.dao.TecnicoDao
import com.example.myapplication.util.CodigoRecuperacion
import com.example.myapplication.util.PasswordHasher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Edita el nombre, el usuario y la contraseña de un técnico, y genera el código de recuperación del propio.
class EditarUsuarioViewModel(
    private val tecnicoDao: TecnicoDao,
    private val tecnicoActualId: Long,
    private val tecnicoId: Long
) : ViewModel() {

    private val esPropio = tecnicoActualId == tecnicoId
    private val _uiState = MutableStateFlow(EditarUsuarioUiState(esPropio = esPropio))
    val uiState: StateFlow<EditarUsuarioUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val tecnico = tecnicoDao.obtener(tecnicoId)
                _uiState.update {
                    if (tecnico == null) {
                        it.copy(cargando = false, noEncontrado = true)
                    } else {
                        it.copy(cargando = false, nombre = tecnico.nombre, usuario = tecnico.usuario,
                            nombreOriginal = tecnico.nombre, usuarioOriginal = tecnico.usuario,
                            tieneRecuperacion = tecnico.recuperacionHash != null) } }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _uiState.update { it.copy(cargando = false, noEncontrado = true) } } } }

    // Los campos quedan fijos mientras se guarda o después de guardar.
    private fun editable(): Boolean = _uiState.value.let { !it.cargando && !it.guardando && it.guardado == null }

    fun onNombreChange(valor: String) {
        if (editable()) _uiState.update { it.copy(nombre = valor, errorGeneral = null) } }

    fun onUsuarioChange(valor: String) {
        if (editable() && !esPropio) {
            _uiState.update { it.copy(usuario = valor, errorUsuarioRepetido = null, errorGeneral = null) } } }

    fun onPasswordActualChange(valor: String) {
        if (editable()) _uiState.update { it.copy(passwordActual = valor, errorPasswordActual = null, errorGeneral = null) } }

    fun onPasswordNuevaChange(valor: String) {
        if (editable()) _uiState.update { it.copy(passwordNueva = valor, errorGeneral = null) } }

    fun onConfirmacionChange(valor: String) {
        if (editable()) _uiState.update { it.copy(confirmacion = valor, errorGeneral = null) } }

    // Guarda los cambios. En el perfil propio, cambiar la contraseña exige escribir la actual.
    fun guardar() {
        val actual = _uiState.value
        if (!actual.puedeGuardar) return
        val nombre = ReglasUsuario.limpiarNombre(actual.nombre)
        val usuario = if (esPropio) actual.usuarioOriginal else actual.usuario.trim()
        _uiState.update { it.copy(guardando = true, errorGeneral = null, errorPasswordActual = null,
            errorUsuarioRepetido = null) }
        viewModelScope.launch {
            try {
                val tecnico = tecnicoDao.obtener(tecnicoId)
                if (tecnico == null) {
                    _uiState.update { it.copy(guardando = false, noEncontrado = true) }
                    return@launch }
                if (usuario != actual.usuarioOriginal && tecnicoDao.contarPorUsuarioExcepto(usuario, tecnicoId) > 0) {
                    _uiState.update { it.copy(guardando = false,
                        errorUsuarioRepetido = R.string.usuarios_error_usuario_repetido) }
                    return@launch }
                if (actual.cambiaPassword && esPropio) {
                    val correcta = withContext(Dispatchers.Default) {
                        PasswordHasher.verificar(actual.passwordActual, tecnico.passwordHash) }
                    if (!correcta) {
                        _uiState.update { it.copy(guardando = false,
                            errorPasswordActual = R.string.usuarios_error_password_actual) }
                        return@launch } }
                if (nombre != actual.nombreOriginal || usuario != actual.usuarioOriginal) {
                    tecnicoDao.actualizarDatos(tecnicoId, nombre, usuario) }
                if (actual.cambiaPassword) {
                    val hash = withContext(Dispatchers.Default) { PasswordHasher.hash(actual.passwordNueva) }
                    tecnicoDao.actualizarPassword(tecnicoId, hash) }
                _uiState.update {
                    it.copy(guardando = false, passwordActual = "", passwordNueva = "", confirmacion = "",
                        guardado = UsuarioEditado(tecnicoId, nombre, usuario, esPropio)) }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _uiState.update { it.copy(guardando = false, errorGeneral = R.string.usuarios_error_guardar) } } } }

    // Código de recuperación: solo en el perfil propio y después de escribir la contraseña actual.
    fun pedirCodigo() {
        if (!esPropio || !editable()) return
        _uiState.update { it.copy(pidiendoPasswordCodigo = true, passwordCodigo = "", errorPasswordCodigo = null) } }

    fun onPasswordCodigoChange(valor: String) {
        _uiState.update { if (it.generandoCodigo) it else it.copy(passwordCodigo = valor, errorPasswordCodigo = null) } }

    fun cancelarCodigo() {
        _uiState.update { if (it.generandoCodigo) it else it.copy(pidiendoPasswordCodigo = false, passwordCodigo = "",
            errorPasswordCodigo = null) } }

    // Comprueba la contraseña y, si es correcta, reemplaza el código anterior por uno nuevo que se muestra una vez.
    fun confirmarCodigo() {
        val actual = _uiState.value
        if (!esPropio || !actual.pidiendoPasswordCodigo || actual.generandoCodigo) return
        if (actual.passwordCodigo.isEmpty()) {
            _uiState.update { it.copy(errorPasswordCodigo = R.string.usuarios_error_password_actual) }
            return }
        _uiState.update { it.copy(generandoCodigo = true, errorPasswordCodigo = null) }
        viewModelScope.launch {
            try {
                val tecnico = tecnicoDao.obtener(tecnicoId)
                val correcta = tecnico != null && withContext(Dispatchers.Default) {
                    PasswordHasher.verificar(actual.passwordCodigo, tecnico.passwordHash) }
                if (!correcta) {
                    _uiState.update { it.copy(generandoCodigo = false,
                        errorPasswordCodigo = R.string.usuarios_error_password_actual) }
                    return@launch }
                val codigo = CodigoRecuperacion.generar()
                val hash = withContext(Dispatchers.Default) { PasswordHasher.hash(CodigoRecuperacion.normalizar(codigo)) }
                tecnicoDao.actualizarRecuperacion(tecnicoId, hash)
                _uiState.update { it.copy(generandoCodigo = false, pidiendoPasswordCodigo = false, passwordCodigo = "",
                    codigoNuevo = codigo, tieneRecuperacion = true) }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _uiState.update { it.copy(generandoCodigo = false, errorPasswordCodigo = R.string.usuarios_error_guardar) } } } }

    // El código solo se muestra una vez: al cerrarlo ya no queda en memoria.
    fun cerrarCodigo() { _uiState.update { it.copy(codigoNuevo = null) } } }
