package com.example.myapplication.ui.diagnostico

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.local.repository.DashboardRepository
import com.example.myapplication.data.local.repository.DiagnosticoRepository
import com.example.myapplication.data.repository.ChecklistRepository

// Construye el ViewModel del diagnóstico para un ingreso específico.
class DiagnosticoViewModelFactory(
    private val ingresoId: Long,
    private val repository: DiagnosticoRepository,
    private val dashboardRepository: DashboardRepository,
    private val checklistRepository: ChecklistRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiagnosticoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DiagnosticoViewModel(
                ingresoId = ingresoId,
                repository = repository,
                dashboardRepository = dashboardRepository,
                checklistRepository = checklistRepository
            ) as T
        }

        throw IllegalArgumentException(
            "ViewModel desconocido: ${modelClass.name}"
        )
    }
}