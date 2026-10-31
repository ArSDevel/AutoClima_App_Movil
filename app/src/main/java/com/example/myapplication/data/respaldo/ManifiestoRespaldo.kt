package com.example.myapplication.data.respaldo

import java.io.StringReader
import java.util.Properties

// Datos que acompañan al respaldo dentro del .zip: sirven para reconocerlo, validarlo y resumirlo al usuario antes de
// restaurarlo. Se guarda como texto "clave=valor".
data class ManifiestoRespaldo(
    val formato: Int = FORMATO_ACTUAL,
    val versionApp: String,
    val versionBd: Int,
    val fechaMs: Long,
    val ingresos: Int,
    val vehiculos: Int,
    val clientes: Int,
    val usuarios: Int,
    val archivos: Int) {

    fun aTexto(): String = listOf(
        "formato=$formato", "versionApp=$versionApp", "versionBd=$versionBd", "fechaMs=$fechaMs",
        "ingresos=$ingresos", "vehiculos=$vehiculos", "clientes=$clientes", "usuarios=$usuarios",
        "archivos=$archivos").joinToString("\n")

    companion object {
        const val FORMATO_ACTUAL = 1
        const val NOMBRE_ENTRADA = "manifiesto.properties"

        // Devuelve null si el texto no es un manifiesto completo y legible.
        fun desdeTexto(texto: String): ManifiestoRespaldo? {
            val propiedades = Properties()
            try { propiedades.load(StringReader(texto)) } catch (_: Exception) { return null }
            fun entero(clave: String): Int? = propiedades.getProperty(clave)?.trim()?.toIntOrNull()
            return ManifiestoRespaldo(
                formato = entero("formato") ?: return null,
                versionApp = propiedades.getProperty("versionApp")?.trim().orEmpty(),
                versionBd = entero("versionBd") ?: return null,
                fechaMs = propiedades.getProperty("fechaMs")?.trim()?.toLongOrNull() ?: return null,
                ingresos = entero("ingresos") ?: return null,
                vehiculos = entero("vehiculos") ?: return null,
                clientes = entero("clientes") ?: return null,
                usuarios = entero("usuarios") ?: return null,
                archivos = entero("archivos") ?: return null) } } }
