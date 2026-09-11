package com.example.myapplication.ui.dashboard

/* Acciones que la interfaz puede ofrecer sobre un ingreso.
Declarar una acción no significa que esté disponible
para todos los estados ni que su función esté implementada.*/
enum class IngresoAccion {
    // Consulta del expediente
    VER_PDF,
    // Trabajo técnico
    INICIAR_REVISION,
    CHECKLIST,
    EVIDENCIAS,
    DIAGNOSTICO,
    // Firma y cierre
    FIRMAR,
    DEVOLVER_A_REPARACION,
    ENTREGAR,
    // Administración del ingreso
    EDITAR,
    CANCELAR,
    ELIMINAR,
    // Corrección de registros con estado desconocido
    REGULARIZAR_ESTADO
}