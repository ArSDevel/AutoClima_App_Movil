package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Información adicional del diagnóstico de un ingreso.
// Los datos del vehículo, cliente y checklist se consultan
// desde sus registros existentes.
@Entity(
    tableName = "diagnostico",
    foreignKeys = [
        ForeignKey(
            entity = Ingreso::class,
            parentColumns = ["ingresoId"],
            childColumns = ["ingresoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        // Cada ingreso tiene un solo diagnóstico editable.
        Index(value = ["ingresoId"], unique = true)
    ]
)
data class Diagnostico(
    @PrimaryKey(autoGenerate = true)
    val diagnosticoId: Long = 0,

    val ingresoId: Long,

    // Notas redactadas por el mecánico para el reporte.
    // No reemplazan los comentarios originales del checklist.
    val observaciones: String = "",

    // Técnico que realizó el último guardado.
    val tecnicoId: Long,

    // Se conserva al modificar el diagnóstico.
    val fechaCreacion: Long = System.currentTimeMillis(),

    // Se actualiza únicamente cuando se guardan cambios.
    val fechaActualizacion: Long = fechaCreacion
)