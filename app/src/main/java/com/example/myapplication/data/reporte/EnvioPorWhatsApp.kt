package com.example.myapplication.data.reporte

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider

// Abre WhatsApp con el chat del cliente y los archivos adjuntos listos para enviar.
// No usa servidores propios: el técnico revisa y pulsa enviar dentro de WhatsApp.
class EnvioPorWhatsApp(private val contexto: Context) : EnvioReporte {

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
                contexto.startActivity(
                    if (paquete == null) Intent.createChooser(intento, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    else intento)
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
                type = "*/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris)) } }
        return intento.apply {
            putExtra(Intent.EXTRA_TEXT, solicitud.mensaje)
            // Abre directamente el chat de este número.
            putExtra("jid", "${solicitud.telefono}@s.whatsapp.net")
            if (paquete != null) setPackage(paquete)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK) } }

    // Tipo del único archivo: el PDF del reporte o una fotografía.
    private fun tipoUnico(solicitud: SolicitudEnvio): String {
        return if (solicitud.pdf != null) "application/pdf" else "image/jpeg" } }
