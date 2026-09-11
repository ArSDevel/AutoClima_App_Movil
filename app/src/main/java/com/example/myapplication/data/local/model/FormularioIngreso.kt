package com.example.myapplication.data.local.model

import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/* Datos utilizados para registrar o editar un ingreso.
No es una entidad de Room: representa los valores que el usuario captura en el formulario.*/
data class FormularioIngreso(
    val placa: String = "",
    val modelo: String = "",
    val color: String = "",
    val numeroSerie: String = "",
    val nombre: String = "",
    val telefono: String = "",
    val email: String = "",
    val fecha: String = fechaActual(),
    val motivo: String = "",
    val condicion: String = ""
) {
    // Devuelve los errores identificados por nombre de campo.
    // En edición de contacto únicamente valida teléfono y correo.
    fun errores(soloContacto: Boolean = false): Map<String, String> {
        val errores = mutableMapOf<String, String>()
        if (!soloContacto) {
            if (placa.isBlank()) { errores["placa"] = "Escribe la placa." }
            if (modelo.isBlank()) { errores["modelo"] = "Escribe el modelo." }
            if (nombre.isBlank()) { errores["nombre"] = "Escribe el nombre del cliente." }
            if (interpretarFecha(fecha) == null) { errores["fecha"] = "Escribe una fecha válida con formato dd/MM/aaaa." }
        }

        val telefonoLimpio = telefono.trim()
        val cantidadDigitos = telefonoLimpio.count { it in '0'..'9' }
        val caracteresValidos = telefonoLimpio.all { it in '0'..'9' || it in "+ -()" }
        val signoMasValido = telefonoLimpio.count { it == '+' } <= 1 &&
                ('+' !in telefonoLimpio || telefonoLimpio.startsWith("+"))

        if (cantidadDigitos !in 7..15 || !caracteresValidos || !signoMasValido) {
            errores["telefono"] = "Escribe un teléfono de 7 a 15 dígitos."
        }

        val emailLimpio = email.trim()
        if (emailLimpio.isNotEmpty() && !patronCorreo.matches(emailLimpio)) {
            errores["email"] = "Revisa el correo electrónico."
        }
        return errores
    }

    companion object {
        private const val PATRON_FECHA = "dd/MM/yyyy"
        private val patronFecha = Regex("[0-9]{2}/[0-9]{2}/[0-9]{4}")
        // Validación básica de formato, no comprueba que el correo exista.
        private val patronCorreo = Regex("""^[^\s@]+@[^\s@]+\.[^\s@]+$""")
        private fun crearFormatoFecha(): SimpleDateFormat {
            return SimpleDateFormat(PATRON_FECHA, Locale.ROOT).apply { isLenient = false }
        }
        fun fechaActual(): String { return formatearFecha(System.currentTimeMillis()) }
        fun formatearFecha(milisegundos: Long): String {
            return crearFormatoFecha().format(Date(milisegundos))
        }

        // Interpreta una fecha en la zona horaria del dispositivo.
        // Devuelve null si el texto está incompleto o si la fecha o existe, por ejemplo 31/02/2026.
        fun interpretarFecha(texto: String): Date? {
            val valor = texto.trim()
            if (!patronFecha.matches(valor)) { return null }

            val posicion = ParsePosition(0)
            val resultado = crearFormatoFecha().parse(valor, posicion)
            return resultado?.takeIf { posicion.index == valor.length }
        }
    }
}