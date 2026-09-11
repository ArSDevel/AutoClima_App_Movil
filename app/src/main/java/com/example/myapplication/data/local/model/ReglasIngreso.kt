package com.example.myapplication.data.local.model

/* Reglas del proceso de un ingreso. No depende de Compose ni de los botones de la interfaz.
El repositorio debe comprobar estas reglas antes de guardaruna modificación en la base de datos.*/
object ReglasIngreso {

    // Permite modificar vehículo, cliente y datos de recepción
    fun edicionCompleta(estado: EstadoIngreso): Boolean {
        return estado in setOf(
            EstadoIngreso.REGISTRADO,
            EstadoIngreso.EN_REVISION,
            EstadoIngreso.EN_REPARACION
        )
    }

    // En estas etapas solo se permite corregir teléfono y correo.
    fun soloContacto(estado: EstadoIngreso): Boolean {
        return estado in setOf(
            EstadoIngreso.LISTO_PARA_FIRMA,
            EstadoIngreso.LISTO_PARA_ENTREGA,
            EstadoIngreso.NO_DISPONIBLE
        )
    }

    fun editar(estado: EstadoIngreso): Boolean {
        return edicionCompleta(estado) || soloContacto(estado)
    }

    // La cancelación se permite antes de comenzar la reparación.
    fun cancelar(estado: EstadoIngreso): Boolean {
        return estado in setOf(
            EstadoIngreso.REGISTRADO,
            EstadoIngreso.EN_REVISION
        )
    }

    // Solo se puede eliminar una captura registrada que no tenga actividad posterior a su creación.
    fun eliminar(estado: EstadoIngreso, tieneActividadPosterior: Boolean): Boolean {
        return estado == EstadoIngreso.REGISTRADO && !tieneActividadPosterior
    }

    fun iniciarRevision(estado: EstadoIngreso): Boolean {
        return estado == EstadoIngreso.REGISTRADO
    }

    fun devolverAReparacion(estado: EstadoIngreso): Boolean {
        return estado == EstadoIngreso.LISTO_PARA_FIRMA
    }

    fun regularizarEstado(estado: EstadoIngreso): Boolean {
        return estado == EstadoIngreso.NO_DISPONIBLE
    }

    fun estaCerrado(estado: EstadoIngreso): Boolean {
        return estado in setOf(
            EstadoIngreso.ENTREGADO,
            EstadoIngreso.CANCELADO
        )
    }

    // Transiciones disponibles en esta etapa del proyecto.
    // Los avances que requieren checklist, diagnóstico,
    // firma o validación de entrega se incorporarán cuando
    // sus módulos puedan comprobar esos requisitos.
    fun puedeCambiarEstado(origen: EstadoIngreso, destino: EstadoIngreso): Boolean {
        return when (destino) {
            EstadoIngreso.CANCELADO -> cancelar(origen)
            EstadoIngreso.EN_REVISION -> iniciarRevision(origen) || regularizarEstado(origen)
            EstadoIngreso.REGISTRADO -> regularizarEstado(origen)
            EstadoIngreso.EN_REPARACION -> devolverAReparacion(origen)
            EstadoIngreso.LISTO_PARA_FIRMA,
            EstadoIngreso.LISTO_PARA_ENTREGA,
            EstadoIngreso.ENTREGADO,
            EstadoIngreso.NO_DISPONIBLE -> false
        }
    }

    // Detiene la operación si el cambio no está permitido o no se proporcionó un motivo
    fun validarTransicion(origen: EstadoIngreso, destino: EstadoIngreso, motivo: String) {
        require(puedeCambiarEstado(origen, destino)) {
            "El cambio de estado no está permitido o requiere " +
                    "completar los módulos técnicos correspondientes."
        }
        require(motivo.isNotBlank()) { "Escribe un motivo para registrar el cambio." }
    }
}