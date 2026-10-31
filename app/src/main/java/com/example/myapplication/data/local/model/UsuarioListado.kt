package com.example.myapplication.data.local.model

// Fila de la lista de usuarios. ingresos es lo que se muestra; actividad suma además los eventos, firmas,
// diagnósticos y fotos del técnico y decide si se puede eliminar. tieneRecuperacion indica si ya generó su código.
data class UsuarioListado(
    val tecnicoId: Long,
    val nombre: String,
    val usuario: String,
    val ingresos: Int,
    val actividad: Int,
    val tieneRecuperacion: Boolean = false) {
    val puedeEliminarse: Boolean get() = actividad == 0 }
