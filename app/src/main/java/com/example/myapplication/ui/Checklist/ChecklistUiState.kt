package com.example.myapplication.ui.checklist

import com.example.myapplication.data.local.model.EstadoIngreso
import com.example.myapplication.data.local.model.IngresoResumen
import com.example.myapplication.data.repository.ChecklistRepository

// Información que utiliza la pantalla del checklist.
// Las respuestas comienzan en null para distinguir una
// pregunta pendiente de una respuesta explícita "No".
data class ChecklistUiState(
    // Datos del ingreso que se está revisando.
    val ingreso: IngresoResumen? = null,

    // Identificador del checklist guardado.
    // null significa que todavía no existe uno.
    val checklistId: Long? = null,

    // Refrigerante y fugas.
    val faltaGasRefrigerante: Boolean? = null,
    val fugaVisibleMangueras: Boolean? = null,
    val fugasDetectadasLuzUV: Boolean? = null,

    // Compresor y transmisión.
    val compresorEmbragaAlEncender: Boolean? = null,
    val compresorDanadoOAmarrado: Boolean? = null,
    val bandaCompresorDesgastada: Boolean? = null,

    // Ventilación y limpieza.
    val funcionanAbanicosRadiador: Boolean? = null,
    val filtroCabinaSucioUObstruido: Boolean? = null,
    val condensadorObstruido: Boolean? = null,

    // Observaciones adicionales del técnico.
    val comentarios: String = "",

    // Fecha del último guardado, expresada en milisegundos.
    val fechaUltimoGuardado: Long? = null,

    // Carga inicial del ingreso y sus respuestas.
    val cargando: Boolean = true,
    val errorCarga: String? = null,
    val ingresoNoEncontrado: Boolean = false,

    // Estado de la operación de guardado.
    val guardando: Boolean = false,
    val errorGuardado: String? = null,
    val guardadoExitoso: Boolean = false,

    // Permite confirmar la salida cuando hay cambios pendientes.
    val tieneCambiosSinGuardar: Boolean = false,

    // Activa los avisos de preguntas pendientes después
    // de intentar guardar un checklist incompleto.
    val mostrarPreguntasPendientes: Boolean = false
) {

    // Agrupa las respuestas para calcular el avance.
    // No almacena una segunda copia del formulario.
    private val respuestas: List<Boolean?>
        get() = listOf(
            faltaGasRefrigerante,
            fugaVisibleMangueras,
            fugasDetectadasLuzUV,
            compresorEmbragaAlEncender,
            compresorDanadoOAmarrado,
            bandaCompresorDesgastada,
            funcionanAbanicosRadiador,
            filtroCabinaSucioUObstruido,
            condensadorObstruido
        )

    val totalPreguntas: Int
        get() = respuestas.size

    // Tanto "Sí" como "No" cuentan como una respuesta.
    val preguntasRespondidas: Int
        get() = respuestas.count { it != null }

    val preguntasPendientes: Int
        get() = totalPreguntas - preguntasRespondidas

    // Valor entre 0 y 1 para la barra de progreso.
    val progreso: Float
        get() = preguntasRespondidas.toFloat() / totalPreguntas

    val estaCompleto: Boolean
        get() = preguntasPendientes == 0

    // Indica si el expediente se cargó correctamente.
    val formularioDisponible: Boolean
        get() = !cargando &&
                errorCarga == null &&
                !ingresoNoEncontrado &&
                ingreso != null

    val estadoActual: EstadoIngreso?
        get() = ingreso?.let {
            EstadoIngreso.desdeValor(it.estado)
        }

    // Respeta las mismas etapas que valida el repositorio.
    val permiteEdicion: Boolean
        get() = formularioDisponible &&
                estadoActual?.let {
                    ChecklistRepository.puedeEditar(it)
                } == true

    // Bloquea las respuestas durante el guardado.
    val puedeModificar: Boolean
        get() = permiteEdicion &&
                !guardando &&
                !guardadoExitoso

    // El ViewModel comprobará las respuestas pendientes al guardar.
    // Mantener habilitado el botón permite mostrar qué falta.
    val puedeGuardar: Boolean
        get() = puedeModificar &&
                (checklistId == null || tieneCambiosSinGuardar)
}