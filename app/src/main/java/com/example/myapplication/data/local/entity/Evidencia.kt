package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabla_evidencias")
data class Evidencia(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val rutaImagen: String
)