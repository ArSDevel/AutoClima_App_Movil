package com.example.myapplication.ui.registro

import com.example.myapplication.data.local.model.EstadoIngreso
import com.example.myapplication.data.local.model.FormularioIngreso

// Información que utiliza la pantalla de registro y edición.
data class RegistroUiState(
    // Datos del vehículo
    val placa: String = "",
    val modelo: String = "",
    val color: String = "",
    val numeroSerie: String = "",
    // Datos del cliente
    val nombreCliente: String = "",
    val telefonoCliente: String = "",
    val emailCliente: String = "",
    // Datos de recepción
    val fechaEntrada: String = FormularioIngreso.fechaActual(),
    val motivoIngreso: String = "",
    val condicionInicial: String = "",
    // Carga inicial del expediente para edición
    val cargandoIngreso: Boolean = false,
    val errorCarga: String? = null,
    // Estado de la operación de guardado
    val cargando: Boolean = false,
    val mensajeError: String? = null,
    val erroresCampos: Map<String, String> = emptyMap(),
    // Resultado del guardado
    // Conservamos estos nombres para las llamadas existentes.
    val registroExitoso: Boolean = false,
    val idRegistrado: Long? = null,
    // Contexto de edición
    // null significa que se está creando un ingreso nuevo.
    val ingresoIdEnEdicion: Long? = null,
    val estadoIngreso: EstadoIngreso = EstadoIngreso.REGISTRADO,
    // Indica si hay modificaciones pendientes de guardar
    val tieneCambiosSinGuardar: Boolean = false
) {
    val esEdicion: Boolean
        get() = ingresoIdEnEdicion != null
    // Impide editar o guardar mientras se prepara el expediente
    // o cuando su consulta no pudo completarse
    val formularioDisponible: Boolean
        get() = !cargandoIngreso && errorCarga == null

    // Construye los datos que validan y utilizan los repositorios
    // Permite conservar los campos actuales de la pantalla
    // sin duplicar sus valores en otro formulario almacenado
    fun aFormulario(): FormularioIngreso {
        return FormularioIngreso(
            placa = placa,
            modelo = modelo,
            color = color,
            numeroSerie = numeroSerie,
            nombre = nombreCliente,
            telefono = telefonoCliente,
            email = emailCliente,
            fecha = fechaEntrada,
            motivo = motivoIngreso,
            condicion = condicionInicial
        )
    }
}
