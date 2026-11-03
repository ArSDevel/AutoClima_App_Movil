package com.example.myapplication.ui.components

import com.example.myapplication.data.local.model.EstadoIngreso
import com.example.myapplication.data.local.model.IngresoResumen

// Ingreso de ejemplo que comparten las vistas previas de Android Studio. La app no lo usa.
// Cada vista previa cambia solo lo que necesita mostrar.
internal fun ingresoDeEjemplo(
    ingresoId: Long = 1L,
    placa: String = "ABC-123",
    modelo: String = "Honda Civic",
    color: String? = "Rojo",
    numeroSerie: String? = "SERIE-DE-EJEMPLO",
    nombreCliente: String = "Cliente de ejemplo",
    telefonoCliente: String = "8100000000",
    emailCliente: String? = null,
    estado: EstadoIngreso = EstadoIngreso.REGISTRADO,
    motivoIngreso: String? = null,
    condicionInicial: String? = null): IngresoResumen {
    return IngresoResumen(
        ingresoId = ingresoId,
        placa = placa,
        modelo = modelo,
        color = color,
        numeroSerie = numeroSerie,
        nombreCliente = nombreCliente,
        telefonoCliente = telefonoCliente,
        emailCliente = emailCliente,
        fechaEntrada = 1788955200000L,
        estado = estado.name,
        motivoIngreso = motivoIngreso,
        condicionInicial = condicionInicial) }
