package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tecnico")
data class Tecnico(
    @PrimaryKey(autoGenerate = true) val tecnicoId: Long = 0,
    val nombre: String,
    val usuario: String,
    val passwordHash: String
)