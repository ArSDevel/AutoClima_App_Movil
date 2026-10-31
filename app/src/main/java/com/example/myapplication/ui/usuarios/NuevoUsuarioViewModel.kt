package com.example.myapplication.ui.usuarios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.R
import com.example.myapplication.data.local.dao.TecnicoDao
import com.example.myapplication.data.local.entity.Tecnico
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

// Crea usuarios nuevos. Sirve para el primer uso de la app y para agregar más técnicos con la sesión abierta.
// En el primer uso se genera además el código de recuperación, porque es el único usuario y nadie más puede restablecer
// su contraseña; a los que se agregan después cada quien se genera el suyo desde su perfil.
class NuevoUsuarioViewModel(
    private val tecnicoDao: TecnicoDao,
    private val generarCodigoRecuperacion: Boolean = false
) : ViewModel() {

    private val _uiState = MutableStateFlow(NuevoUsuarioUiState())
    val uiState: StateFlow<NuevoUsuarioUiState> = _uiState.asStateFlow()

    // Mientras se guarda o después de guardar el formulario queda fijo.
    private fun editable(): Boolean = !_uiState.value.guardando && _uiState.value.creado == null

    fun onNombreChange(valor: String) {
        if (editable()) _uiState.update { it.copy(nombre = valor, errorGeneral = null) } }

    // Al cambiar el usuario deja de valer el aviso de "ya existe".
    fun onUsuarioChange(valor: String) {
        if (editable()) {
            _uiState.update { it.copy(usuario = valor, errorUsuarioRepetido = null, errorGeneral = null) } } }

    fun onPasswordChange(valor: String) {
        if (editable()) _uiState.update { it.copy(password = valor, errorGeneral = null) } }

    fun onConfirmacionChange(valor: String) {
        if (editable()) _uiState.update { it.copy(confirmacion = valor, errorGeneral = null) } }

    // Guarda el usuario si los datos son válidos y el nombre de usuario no está ocupado.
    fun crear() {
        val actual = _uiState.value
        if (!actual.puedeCrear) return
        val usuario = actual.usuario.trim()
        val nombre = ReglasUsuario.limpiarNombre(actual.nombre)
        val password = actual.password
        _uiState.update { it.copy(guardando = true, errorGeneral = null, errorUsuarioRepetido = null) }
        viewModelScope.launch {
            try {
                if (tecnicoDao.contarPorUsuario(usuario) > 0) {
                    _uiState.update {
                        it.copy(guardando = false, errorUsuarioRepetido = R.string.usuarios_error_usuario_repetido) }
                    return@launch }
                // El cálculo del hash se hace fuera del hilo principal.
                val codigo = if (generarCodigoRecuperacion) CodigoRecuperacion.generar() else null
                val (hash, hashCodigo) = withContext(Dispatchers.Default) {
                    PasswordHasher.hash(password) to codigo?.let { PasswordHasher.hash(CodigoRecuperacion.normalizar(it)) } }
                val id = tecnicoDao.insertar(
                    Tecnico(nombre = nombre, usuario = usuario, passwordHash = hash, recuperacionHash = hashCodigo))
                _uiState.update {
                    it.copy(guardando = false, password = "", confirmacion = "",
                        creado = UsuarioCreado(tecnicoId = id, nombre = nombre, usuario = usuario,
                            codigoRecuperacion = codigo)) }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _uiState.update { it.copy(guardando = false, errorGeneral = R.string.usuarios_error_guardar) } } } } }
