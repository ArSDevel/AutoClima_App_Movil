package com.example.myapplication.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Fotografía almacenada en el dispositivo.
// Room guarda su ubicación y los datos asociados,
// mientras que la imagen permanece en un archivo.
@Entity(tableName = "tabla_evidencias",
    foreignKeys = [
        ForeignKey(entity = Ingreso::class,
            parentColumns = ["ingresoId"],
            childColumns = ["ingresoId"],
            onDelete = ForeignKey.RESTRICT)],
    indices = [Index(value = ["ingresoId"])])
data class Evidencia(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val rutaImagen: String,
    val ingresoId: Long? = null,

    // Técnico que registró la evidencia.
    val tecnicoId: Long? = null,

    // Fecha de registro en milisegundos.
    val fecha: Long? = null,

    // Descripción breve y opcional de lo que muestra la fotografía.
    @ColumnInfo(defaultValue = "''")
    val descripcion: String = "")