package com.example.myapplication.ui.dashboard

import com.example.myapplication.ui.components.EstadoPantallaIngreso
import com.example.myapplication.data.local.entity.EventoIngreso
import com.example.myapplication.data.local.model.IngresoResumen

// Estado de la pantalla de detalle de un ingreso.
// Contiene los datos que muestra la interfaz y el estado
// de las operaciones. No realiza consultas ni guarda cambios.
data class DetalleIngresoUiState(
    // Carga inicial del expediente y su historial.
    override val cargando: Boolean = true,
    // null mientras se carga o si el ingreso no existe.
    override val ingreso: IngresoResumen? = null,
    // Eventos asociados al ingreso.
    val historial: List<EventoIngreso> = emptyList(),
    // Nombre de cada técnico del historial; si falta uno, la pantalla muestra su número.
    val nombresTecnicos: Map<Long, String> = emptyMap(),
    // Diferencia un registro inexistente de un error de consulta.
    override val ingresoNoEncontrado: Boolean = false,
    override val errorCarga: String? = null,
    // Acción que se está ejecutando.
    val accionEnCurso: IngresoAccion? = null,
    // Error de una operación, conservando el expediente visible.
    val errorOperacion: String? = null,
    // Permite cerrar la confirmación después de una operación exitosa.
    val operacionExitosa: Boolean = false,
    // Permite regresar al dashboard después de eliminar.
    val eliminado: Boolean = false) : EstadoPantallaIngreso {

    // Indica si hay una operación en curso.
    // La pantalla lo usa para evitar pulsaciones repetidas.
    val procesando: Boolean
        get() = accionEnCurso != null


    // Identifica actividad posterior a la creación.
    // Solo debe utilizarse para presentar acciones cuando
    // la carga completa haya terminado correctamente.
    val tieneActividadPosterior: Boolean
        get() = historial.any { evento ->
            evento.accion != "CREADO" } }