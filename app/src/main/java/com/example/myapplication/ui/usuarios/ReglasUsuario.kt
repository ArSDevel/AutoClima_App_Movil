package com.example.myapplication.ui.usuarios

import androidx.annotation.StringRes
import com.example.myapplication.R

// Reglas para crear un usuario; las usan la pantalla de primer uso y la de nuevo usuario.
object ReglasUsuario {
    const val MIN_USUARIO = 3
    const val MAX_USUARIO = 30
    const val MIN_PASSWORD = 6
    private const val MIN_NOMBRE = 2

    // Nombre sin espacios sobrantes; es el que se guarda.
    fun limpiarNombre(nombre: String): String = nombre.trim().replace(Regex("\\s+"), " ")

    @StringRes
    fun errorNombre(nombre: String): Int? {
        return if (limpiarNombre(nombre).length < MIN_NOMBRE) R.string.usuarios_error_nombre else null }

    // El usuario se escribe en el inicio de sesión, por eso no admite espacios.
    @StringRes
    fun errorUsuario(usuario: String): Int? {
        val texto = usuario.trim()
        return when {
            texto.isEmpty() -> R.string.usuarios_error_usuario_vacio
            texto.any { it.isWhitespace() } -> R.string.usuarios_error_usuario_espacios
            texto.length < MIN_USUARIO -> R.string.usuarios_error_usuario_corto
            texto.length > MAX_USUARIO -> R.string.usuarios_error_usuario_largo
            else -> null } }

    fun passwordLargaSuficiente(password: String): Boolean = password.length >= MIN_PASSWORD

    // Con el usuario vacío no hay con qué comparar, así que la regla se da por cumplida.
    fun passwordDistintaDelUsuario(password: String, usuario: String): Boolean {
        val texto = usuario.trim()
        return texto.isEmpty() || !password.equals(texto, ignoreCase = true) }

    fun confirmacionCoincide(password: String, confirmacion: String): Boolean = password == confirmacion }
