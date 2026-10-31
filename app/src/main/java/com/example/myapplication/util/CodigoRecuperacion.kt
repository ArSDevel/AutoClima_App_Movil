package com.example.myapplication.util

import java.security.SecureRandom

// Código que permite restablecer la contraseña sin iniciar sesión. Se muestra una sola vez y en la base solo queda su
// hash, como una contraseña. Tiene 12 caracteres, agrupados de cuatro en cuatro (XXXX-XXXX-XXXX).
object CodigoRecuperacion {
    // Sin 0, O, 1 ni I para que no se confundan al copiarlo a mano.
    private const val ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    private const val LARGO = 12
    private const val GRUPO = 4

    private val aleatorio = SecureRandom()

    fun generar(): String {
        val caracteres = (1..LARGO).map { ALFABETO[aleatorio.nextInt(ALFABETO.length)] }
        return caracteres.chunked(GRUPO).joinToString("-") { it.joinToString("") } }

    // Deja solo letras y números en mayúsculas, para aceptar el código con o sin guiones, espacios o minúsculas.
    fun normalizar(texto: String): String = texto.filter { it.isLetterOrDigit() }.uppercase()

    fun tieneFormatoValido(texto: String): Boolean {
        val limpio = normalizar(texto)
        return limpio.length == LARGO && limpio.all { it in ALFABETO } } }
