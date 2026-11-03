package com.example.myapplication.ui.components

import com.example.myapplication.data.local.model.EstadoIngreso
import com.example.myapplication.data.local.model.IngresoResumen

// Mensaje que ven las pantallas de un ingreso cuando su identificador no es válido.
const val ERROR_INGRESO_INVALIDO = "El identificador del ingreso no es válido."

// Lo que comparten los estados de las pantallas que trabajan sobre un solo ingreso: detalle, checklist, diagnóstico y fotos.
interface EstadoPantallaIngreso {
    // null mientras se carga o si el ingreso no existe.
    val ingreso: IngresoResumen?
    val cargando: Boolean
    val errorCarga: String?
    val ingresoNoEncontrado: Boolean

    // El expediente se cargó sin errores y hay un ingreso para mostrar.
    val ingresoCargado: Boolean
        get() = !cargando && errorCarga == null && !ingresoNoEncontrado && ingreso != null

    // Estado del ingreso mostrado; null mientras todavía no hay un ingreso disponible.
    val estadoActual: EstadoIngreso?
        get() = ingreso?.let { EstadoIngreso.desdeValor(it.estado) } }
