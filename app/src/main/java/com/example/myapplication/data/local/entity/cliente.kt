package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Persona que deja el vehículo; el teléfono identifica al cliente al registrar otra visita.
@Entity(tableName = "cliente")
data class Cliente(
    @PrimaryKey(autoGenerate = true) val clienteId: Long = 0,
    val nombre: String,
    val telefono: String,
    // Opcional; null si no se capturó.
    val email: String?)