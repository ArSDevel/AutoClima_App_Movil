package com.example.myapplication.ui.dashboard

/* Acciones que la interfaz puede ofrecer sobre un ingreso.
Declarar una acción no significa que esté disponible
para todos los estados ni que su función esté implementada.*/
enum class IngresoAccion {
    // Trabajo técnico
    INICIAR_REVISION,
    INICIAR_REPARACION,
    ENVIAR_A_FIRMA,
    // Firma y cierre
    FIRMAR,
    DEVOLVER_A_REPARACION,
    ENTREGAR,
    // Administración del ingreso
    CANCELAR,
    ELIMINAR,
    // Corrección de registros con estado desconocido
    REGULARIZAR_ESTADO }