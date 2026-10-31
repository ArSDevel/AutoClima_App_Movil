package com.example.myapplication.ui.usuarios

import androidx.annotation.StringRes
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

// Resultado de restablecer la contraseña: el código anterior queda usado y se entrega uno nuevo.
data class ContrasenaRestablecida(val usuario: String, val codigoNuevo: String)

// Estado de la pantalla "Olvidé mi contraseña".
data class RecuperarPasswordUiState(
    val usuario: String = "",
    val codigo: String = "",
    val password: String = "",
    val confirmacion: String = "",
    val guardando: Boolean = false,
    @StringRes val error: Int? = null,
    val resultado: ContrasenaRestablecida? = null) {

    val passwordLarga: Boolean get() = ReglasUsuario.passwordLargaSuficiente(password)
    val passwordDistinta: Boolean get() = ReglasUsuario.passwordDistintaDelUsuario(password, usuario)
    val hayConfirmacionIncorrecta: Boolean get() =
        confirmacion.isNotEmpty() && !ReglasUsuario.confirmacionCoincide(password, confirmacion)

    val puedeRestablecer: Boolean get() = !guardando && resultado == null &&
        usuario.isNotBlank() && CodigoRecuperacion.tieneFormatoValido(codigo) &&
        passwordLarga && passwordDistinta && confirmacion.isNotEmpty() &&
        ReglasUsuario.confirmacionCoincide(password, confirmacion) }

// Restablece la contraseña con el código de recuperación, sin iniciar sesión.
class RecuperarPasswordViewModel(
    private val tecnicoDao: TecnicoDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecuperarPasswordUiState())
    val uiState: StateFlow<RecuperarPasswordUiState> = _uiState.asStateFlow()

    private fun editable(): Boolean = _uiState.value.let { !it.guardando && it.resultado == null }

    fun onUsuarioChange(valor: String) { if (editable()) _uiState.update { it.copy(usuario = valor, error = null) } }
    fun onCodigoChange(valor: String) { if (editable()) _uiState.update { it.copy(codigo = valor, error = null) } }
    fun onPasswordChange(valor: String) { if (editable()) _uiState.update { it.copy(password = valor, error = null) } }
    fun onConfirmacionChange(valor: String) {
        if (editable()) _uiState.update { it.copy(confirmacion = valor, error = null) } }

    // Comprueba usuario y código y deja la contraseña nueva. Si el usuario no existe, no tiene código o el código no
    // coincide, el mensaje es el mismo para no revelar qué usuarios existen.
    fun restablecer() {
        val actual = _uiState.value
        if (!actual.puedeRestablecer) return
        val usuario = actual.usuario.trim()
        val codigo = CodigoRecuperacion.normalizar(actual.codigo)
        _uiState.update { it.copy(guardando = true, error = null) }
        viewModelScope.launch {
            try {
                val tecnico = tecnicoDao.buscarPorUsuario(usuario)
                val hashActual = tecnico?.recuperacionHash
                val codigoCorrecto = hashActual != null && withContext(Dispatchers.Default) {
                    PasswordHasher.verificar(codigo, hashActual) }
                if (tecnico == null || hashActual == null || !codigoCorrecto) {
                    _uiState.update { it.copy(guardando = false, error = R.string.recuperar_error_datos) }
                    return@launch }
                val codigoNuevo = CodigoRecuperacion.generar()
                val (hashPassword, hashCodigoNuevo) = withContext(Dispatchers.Default) {
                    PasswordHasher.hash(actual.password) to PasswordHasher.hash(CodigoRecuperacion.normalizar(codigoNuevo)) }
                val filas = tecnicoDao.restablecerConCodigo(tecnico.tecnicoId, hashPassword, hashCodigoNuevo, hashActual)
                _uiState.update {
                    if (filas == 0) {
                        it.copy(guardando = false, error = R.string.recuperar_error_datos)
                    } else {
                        it.copy(guardando = false, password = "", confirmacion = "", codigo = "",
                            resultado = ContrasenaRestablecida(tecnico.usuario, codigoNuevo)) } }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _uiState.update { it.copy(guardando = false, error = R.string.usuarios_error_guardar) } } } } }
