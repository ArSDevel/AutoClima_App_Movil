package com.example.myapplication.data.local.model

data class IngresoResumen(
    val ingresoId: Long,
    val placa: String,
    val modelo: String,
    val color: String?,
    val numeroSerie: String?,
    val nombreCliente: String,
    val telefonoCliente: String,
    val emailCliente: String?,
    val fechaEntrada: Long
)