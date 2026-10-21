package com.example.myapplication.data.local.model

/* Reglas del proceso de un ingreso. No depende de Compose ni de los botones de la interfaz.
El repositorio debe comprobar estas reglas antes de guardaruna modificación en la base de datos.*/
object ReglasIngreso {

    // Permite modificar vehículo, cliente y datos de recepción
    fun edicionCompleta(estado: EstadoIngreso): Boolean {
        return estado in setOf(
            EstadoIngreso.REGISTRADO,
            EstadoIngreso.EN_REVISION,
            EstadoIngreso.EN_REPARACION) }

    // En estas etapas solo se permite corregir teléfono y correo.
    fun soloContacto(estado: EstadoIngreso): Boolean {
        return estado in setOf(
            EstadoIngreso.LISTO_PARA_FIRMA,
            EstadoIngreso.LISTO_PARA_ENTREGA,
            EstadoIngreso.NO_DISPONIBLE) }

    // Un ingreso se puede editar si permite edición completa o solo de contacto.
    fun editar(estado: EstadoIngreso): Boolean {
        return edicionCompleta(estado) || soloContacto(estado) }

    // La cancelación se permite antes de comenzar la reparación.
    fun cancelar(estado: EstadoIngreso): Boolean {
        return estado in setOf(
            EstadoIngreso.REGISTRADO,
            EstadoIngreso.EN_REVISION) }

    // Solo se puede eliminar una captura registrada que no tenga actividad posterior a su creación.
    fun eliminar(estado: EstadoIngreso, tieneActividadPosterior: Boolean): Boolean {
        return estado == EstadoIngreso.REGISTRADO && !tieneActividadPosterior }

    // Solo un ingreso registrado puede pasar a revisión.
    fun iniciarRevision(estado: EstadoIngreso): Boolean {
        return estado == EstadoIngreso.REGISTRADO }

    // Avanza de la revisión a la reparación. Requiere el checklist guardado.
    fun iniciarReparacion(estado: EstadoIngreso): Boolean {
        return estado == EstadoIngreso.EN_REVISION }

    // Termina la reparación y pasa a firma. Requiere el diagnóstico guardado.
    fun enviarAFirma(estado: EstadoIngreso): Boolean {
        return estado == EstadoIngreso.EN_REPARACION }

    // Desde listo para firma se puede regresar a reparación.
    fun devolverAReparacion(estado: EstadoIngreso): Boolean {
        return estado == EstadoIngreso.LISTO_PARA_FIRMA }

    // La firma del cliente se registra cuando el ingreso está listo para firma.
    fun firmar(estado: EstadoIngreso): Boolean {
        return estado == EstadoIngreso.LISTO_PARA_FIRMA }

    // La entrega se confirma cuando el ingreso ya está firmado.
    fun entregar(estado: EstadoIngreso): Boolean {
        return estado == EstadoIngreso.LISTO_PARA_ENTREGA }

    // Un ingreso con estado desconocido se puede regularizar.
    fun regularizarEstado(estado: EstadoIngreso): Boolean {
        return estado == EstadoIngreso.NO_DISPONIBLE }

    // Un ingreso entregado o cancelado ya no admite cambios.
    fun estaCerrado(estado: EstadoIngreso): Boolean {
        return estado in setOf(
            EstadoIngreso.ENTREGADO,
            EstadoIngreso.CANCELADO) }

    // Cambios de estado que se confirman con un motivo.
    // Pasar a listo para entrega y a entregado exige además la firma guardada,
    // que el repositorio comprueba antes de cambiar el estado.
    fun puedeCambiarEstado(origen: EstadoIngreso, destino: EstadoIngreso): Boolean {
        return when (destino) {
            EstadoIngreso.CANCELADO -> cancelar(origen)
            EstadoIngreso.EN_REVISION -> iniciarRevision(origen) || regularizarEstado(origen)
            EstadoIngreso.REGISTRADO -> regularizarEstado(origen)
            EstadoIngreso.EN_REPARACION -> devolverAReparacion(origen) || iniciarReparacion(origen)
            EstadoIngreso.LISTO_PARA_FIRMA -> enviarAFirma(origen)
            EstadoIngreso.LISTO_PARA_ENTREGA -> firmar(origen)
            EstadoIngreso.ENTREGADO -> entregar(origen)
            EstadoIngreso.NO_DISPONIBLE -> false } }

    // Detiene la operación si el cambio no está permitido o no se proporcionó un motivo
    fun validarTransicion(origen: EstadoIngreso, destino: EstadoIngreso, motivo: String) {
        require(puedeCambiarEstado(origen, destino)) {
            "El cambio de estado no está permitido o requiere " +
                    "completar el paso anterior." }
        require(motivo.isNotBlank()) { "Escribe un motivo para registrar el cambio." } } }