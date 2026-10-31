package com.example.myapplication.data.respaldo

import java.io.File

// Reglas de nombres y rutas del respaldo. La base de datos guarda cada foto y firma con su ruta completa en el
// celular; dentro del .zip se guardan con un nombre relativo (fotos/x.jpg, firmas/y.png), y al restaurar se vuelven a
// apuntar a la carpeta de la app, que puede ser distinta en otro celular o usuario de Android.
object RutasRespaldo {
    const val ARCHIVO_BD = "autoclima_diag.db"
    const val CARPETA_FOTOS = "fotos"
    const val CARPETA_FIRMAS = "firmas"

    // Nombre que lleva en el .zip un archivo guardado en la carpeta de la app; null si está en otro lugar.
    fun entradaDe(ruta: String, directorioArchivos: File): String? {
        val archivo = File(ruta)
        val padre = archivo.parentFile ?: return null
        val base = directorioArchivos.canonicalFile
        return when {
            padre.canonicalFile == base -> "$CARPETA_FOTOS/${archivo.name}"
            padre.name == CARPETA_FIRMAS && padre.parentFile?.canonicalFile == base ->
                "$CARPETA_FIRMAS/${archivo.name}"
            else -> null } }

    // Dónde queda un archivo del .zip dentro de la carpeta de la app.
    fun destinoDe(entrada: String, directorioArchivos: File): File {
        val nombre = entrada.substringAfterLast('/')
        return if (entrada.substringBefore('/') == CARPETA_FIRMAS) File(File(directorioArchivos, CARPETA_FIRMAS), nombre)
        else File(directorioArchivos, nombre) }

    // Convierte una ruta guardada en la base (de cualquier celular) en la ruta equivalente en este.
    fun reubicar(rutaGuardada: String, directorioArchivos: File): String {
        val partes = rutaGuardada.replace('\\', '/').split('/')
        val nombre = partes.last()
        val enFirmas = partes.dropLast(1).lastOrNull() == CARPETA_FIRMAS
        return (if (enFirmas) File(File(directorioArchivos, CARPETA_FIRMAS), nombre) else File(directorioArchivos, nombre)).path }

    // Nombre que tiene en el .zip un archivo cuya ruta quedó guardada en la base de datos de otro celular.
    fun entradaDeRutaGuardada(rutaGuardada: String): String {
        val partes = rutaGuardada.replace('\\', '/').split('/')
        val nombre = partes.last()
        return if (partes.dropLast(1).lastOrNull() == CARPETA_FIRMAS) "$CARPETA_FIRMAS/$nombre" else "$CARPETA_FOTOS/$nombre" }

    // Solo se aceptan las entradas que el propio respaldo crea; cualquier otra se rechaza.
    fun esEntradaSegura(nombre: String): Boolean {
        if (nombre == ManifiestoRespaldo.NOMBRE_ENTRADA || nombre == ARCHIVO_BD) return true
        val partes = nombre.split('/')
        return partes.size == 2 && (partes[0] == CARPETA_FOTOS || partes[0] == CARPETA_FIRMAS) &&
            esNombreArchivoSeguro(partes[1]) }

    fun esNombreArchivoSeguro(nombre: String): Boolean =
        nombre.isNotEmpty() && nombre != "." && nombre != ".." && nombre.none { it == '/' || it == '\\' || it.isISOControl() } }
