package com.example.myapplication.data.reporte

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

// Formatos de texto del reporte y de los envíos.
object FormatoReporte {

    // Centavos como pesos mexicanos, sin pasar por Double.
    fun pesos(centavos: Long): String {
        val formato = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-MX")).apply {
            currency = Currency.getInstance("MXN")
            minimumFractionDigits = 2
            maximumFractionDigits = 2 }
        return formato.format(BigDecimal.valueOf(centavos, 2)) }

    // Número de WhatsApp con lada de país y solo dígitos.
    // Acepta 10 dígitos de México, con 52 o con 521 delante, o cualquier número con + y lada de país;
    // devuelve null si no es válido.
    fun telefonoWhatsApp(texto: String): String? {
        val digitos = texto.filter { it.isDigit() }
        val conLada = texto.trim().startsWith("+")
        return when {
            digitos.length == 10 -> "52$digitos"
            digitos.length == 12 && digitos.startsWith("52") -> digitos
            digitos.length == 13 && digitos.startsWith("521") -> "52" + digitos.takeLast(10)
            conLada && digitos.length in 11..15 && !digitos.startsWith("0") -> digitos
            else -> null } } }
