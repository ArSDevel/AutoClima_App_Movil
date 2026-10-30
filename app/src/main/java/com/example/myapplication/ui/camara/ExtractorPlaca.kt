package com.example.myapplication.ui.camara

import com.example.myapplication.data.local.model.FormularioIngreso
import java.util.Locale

// Busca dentro del texto que lee la cámara el fragmento que tiene forma de placa.
object ExtractorPlaca {

    // Prueba cada palabra y las uniones de dos o tres palabras seguidas, con guion, por ejemplo "ABC 123 D".
    // Conserva solo las que cumplen el patrón de placa y entre ellas prefiere la más larga.
    // Devuelve null si el texto no contiene nada que parezca una placa.
    fun extraer(texto: String): String? {
        val palabras = texto.uppercase(Locale.ROOT)
            .split(Regex("[^A-Z0-9-]+"))
            .map { it.trim('-') }
            .filter { it.isNotEmpty() }
        val validas = mutableListOf<String>()
        for (inicio in palabras.indices) {
            for (cantidad in 1..3) {
                if (inicio + cantidad > palabras.size) break
                val candidata = palabras.subList(inicio, inicio + cantidad).joinToString("-")
                if (FormularioIngreso.placaValida(candidata)) { validas += candidata } } }
        return validas.maxByOrNull { it.count { caracter -> caracter != '-' } } } }

// Confirma una placa solo cuando la cámara la lee igual varias veces seguidas, para ignorar lecturas sueltas con errores.
class EstabilizadorPlaca(private val lecturasNecesarias: Int = 3) {
    private var candidata: String? = null
    private var repeticiones = 0

    // Recibe la placa de un fotograma (o null si no hubo) y devuelve la placa confirmada, o null si todavía no.
    fun registrar(placa: String?): String? {
        if (placa == null) {
            candidata = null
            repeticiones = 0
            return null }
        val actual = candidata
        if (actual != null && FormularioIngreso.mismaPlaca(actual, placa)) {
            repeticiones += 1
        } else {
            candidata = placa
            repeticiones = 1 }
        return if (repeticiones >= lecturasNecesarias) candidata else null } }
