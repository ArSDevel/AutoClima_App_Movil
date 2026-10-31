package com.example.myapplication.ui.usuarios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.dao.TecnicoDao
import com.example.myapplication.data.local.model.UsuarioListado
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Lista los usuarios y elimina a los que no tienen registros. El usuario de la sesión nunca se elimina.
class UsuariosViewModel(
    private val tecnicoDao: TecnicoDao,
    val tecnicoActualId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(UsuariosUiState())
    val uiState: StateFlow<UsuariosUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            tecnicoDao.observarListado()
                .catch { _uiState.update { it.copy(cargando = false, hayError = true) } }
                .collect { lista -> _uiState.update { it.copy(usuarios = lista, cargando = false, hayError = false) } } } }

    // Quien tiene registros no puede eliminarse y se explica por qué; los demás piden confirmación.
    fun pedirEliminar(usuario: UsuarioListado) {
        if (usuario.tecnicoId == tecnicoActualId || _uiState.value.eliminando) return
        _uiState.update {
            if (usuario.puedeEliminarse) it.copy(pendienteEliminar = usuario) else it.copy(bloqueado = usuario) } }

    fun cancelarEliminar() {
        if (!_uiState.value.eliminando) _uiState.update { it.copy(pendienteEliminar = null) } }

    fun cerrarBloqueado() { _uiState.update { it.copy(bloqueado = null) } }

    fun confirmarEliminar() {
        val usuario = _uiState.value.pendienteEliminar ?: return
        if (_uiState.value.eliminando) return
        _uiState.update { it.copy(eliminando = true) }
        viewModelScope.launch {
            try {
                // La base repite la comprobación al borrar: si el técnico ya tiene registros no se elimina.
                val borrados = tecnicoDao.eliminarSinActividad(usuario.tecnicoId, tecnicoActualId)
                _uiState.update {
                    if (borrados > 0) {
                        it.copy(eliminando = false, pendienteEliminar = null, usuarioEliminado = usuario.usuario)
                    } else {
                        it.copy(eliminando = false, pendienteEliminar = null, bloqueado = usuario) } }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _uiState.update { it.copy(eliminando = false, pendienteEliminar = null, errorEliminar = true) } } } }

    // La pantalla avisa que ya mostró el mensaje.
    fun avisoMostrado() { _uiState.update { it.copy(usuarioEliminado = null, errorEliminar = false) } } }
