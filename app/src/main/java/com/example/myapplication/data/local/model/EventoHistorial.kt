package com.example.myapplication.data.local.model

// Una fila de la bitácora. Los datos del vehículo y cliente son los actuales;
// la acción, fecha, técnico, motivo y estados pertenecen al evento guardado.
data class EventoHistorial(
    val eventoId: Long,
    val ingresoId: Long,
    val fecha: Long,
    val tecnicoId: Long,
    val nombreTecnico: String?,
    val placa: String,
    val modelo: String,
    val nombreCliente: String,
    val accion: String,
    val estadoAnterior: String,
    val estadoNuevo: String,
    val motivo: String)
