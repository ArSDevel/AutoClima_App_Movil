package com.example.myapplication.data.respaldo

import com.example.myapplication.R
import java.io.File
import java.io.InputStream
import java.util.zip.ZipInputStream

// Descomprime un respaldo en una carpeta temporal con las precauciones de un archivo que viene de fuera: solo acepta los
// nombres que un respaldo propio puede tener (evita que una entrada escriba fuera de la carpeta), no admite repetidos,
// limita la cantidad de entradas y se detiene si el teléfono se queda sin espacio.
object ExtractorZip {
    const val MAXIMO_ENTRADAS = 20_000
    const val ESPACIO_MINIMO_LIBRE = 50L * 1024 * 1024
    private const val BLOQUE = 64 * 1024

    // Devuelve los nombres de las entradas extraídas.
    fun extraer(
        entrada: InputStream,
        destino: File,
        espacioMinimoLibre: Long = ESPACIO_MINIMO_LIBRE,
        maximoEntradas: Int = MAXIMO_ENTRADAS): List<String> {
        destino.mkdirs()
        val raiz = destino.canonicalFile
        val extraidas = LinkedHashSet<String>()
        val buffer = ByteArray(BLOQUE)
        try {
            ZipInputStream(entrada).use { zip ->
                while (true) {
                    val actual = zip.nextEntry ?: break
                    if (actual.isDirectory) continue
                    val nombre = actual.name
                    if (!RutasRespaldo.esEntradaSegura(nombre) || !extraidas.add(nombre) || extraidas.size > maximoEntradas) {
                        throw RespaldoException(R.string.respaldo_error_no_valido) }
                    val archivo = File(raiz, nombre).canonicalFile
                    // Segunda comprobación, por si el nombre pasó el filtro pero la ruta real sale de la carpeta.
                    if (archivo.parentFile?.canonicalPath?.startsWith(raiz.path) != true) {
                        throw RespaldoException(R.string.respaldo_error_no_valido) }
                    archivo.parentFile?.mkdirs()
                    archivo.outputStream().use { salida ->
                        var desdeUltimaRevision = 0L
                        while (true) {
                            val leidos = zip.read(buffer)
                            if (leidos < 0) break
                            salida.write(buffer, 0, leidos)
                            desdeUltimaRevision += leidos
                            if (desdeUltimaRevision >= 8L * 1024 * 1024) {
                                desdeUltimaRevision = 0
                                if (raiz.usableSpace < espacioMinimoLibre) throw RespaldoException(R.string.respaldo_error_espacio) } } } } }
        } catch (e: RespaldoException) {
            throw e
        } catch (e: java.io.IOException) {
            // Un archivo cortado o que no es .zip falla al leerse.
            throw RespaldoException(R.string.respaldo_error_no_valido) }
        return extraidas.toList() } }
