package com.example.myapplication.util

import java.security.MessageDigest

// Calcula el SHA-256 de la contraseña en hexadecimal; no usa sal, por lo que solo sirve para el acceso local.
object PasswordHasher {
    fun hash(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) } } }