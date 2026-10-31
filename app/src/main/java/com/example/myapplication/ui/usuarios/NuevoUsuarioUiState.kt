package com.example.myapplication.ui.usuarios

import androidx.annotation.StringRes

// Usuario que acaba de guardarse. codigoRecuperacion viene en claro solo aquí, para mostrarlo una vez; es null cuando
// el usuario se creó sin código.
data class UsuarioCreado(
    val tecnicoId: Long,
    val nombre: String,
    val usuario: String,
    val codigoRecuperacion: String? = null)

// Estado del formulario para crear un usuario.
// Los errores de texto salen de ReglasUsuario; solo el usuario repetido se conoce al consultar la base.
data class NuevoUsuarioUiState(
    val nombre: String = "",
    val usuario: String = "",
    val password: String = "",
    val confirmacion: String = "",
    @StringRes val errorUsuarioRepetido: Int? = null,
    @StringRes val errorGeneral: Int? = null,
    val guardando: Boolean = false,
    val creado: UsuarioCreado? = null) {

    // Los errores solo se muestran en campos con texto, para no avisar antes de que se escriba.
    @get:StringRes
    val errorNombre: Int? get() = if (nombre.isEmpty()) null else ReglasUsuario.errorNombre(nombre)

    @get:StringRes
    val errorUsuario: Int? get() {
        if (usuario.isEmpty()) return null
        return ReglasUsuario.errorUsuario(usuario) ?: errorUsuarioRepetido }

    val passwordLarga: Boolean get() = ReglasUsuario.passwordLargaSuficiente(password)
    val passwordDistinta: Boolean get() = ReglasUsuario.passwordDistintaDelUsuario(password, usuario)
    val confirmacionCoincide: Boolean get() = ReglasUsuario.confirmacionCoincide(password, confirmacion)
    val hayConfirmacionIncorrecta: Boolean get() = confirmacion.isNotEmpty() && !confirmacionCoincide

    val formularioValido: Boolean get() =
        ReglasUsuario.errorNombre(nombre) == null &&
            ReglasUsuario.errorUsuario(usuario) == null &&
            passwordLarga && passwordDistinta && confirmacion.isNotEmpty() && confirmacionCoincide

    val puedeCrear: Boolean get() = formularioValido && !guardando && creado == null }
