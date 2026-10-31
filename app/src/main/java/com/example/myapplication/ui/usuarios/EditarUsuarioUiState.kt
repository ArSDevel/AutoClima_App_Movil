package com.example.myapplication.ui.usuarios

import androidx.annotation.StringRes

// Datos con los que quedó el usuario después de guardar; la lista y el menú los usan para avisar y actualizar el nombre.
data class UsuarioEditado(val tecnicoId: Long, val nombre: String, val usuario: String, val esPropio: Boolean)

// Estado de la pantalla para editar un usuario.
// En el perfil propio el usuario de la sesión no se puede cambiar y la contraseña pide la actual; en el de otro técnico
// se puede cambiar el usuario y restablecer la contraseña sin pedirla, que es la ayuda cuando alguien la olvidó.
data class EditarUsuarioUiState(
    val cargando: Boolean = true,
    val noEncontrado: Boolean = false,
    val esPropio: Boolean = false,
    val nombre: String = "",
    val usuario: String = "",
    val nombreOriginal: String = "",
    val usuarioOriginal: String = "",
    val passwordActual: String = "",
    val passwordNueva: String = "",
    val confirmacion: String = "",
    val tieneRecuperacion: Boolean = false,
    @StringRes val errorUsuarioRepetido: Int? = null,
    @StringRes val errorPasswordActual: Int? = null,
    @StringRes val errorGeneral: Int? = null,
    val guardando: Boolean = false,
    val guardado: UsuarioEditado? = null,
    // Código de recuperación: primero se pide la contraseña y luego se muestra el código nuevo una sola vez.
    val pidiendoPasswordCodigo: Boolean = false,
    val passwordCodigo: String = "",
    @StringRes val errorPasswordCodigo: Int? = null,
    val generandoCodigo: Boolean = false,
    val codigoNuevo: String? = null) {

    // Los errores de texto solo se muestran cuando el campo tiene algo escrito.
    @get:StringRes
    val errorNombre: Int? get() = if (nombre.isEmpty()) null else ReglasUsuario.errorNombre(nombre)

    @get:StringRes
    val errorUsuario: Int? get() {
        if (esPropio) return null
        return ReglasUsuario.errorUsuario(usuario) ?: errorUsuarioRepetido }

    val cambiaPassword: Boolean get() = passwordNueva.isNotEmpty() || confirmacion.isNotEmpty()
    val passwordLarga: Boolean get() = ReglasUsuario.passwordLargaSuficiente(passwordNueva)
    val passwordDistinta: Boolean get() = ReglasUsuario.passwordDistintaDelUsuario(passwordNueva, usuario)
    val hayConfirmacionIncorrecta: Boolean get() =
        confirmacion.isNotEmpty() && !ReglasUsuario.confirmacionCoincide(passwordNueva, confirmacion)

    private val passwordValida: Boolean get() = !cambiaPassword || (
        passwordLarga && passwordDistinta && confirmacion.isNotEmpty() &&
            ReglasUsuario.confirmacionCoincide(passwordNueva, confirmacion) &&
            (!esPropio || passwordActual.isNotEmpty()))

    val hayCambios: Boolean get() =
        ReglasUsuario.limpiarNombre(nombre) != nombreOriginal ||
            (!esPropio && usuario.trim() != usuarioOriginal) || cambiaPassword

    val puedeGuardar: Boolean get() = !cargando && !noEncontrado && !guardando && guardado == null &&
        ReglasUsuario.errorNombre(nombre) == null &&
        (esPropio || ReglasUsuario.errorUsuario(usuario) == null) &&
        passwordValida && hayCambios }
