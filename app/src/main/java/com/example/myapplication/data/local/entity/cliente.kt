package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cliente")
data class Cliente(
    @PrimaryKey(autoGenerate = true) val clienteId: Long = 0,
    val nombre: String,
    val telefono: String,
    val email: String?
)