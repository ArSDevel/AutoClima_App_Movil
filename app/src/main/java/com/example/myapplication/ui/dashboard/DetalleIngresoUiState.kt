package com.example.myapplication.ui.dashboard

import com.example.myapplication.data.local.entity.EventoIngreso
import com.example.myapplication.data.local.model.EstadoIngreso
import com.example.myapplication.data.local.model.IngresoResumen

// Estado de la pantalla de detalle de un ingreso.
// Contiene los datos que muestra la interfaz y el estado
// de las operaciones. No realiza consultas ni guarda cambios.
data class DetalleIngresoUiState(
    // Carga inicial del expediente y su historial
    val cargando: Boolean = true,
    // null mientras se carga o si el ingreso no existe
    val ingreso: IngresoResumen? = null,
    // Eventos asociados al ingreso.
    val historial: List<EventoIngreso> = emptyList(),
    // Diferencia un registro inexistente de un error de consulta
    val ingresoNoEncontrado: Boolean = false,
    val errorCarga: String? = null,
    // Acción que se está ejecutando.
    val accionEnCurso: IngresoAccion? = null,
    // Error de una operación, conservando el expediente visible
    val errorOperacion: String? = null,
    // Permite cerrar la confirmación después de una operación exitosa
    val operacionExitosa: Boolean = false,
    // Permite regresar al dashboard después de eliminar
    val eliminado: Boolean = false
) {

    // Indica si hay una operación en curso.
    // La pantalla lo utilizará para evitar pulsaciones repetidas
    val procesando: Boolean
        get() = accionEnCurso != null

    //Estado del ingreso mostrado.
    // null significa que todavía no hay un ingreso disponible;
    // NO_DISPONIBLE corresponde a un ingreso con estado desconocido.
    val estadoActual: EstadoIngreso?
        get() = ingreso?.let {
            EstadoIngreso.desdeValor(it.estado)
        }

    // Identifica actividad posterior a la creación.
    // Solo debe utilizarse para presentar acciones cuando
    // la carga completa haya terminado correctamente.
    val tieneActividadPosterior: Boolean
        get() = historial.any { evento ->
            evento.accion != "CREADO"
        }
}