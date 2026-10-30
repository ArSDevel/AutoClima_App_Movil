package com.example.myapplication.data.local.model

import java.text.ParsePosition
import java.text.SimpleDateFormat
import com.example.myapplication.data.reporte.FormatoReporte
import java.util.Calendar
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
    val condicion: String = "") {
    // Devuelve los errores identificados por nombre de campo.
    // En edición de contacto únicamente valida teléfono y correo.
    // ahora es el instante de referencia para comprobar que la fecha de entrada no sea futura.
    fun errores(soloContacto: Boolean = false, ahora: Long = System.currentTimeMillis()): Map<String, String> {
        val errores = mutableMapOf<String, String>()
        if (!soloContacto) {
            if (placa.isBlank()) { errores["placa"] = "Escribe la placa." }
            else if (!placaValida(placa)) { errores["placa"] = MENSAJE_PLACA }

            val modeloLimpio = modelo.trim()
            if (modeloLimpio.isEmpty()) { errores["modelo"] = "Escribe el modelo." }
            else if (modeloLimpio.length > MAXIMO_MODELO) { errores["modelo"] = "El modelo admite hasta $MAXIMO_MODELO caracteres." }

            if (color.trim().length > MAXIMO_COLOR) { errores["color"] = "El color admite hasta $MAXIMO_COLOR caracteres." }

            val serieLimpia = numeroSerie.trim()
            if (serieLimpia.isNotEmpty() && !serieValida(serieLimpia)) { errores["numeroSerie"] = MENSAJE_SERIE }

            val nombreLimpio = nombre.trim()
            if (nombreLimpio.isEmpty()) { errores["nombre"] = "Escribe el nombre del cliente." }
            else if (nombreLimpio.length < 2 || nombreLimpio.none { it.isLetter() }) {
                errores["nombre"] = "Escribe un nombre válido, con letras." }
            else if (nombreLimpio.length > MAXIMO_NOMBRE) { errores["nombre"] = "El nombre admite hasta $MAXIMO_NOMBRE caracteres." }

            val fechaEntrada = interpretarFecha(fecha)
            if (fechaEntrada == null) { errores["fecha"] = "Escribe una fecha válida con formato dd/MM/aaaa." }
            else if (esPosteriorAHoy(fechaEntrada, ahora)) { errores["fecha"] = "La fecha de entrada no puede ser posterior a hoy." }

            val motivoLimpio = motivo.trim()
            if (motivoLimpio.isEmpty()) { errores["motivo"] = "Escribe el motivo del ingreso." }
            else if (motivoLimpio.length > MAXIMO_MOTIVO) { errores["motivo"] = "El motivo admite hasta $MAXIMO_MOTIVO caracteres." }

            if (condicion.trim().length > MAXIMO_CONDICION) {
                errores["condicion"] = "La condición admite hasta $MAXIMO_CONDICION caracteres." } }

        errorTelefono(telefono)?.let { errores["telefono"] = it }

        val emailLimpio = email.trim()
        if (emailLimpio.isNotEmpty() && (!patronCorreo.matches(emailLimpio) || emailLimpio.length > MAXIMO_EMAIL)) {
            errores["email"] = "Revisa el correo electrónico." }
        return errores }

    companion object {
        // Longitudes máximas de cada campo, aplicadas en el formulario y de nuevo en el repositorio.
        const val MAXIMO_NOMBRE = 80
        const val MAXIMO_MODELO = 60
        const val MAXIMO_COLOR = 30
        const val MAXIMO_MOTIVO = 200
        const val MAXIMO_CONDICION = 300
        const val MAXIMO_EMAIL = 100
        const val MAXIMO_TELEFONO = 20
        const val LONGITUD_SERIE = 17
        const val MENSAJE_SERIE = "La serie tiene 17 letras y números (sin I, O ni Q)."
        private val patronSerie = Regex("^[A-HJ-NPR-Z0-9]{17}$")

        // Número de identificación vehicular: 17 letras y números, sin I, O ni Q.
        fun serieValida(valor: String): Boolean = patronSerie.matches(valor.trim().uppercase(Locale.ROOT))

        // Pasa a mayúsculas y quita lo que una serie no admite mientras se escribe.
        fun normalizarSerie(valor: String): String {
            return valor.uppercase(Locale.ROOT).filter { it in 'A'..'Z' || it in '0'..'9' }.take(LONGITUD_SERIE) }

        // Explica qué le falta al teléfono; null si sirve para llamar y para enviar por WhatsApp.
        fun errorTelefono(texto: String): String? {
            val limpio = texto.trim()
            if (limpio.isEmpty()) return "Escribe el teléfono del cliente."
            val caracteresValidos = limpio.all { it in '0'..'9' || it in "+ -()" }
            val signoMasValido = limpio.count { it == '+' } <= 1 && ('+' !in limpio || limpio.startsWith("+"))
            if (!caracteresValidos || !signoMasValido) return "Usa solo números, espacios, guiones o un + al inicio."
            if (FormatoReporte.telefonoWhatsApp(limpio) != null) return null
            val digitos = limpio.count { it in '0'..'9' }
            return if (!limpio.startsWith("+") && digitos < 10) "Faltan dígitos: son 10."
            else "Revisa el teléfono: son 10 dígitos, o con lada de país (+52...)." }

        // Una fecha es futura si cae después del final de hoy, en la zona horaria del dispositivo.
        private fun esPosteriorAHoy(fecha: Date, ahora: Long): Boolean {
            val inicioManana = Calendar.getInstance().apply {
                timeInMillis = ahora
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                add(Calendar.DAY_OF_MONTH, 1) }
            return fecha.time >= inicioManana.timeInMillis }

        const val MENSAJE_PLACA = "Revisa la placa. Ejemplos: ABC-123-D, A12-BCD, 123-ABC."
        private val patronPlaca = Regex("^[A-Z0-9]+(-[A-Z0-9]+){0,2}$")

        // Comprueba el patrón de una placa: letras y números, con guiones opcionales
        // (por ejemplo ABC-123-D, A12-BCD, 123-ABC o ABC-12-34).
        // Debe tener entre 5 y 7 caracteres, con al menos una letra y un número.
        fun placaValida(valor: String): Boolean {
            val texto = valor.trim().uppercase(Locale.ROOT)
            if (!patronPlaca.matches(texto)) { return false }
            val compacta = texto.filter { it != '-' }
            return compacta.length in 5..7 &&
                    compacta.any { it in 'A'..'Z' } && compacta.any { it in '0'..'9' } }

        // Pasa a mayúsculas y quita lo que una placa no admite mientras se escribe.
        fun normalizarPlaca(valor: String): String {
            return valor.uppercase(Locale.ROOT).filter { it in 'A'..'Z' || it in '0'..'9' || it == '-' }.take(9) }

        private const val PATRON_FECHA = "dd/MM/yyyy"
        // Exige dos dígitos de día y mes y cuatro de año antes de interpretar la fecha.
        private val patronFecha = Regex("[0-9]{2}/[0-9]{2}/[0-9]{4}")
        // Validación básica de formato, no comprueba que el correo exista.
        private val patronCorreo = Regex("""^[^\s@]+@[^\s@]+\.[^\s@]+$""")
        // SimpleDateFormat no es seguro entre hilos, por eso se crea uno nuevo en cada uso; isLenient en false
        // rechaza fechas inexistentes.
        private fun crearFormatoFecha(): SimpleDateFormat {
            return SimpleDateFormat(PATRON_FECHA, Locale.ROOT).apply { isLenient = false } }
        fun fechaActual(): String { return formatearFecha(System.currentTimeMillis()) }
        fun formatearFecha(milisegundos: Long): String {
            return crearFormatoFecha().format(Date(milisegundos)) }

        // Interpreta una fecha en la zona horaria del dispositivo.
        // Devuelve null si el texto está incompleto o si la fecha no existe, por ejemplo 31/02/2026.
        fun interpretarFecha(texto: String): Date? {
            val valor = texto.trim()
            if (!patronFecha.matches(valor)) { return null }

            val posicion = ParsePosition(0)
            val resultado = crearFormatoFecha().parse(valor, posicion)
            return resultado?.takeIf { posicion.index == valor.length } } } }