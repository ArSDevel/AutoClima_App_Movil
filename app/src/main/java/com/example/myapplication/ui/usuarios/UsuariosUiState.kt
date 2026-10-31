package com.example.myapplication.ui.usuarios

import com.example.myapplication.data.local.model.UsuarioListado

// Estado de la lista de usuarios.
// pendienteEliminar espera la confirmación; bloqueado es quien no puede eliminarse por tener registros.
data class UsuariosUiState(
    val usuarios: List<UsuarioListado> = emptyList(),
    val cargando: Boolean = true,
    val hayError: Boolean = false,
    val pendienteEliminar: UsuarioListado? = null,
    val bloqueado: UsuarioListado? = null,
    val eliminando: Boolean = false,
    val usuarioEliminado: String? = null,
    val errorEliminar: Boolean = false)
