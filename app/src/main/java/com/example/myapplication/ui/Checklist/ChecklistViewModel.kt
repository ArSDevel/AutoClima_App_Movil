package com.example.myapplication.ui.checklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.ChecklistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChecklistViewModel(private val repo: ChecklistRepository) : ViewModel() {

    private val _checklistState = MutableStateFlow<ChecklistState>(ChecklistState.Idle)
    val checklistState: StateFlow<ChecklistState> = _checklistState

    fun guardarChecklist(
        ingresoId: Long,
        faltaGasRefrigerante: Boolean,
        fugaVisibleMangueras: Boolean,
        fugasDetectadasLuzUV: Boolean,
        compresorEmbragaAlEncender: Boolean,
        compresorDanadoOAmarrado: Boolean,
        bandaCompresorDesgastada: Boolean,
        funcionanAbanicosRadiador: Boolean,
        filtroCabinaSucioUObstruido: Boolean,
        condensadorObstruido: Boolean,
        comentarios: String?
    ) {
        viewModelScope.launch {
            _checklistState.value = ChecklistState.Loading
            try {
                val id = repo.guardarChecklist(
                    ingresoId, faltaGasRefrigerante, fugaVisibleMangueras, fugasDetectadasLuzUV,
                    compresorEmbragaAlEncender, compresorDanadoOAmarrado, bandaCompresorDesgastada,
                    funcionanAbanicosRadiador, filtroCabinaSucioUObstruido, condensadorObstruido,
                    comentarios
                )
                _checklistState.value = ChecklistState.Success(id)
            } catch (e: Exception) {
                _checklistState.value = ChecklistState.Error("Error al guardar checklist: ${e.message}")
            }
        }
    }
}