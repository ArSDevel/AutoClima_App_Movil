package com.example.myapplication.ui.diagnostico

import com.example.myapplication.data.local.entity.ChecklistDiagnostico
import com.example.myapplication.data.local.model.ConceptoCotizacionFormulario
import com.example.myapplication.data.local.model.EstadoIngreso
import com.example.myapplication.data.local.model.FormularioDiagnostico
import com.example.myapplication.data.local.model.IngresoResumen
import com.example.myapplication.data.local.repository.DiagnosticoRepository

// Identifica los hallazgos sin guardar textos de interfaz.
// La pantalla utilizará strings.xml para mostrar cada descripción.
enum class HallazgoDiagnostico {
    FALTA_REFRIGERANTE,
    FUGA_VISIBLE_MANGUERAS,
    FUGA_DETECTADA_UV,
    COMPRESOR_NO_EMBRAGA,
    COMPRESOR_DANADO,
    BANDA_DESGASTADA,
    VENTILADORES_NO_FUNCIONAN,
    FILTRO_OBSTRUIDO,
    CONDENSADOR_OBSTRUIDO
}

// Información utilizada por la pantalla del diagnóstico.
// No realiza consultas ni guarda cambios en la base de datos.
data class DiagnosticoUiState(
    // Datos de recepción, vehículo y cliente.
    val ingreso: IngresoResumen? = null,

    // Respuestas y comentarios guardados en el checklist.
    // null significa que todavía no se dispone de uno.
    val checklist: ChecklistDiagnostico? = null,

    // Identificador del diagnóstico guardado.
    val diagnosticoId: Long? = null,

    // Notas adicionales y conceptos capturados por el mecánico.
    // No reemplazan los comentarios originales del checklist.
    val formulario: FormularioDiagnostico = FormularioDiagnostico(),

    // Información del último guardado.
    val fechaUltimoGuardado: Long? = null,
    val tecnicoUltimoGuardadoId: Long? = null,

    // Carga inicial.
    val cargando: Boolean = true,
    val errorCarga: String? = null,
    val ingresoNoEncontrado: Boolean = false,

    // Guardado y validación.
    val guardando: Boolean = false,
    val errorGuardado: String? = null,
    val erroresCampos: Map<String, String> = emptyMap(),
    val guardadoExitoso: Boolean = false,

    // Permite advertir antes de abandonar el formulario.
    val tieneCambiosSinGuardar: Boolean = false
) {

    // Accesos a los campos del formulario sin duplicar sus valores.
    val observaciones: String
        get() = formulario.observaciones

    val conceptos: List<ConceptoCotizacionFormulario>
        get() = formulario.conceptos

    val estadoActual: EstadoIngreso?
        get() = ingreso?.let {
            EstadoIngreso.desdeValor(it.estado)
        }

    // El reporte puede mostrarse aunque aún no exista un checklist.
    // En ese caso se indicará que la revisión está pendiente.
    val reporteDisponible: Boolean
        get() = !cargando &&
                errorCarga == null &&
                !ingresoNoEncontrado &&
                ingreso != null

    val tieneChecklist: Boolean
        get() = checklist != null

    // La edición requiere una etapa permitida y un checklist guardado.
    val permiteEdicion: Boolean
        get() = reporteDisponible &&
                tieneChecklist &&
                estadoActual?.let {
                    DiagnosticoRepository.puedeEditar(it)
                } == true

    // Evita cambios durante el guardado y mientras la pantalla
    // termina de presentar la confirmación de éxito.
    val puedeModificar: Boolean
        get() = permiteEdicion &&
                !guardando &&
                !guardadoExitoso

    // El botón permite intentar guardar para señalar los campos
    // inválidos. El ViewModel y el repositorio harán la validación.
    val puedeGuardar: Boolean
        get() = puedeModificar &&
                (diagnosticoId == null || tieneCambiosSinGuardar)

    // Sin conceptos, la cotización está pendiente.
    // Una cotización con conceptos de importe cero sí existe.
    val cotizacionPendiente: Boolean
        get() = !formulario.tieneCotizacion

    // null significa que algún importe no es válido
    // o que la suma supera el rango permitido.
    val totalCentavos: Long?
        get() = formulario.totalEnCentavos()

    // Conserva por separado los comentarios de la revisión.
    val comentariosChecklist: String
        get() = checklist?.comentarios.orEmpty()

    // Traduce las respuestas a hallazgos.
    // Algunas preguntas describen una falla cuando se responde Sí;
    // otras describen una falla cuando se responde No.
    val hallazgos: List<HallazgoDiagnostico>
        get() {
            val respuestas = checklist ?: return emptyList()

            return buildList {
                if (respuestas.faltaGasRefrigerante) {
                    add(HallazgoDiagnostico.FALTA_REFRIGERANTE)
                }

                if (respuestas.fugaVisibleMangueras) {
                    add(HallazgoDiagnostico.FUGA_VISIBLE_MANGUERAS)
                }

                if (respuestas.fugasDetectadasLuzUV) {
                    add(HallazgoDiagnostico.FUGA_DETECTADA_UV)
                }

                if (!respuestas.compresorEmbragaAlEncender) {
                    add(HallazgoDiagnostico.COMPRESOR_NO_EMBRAGA)
                }

                if (respuestas.compresorDanadoOAmarrado) {
                    add(HallazgoDiagnostico.COMPRESOR_DANADO)
                }

                if (respuestas.bandaCompresorDesgastada) {
                    add(HallazgoDiagnostico.BANDA_DESGASTADA)
                }

                if (!respuestas.funcionanAbanicosRadiador) {
                    add(HallazgoDiagnostico.VENTILADORES_NO_FUNCIONAN)
                }

                if (respuestas.filtroCabinaSucioUObstruido) {
                    add(HallazgoDiagnostico.FILTRO_OBSTRUIDO)
                }

                if (respuestas.condensadorObstruido) {
                    add(HallazgoDiagnostico.CONDENSADOR_OBSTRUIDO)
                }
            }
        }
}