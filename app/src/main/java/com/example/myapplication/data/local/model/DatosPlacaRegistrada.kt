package com.example.myapplication.data.local.model

// Datos guardados de un vehículo y de su cliente, para llenar el registro de una visita nueva.
data class DatosPlacaRegistrada(
    // Placa tal como se guardó, con sus guiones.
    val placa: String,
    val modelo: String,
    val color: String,
    val numeroSerie: String,
    val nombre: String,
    val telefono: String,
    val email: String,
    // El vehículo tiene un ingreso sin entregar ni cancelar, así que no admite otro.
    val ingresoAbierto: Boolean)
