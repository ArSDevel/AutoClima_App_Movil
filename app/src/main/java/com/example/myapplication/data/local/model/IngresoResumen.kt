package com.example.myapplication.data.local.model

//bResultado de la consulta que combina ingreso, vehículo y cliente.No es una tabla de Room.
data class IngresoResumen(
    val ingresoId: Long,
    val placa: String,
    val modelo: String,
    val color: String?,
    val numeroSerie: String?,
    val nombreCliente: String,
    val telefonoCliente: String,
    val emailCliente: String?,
    val fechaEntrada: Long,
    val estado: String = EstadoIngreso.NO_DISPONIBLE.name,
    val motivoIngreso: String? = null,
    val condicionInicial: String? = null
)