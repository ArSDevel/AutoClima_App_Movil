package com.example.myapplication

import com.example.myapplication.data.respaldo.ExtractorZip
import com.example.myapplication.data.respaldo.ManifiestoRespaldo
import com.example.myapplication.data.respaldo.RespaldoException
import com.example.myapplication.data.respaldo.RutasRespaldo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ManifiestoRespaldoTest {

    private val ejemplo = ManifiestoRespaldo(
        versionApp = "1.0", versionBd = 12, fechaMs = 1_793_000_000_000L,
        ingresos = 24, vehiculos = 18, clientes = 20, usuarios = 3, archivos = 58)

    @Test
    fun aTexto_yDesdeTexto_danElMismoManifiesto() {
        assertEquals(ejemplo, ManifiestoRespaldo.desdeTexto(ejemplo.aTexto())) }

    @Test
    fun textoIncompletoOBasura_noEsUnManifiesto() {
        assertNull(ManifiestoRespaldo.desdeTexto(""))
        assertNull(ManifiestoRespaldo.desdeTexto("hola mundo"))
        assertNull(ManifiestoRespaldo.desdeTexto(ejemplo.aTexto().replace("ingresos=24", "ingresos=veinte")))
        assertNull(ManifiestoRespaldo.desdeTexto(ejemplo.aTexto().lines().filterNot { it.startsWith("fechaMs") }.joinToString("\n"))) } }

class RutasRespaldoTest {

    @get:Rule val carpeta = TemporaryFolder()

    @Test
    fun entradaDe_reconoceFotosYFirmasDeLaCarpetaDeLaApp() {
        val archivos = carpeta.newFolder("files")
        assertEquals("fotos/a.jpg", RutasRespaldo.entradaDe(File(archivos, "a.jpg").path, archivos))
        assertEquals("firmas/f.png", RutasRespaldo.entradaDe(File(File(archivos, "firmas"), "f.png").path, archivos)) }

    @Test
    fun entradaDe_ignoraArchivosDeOtrasCarpetas() {
        val archivos = carpeta.newFolder("files")
        assertNull(RutasRespaldo.entradaDe("/sdcard/DCIM/a.jpg", archivos))
        assertNull(RutasRespaldo.entradaDe(File(File(archivos, "otra"), "a.jpg").path, archivos)) }

    @Test
    fun reubicar_apuntaALaCarpetaDeEsteCelular() {
        val archivos = carpeta.newFolder("files")
        assertEquals(File(archivos, "a.jpg").path, RutasRespaldo.reubicar("/data/user/0/com.example.myapplication/files/a.jpg", archivos))
        assertEquals(File(File(archivos, "firmas"), "f.png").path,
            RutasRespaldo.reubicar("/data/user/10/com.example.myapplication/files/firmas/f.png", archivos))
        assertEquals(File(archivos, "a.jpg").path, RutasRespaldo.reubicar("C:\\datos\\files\\a.jpg", archivos)) }

    @Test
    fun destinoDe_separaFotosYFirmas() {
        val archivos = carpeta.newFolder("files")
        assertEquals(File(archivos, "a.jpg"), RutasRespaldo.destinoDe("fotos/a.jpg", archivos))
        assertEquals(File(File(archivos, "firmas"), "f.png"), RutasRespaldo.destinoDe("firmas/f.png", archivos)) }

    @Test
    fun soloSeAceptanLasEntradasQueUnRespaldoCrea() {
        assertTrue(RutasRespaldo.esEntradaSegura("manifiesto.properties"))
        assertTrue(RutasRespaldo.esEntradaSegura("autoclima_diag.db"))
        assertTrue(RutasRespaldo.esEntradaSegura("fotos/IMG_1.jpg"))
        assertTrue(RutasRespaldo.esEntradaSegura("firmas/firma_9.png"))
        assertFalse(RutasRespaldo.esEntradaSegura("../afuera.txt"))
        assertFalse(RutasRespaldo.esEntradaSegura("fotos/../../afuera.txt"))
        assertFalse(RutasRespaldo.esEntradaSegura("/etc/passwd"))
        assertFalse(RutasRespaldo.esEntradaSegura("fotos/sub/a.jpg"))
        assertFalse(RutasRespaldo.esEntradaSegura("fotos/"))
        assertFalse(RutasRespaldo.esEntradaSegura("otro.txt")) } }

class ExtractorZipTest {

    @get:Rule val carpeta = TemporaryFolder()

    private fun zip(vararg entradas: Pair<String, ByteArray>): ByteArrayInputStream {
        val salida = ByteArrayOutputStream()
        ZipOutputStream(salida).use { z ->
            entradas.forEach { (nombre, datos) -> z.putNextEntry(ZipEntry(nombre)); z.write(datos); z.closeEntry() } }
        return ByteArrayInputStream(salida.toByteArray()) }

    private fun rechazado(bloque: () -> Unit) {
        try { bloque(); fail("Debió lanzar RespaldoException") } catch (e: RespaldoException) { assertNotNull(e.mensaje) } }

    @Test
    fun extraeLasEntradasValidas() {
        val destino = carpeta.newFolder("tmp")
        val nombres = ExtractorZip.extraer(
            zip("manifiesto.properties" to "x".toByteArray(), "autoclima_diag.db" to byteArrayOf(1, 2, 3),
                "fotos/a.jpg" to byteArrayOf(9), "firmas/f.png" to byteArrayOf(8)), destino)
        assertEquals(listOf("manifiesto.properties", "autoclima_diag.db", "fotos/a.jpg", "firmas/f.png"), nombres)
        assertEquals(3, File(destino, "autoclima_diag.db").length())
        assertTrue(File(destino, "fotos/a.jpg").isFile)
        assertTrue(File(destino, "firmas/f.png").isFile) }

    @Test
    fun rechazaEntradasQueSalenDeLaCarpeta() {
        val destino = carpeta.newFolder("tmp")
        rechazado { ExtractorZip.extraer(zip("../evil.txt" to byteArrayOf(1)), destino) }
        rechazado { ExtractorZip.extraer(zip("fotos/../../evil.txt" to byteArrayOf(1)), destino) }
        assertFalse(File(destino.parentFile, "evil.txt").exists()) }

    @Test
    fun rechazaNombresDesconocidos() {
        val destino = carpeta.newFolder("tmp")
        rechazado { ExtractorZip.extraer(zip("script.sh" to byteArrayOf(1)), destino) }
        rechazado { ExtractorZip.extraer(zip("fotos/sub/a.jpg" to byteArrayOf(1)), destino) } }

    @Test
    fun limitaLaCantidadDeEntradas() {
        val destino = carpeta.newFolder("tmp")
        rechazado {
            ExtractorZip.extraer(
                zip("fotos/1.jpg" to byteArrayOf(1), "fotos/2.jpg" to byteArrayOf(1), "fotos/3.jpg" to byteArrayOf(1)),
                destino, maximoEntradas = 2) } }

    @Test
    fun seDetieneSiNoHayEspacioLibre() {
        val destino = carpeta.newFolder("tmp")
        rechazado {
            ExtractorZip.extraer(zip("fotos/grande.jpg" to ByteArray(9 * 1024 * 1024)), destino,
                espacioMinimoLibre = Long.MAX_VALUE) } }

    @Test
    fun archivoQueNoEsZip_noDejaEntradas() {
        val destino = carpeta.newFolder("tmp")
        assertTrue(ExtractorZip.extraer(ByteArrayInputStream("esto no es un zip".toByteArray()), destino).isEmpty()) } }
