package com.example.myapplication.data.reporte

import java.io.File

// Lo que se entrega al servicio que envía el reporte.
data class SolicitudEnvio(
    val telefono: String,
    val mensaje: String,
    val pdf: File?,
    val fotos: List<File>
)

sealed interface ResultadoEnvio {
    // El reporte quedó listo en el destino. No confirma que el cliente lo haya recibido.
    data object Listo : ResultadoEnvio
    data class Error(val mensaje: String) : ResultadoEnvio
}

// Punto de unión para enviar reportes. La pantalla solo conoce esta interfaz:
// para usar una API de mensajería se crea otra implementación y se cambia en AutoClimaApp.
interface EnvioReporte {
    suspend fun enviar(solicitud: SolicitudEnvio): ResultadoEnvio
}
