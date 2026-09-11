package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Registra una operación realizada sobre un ingreso. Conserva cuándo ocurrió, qué técnico la realizó
// y el motivo o descripción de la operación.
@Entity(
    tableName = "evento_ingreso",
    foreignKeys = [ForeignKey(entity = Ingreso::class, parentColumns = ["ingresoId"], childColumns = ["ingresoId"],
            onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["ingresoId"])]
)
data class EventoIngreso(
    @PrimaryKey(autoGenerate = true)
    val eventoId: Long = 0,
    val ingresoId: Long,
    // Fecha y hora en milisegundos
    val fecha: Long,
    // Técnico que realizó la operación
    val tecnicoId: Long,
    // Valores previstos: CREADO, EDITADO y ESTADO
    val accion: String,
    // En la creación no hay estado anterior: se guardará ""
    val estadoAnterior: String,
    val estadoNuevo: String,
    // Descripción de la operación o motivo del cambio
    val motivo: String
)