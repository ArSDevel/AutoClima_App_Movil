package com.example.myapplication.data.reporte

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider

// Abre WhatsApp con los archivos y el mensaje listos; el técnico elige el chat del cliente y pulsa enviar.
// No se indica el número en el envío de archivos porque WhatsApp ignora el destino cuando hay adjuntos.
// Para un cliente que no está en los contactos, abrirChat crea primero su conversación.
// No usa servidores propios.
class EnvioPorWhatsApp(
    private val contexto: Context,
    private val actividadActual: () -> Activity? = { null }
) : EnvioReporte {

    override suspend fun enviar(solicitud: SolicitudEnvio): ResultadoEnvio {
        val archivos = listOfNotNull(solicitud.pdf) + solicitud.fotos
        if (archivos.isEmpty()) return ResultadoEnvio.Error("No hay nada que enviar.")

        val autoridad = "${contexto.packageName}.archivos"
        val uris = try {
            archivos.map { FileProvider.getUriForFile(contexto, autoridad, it) }
        } catch (_: IllegalArgumentException) {
            return ResultadoEnvio.Error("No se pudieron preparar los archivos.") }

        // WhatsApp normal, WhatsApp Business y, si no hay ninguno, el selector del sistema.
        for (paquete in listOf("com.whatsapp", "com.whatsapp.w4b", null)) {
            val intento = crearIntento(solicitud, uris, paquete)
            try {
                lanzar(if (paquete == null) Intent.createChooser(intento, null) else intento)
                return ResultadoEnvio.Listo
            } catch (_: ActivityNotFoundException) {
                continue } }
        return ResultadoEnvio.Error("No se encontró WhatsApp en este teléfono.") }

    // Abre la conversación con el número, aunque no esté en los contactos.
    override suspend fun abrirChat(telefono: String, mensaje: String): ResultadoEnvio {
        val direccion = Uri.parse(if (mensaje.isBlank()) "https://wa.me/$telefono"
            else "https://wa.me/$telefono?text=${Uri.encode(mensaje)}")
        for (paquete in listOf("com.whatsapp", "com.whatsapp.w4b", null)) {
            val intento = Intent(Intent.ACTION_VIEW, direccion)
            if (paquete != null) intento.setPackage(paquete)
            try {
                lanzar(intento)
                return ResultadoEnvio.Listo
            } catch (_: ActivityNotFoundException) {
                continue } }
        return ResultadoEnvio.Error("No se encontró WhatsApp en este teléfono.") }

    // Un archivo usa ACTION_SEND; varios usan ACTION_SEND_MULTIPLE.
    private fun crearIntento(solicitud: SolicitudEnvio, uris: List<Uri>, paquete: String?): Intent {
        val intento = if (uris.size == 1) {
            Intent(Intent.ACTION_SEND).apply {
                type = tipoUnico(solicitud)
                putExtra(Intent.EXTRA_STREAM, uris.first()) }
        } else {
            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                // Varias fotos se mandan como imágenes; WhatsApp no acepta tipos mezclados.
                type = if (solicitud.pdf == null) "image/*" else "*/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris)) } }
        return intento.apply {
            if (solicitud.mensaje.isNotBlank()) putExtra(Intent.EXTRA_TEXT, solicitud.mensaje)
            // Los permisos de lectura viajan con ClipData para que WhatsApp pueda abrir cada archivo.
            clipData = ClipData.newRawUri(null, uris.first()).also { clip ->
                uris.drop(1).forEach { clip.addItem(ClipData.Item(it)) } }
            if (paquete != null) setPackage(paquete)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) } }

    // Desde la pantalla visible, WhatsApp queda en la misma tarea y atrás regresa a la app.
    // Sin pantalla visible se abre como tarea nueva.
    private fun lanzar(intento: Intent) {
        val actividad = actividadActual()
        if (actividad != null) {
            actividad.startActivity(intento)
        } else {
            contexto.startActivity(intento.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } }

    // Tipo del único archivo: el PDF del reporte o una fotografía.
    private fun tipoUnico(solicitud: SolicitudEnvio): String {
        return if (solicitud.pdf != null) "application/pdf" else "image/jpeg" } }
