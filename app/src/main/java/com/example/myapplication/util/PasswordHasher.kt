package com.example.myapplication.util

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

// Guarda y comprueba contraseñas. El formato actual es "pbkdf2$iteraciones$sal$hash" (PBKDF2 con HMAC-SHA256 y una
// sal distinta por contraseña, en hexadecimal). Las contraseñas anteriores eran un SHA-256 sin sal, de 64 caracteres;
// siguen reconociéndose para poder iniciar sesión y se vuelven a guardar con el formato nuevo al entrar.
object PasswordHasher {
    private const val PREFIJO = "pbkdf2"
    private const val SEPARADOR = '$'
    private const val ITERACIONES = 120_000
    private const val BYTES_SAL = 16
    private const val BITS_CLAVE = 256

    private val aleatorio = SecureRandom()

    // Genera el texto que se guarda en la base de datos; cada llamada usa una sal nueva.
    fun hash(password: String): String {
        val sal = ByteArray(BYTES_SAL).also { aleatorio.nextBytes(it) }
        return listOf(PREFIJO, ITERACIONES.toString(), aTexto(sal), aTexto(derivar(password, sal, ITERACIONES)))
            .joinToString(SEPARADOR.toString()) }

    // Indica si la contraseña coincide con lo guardado, tanto en el formato nuevo como en el anterior.
    fun verificar(password: String, guardado: String): Boolean {
        if (!guardado.startsWith("$PREFIJO$SEPARADOR")) {
            return MessageDigest.isEqual(sha256(password).toByteArray(), guardado.toByteArray()) }
        val partes = guardado.split(SEPARADOR)
        if (partes.size != 4) return false
        val iteraciones = partes[1].toIntOrNull() ?: return false
        val sal = deTexto(partes[2]) ?: return false
        val esperado = deTexto(partes[3]) ?: return false
        if (iteraciones <= 0) return false
        return MessageDigest.isEqual(derivar(password, sal, iteraciones), esperado) }

    // Verdadero para lo guardado con el formato anterior; conviene volver a guardarlo con hash().
    fun requiereActualizar(guardado: String): Boolean = !guardado.startsWith("$PREFIJO$SEPARADOR")

    private fun derivar(password: String, sal: ByteArray, iteraciones: Int): ByteArray {
        val especificacion = PBEKeySpec(password.toCharArray(), sal, iteraciones, BITS_CLAVE)
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(especificacion).encoded
        } finally {
            especificacion.clearPassword() } }

    // Formato anterior; solo se usa para reconocer contraseñas guardadas antes del cambio.
    private fun sha256(password: String): String = aTexto(MessageDigest.getInstance("SHA-256").digest(password.toByteArray()))

    private fun aTexto(bytes: ByteArray): String = bytes.joinToString("") { "%02x".format(it) }

    private fun deTexto(texto: String): ByteArray? {
        if (texto.isEmpty() || texto.length % 2 != 0) return null
        return try {
            ByteArray(texto.length / 2) { texto.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
        } catch (_: NumberFormatException) { null } } }
