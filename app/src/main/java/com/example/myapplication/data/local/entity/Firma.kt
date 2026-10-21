package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Firma del cliente al aceptar la cotización. La imagen vive en un archivo;
// Room guarda su ubicación, quién la registró y cuándo.
@Entity(
    tableName = "firma",
    foreignKeys = [ForeignKey(entity = Ingreso::class, parentColumns = ["ingresoId"],
        childColumns = ["ingresoId"], onDelete = ForeignKey.CASCADE)],
    // Cada ingreso conserva una sola firma vigente.
    indices = [Index(value = ["ingresoId"], unique = true)])
data class Firma(
    @PrimaryKey(autoGenerate = true)
    val firmaId: Long = 0,
    val ingresoId: Long,
    val rutaImagen: String,
    // Técnico que registró la firma.
    val tecnicoId: Long,
    // Fecha y hora en milisegundos.
    val fecha: Long)
