package com.example.myapplication.ui.checklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.local.repository.DashboardRepository
import com.example.myapplication.data.repository.ChecklistRepository

// Proporciona al ViewModel el ingreso seleccionado
// y los repositorios que necesita para consultar y guardar.
class ChecklistViewModelFactory(
    private val ingresoId: Long,
    private val repo: ChecklistRepository,
    private val dashboardRepository: DashboardRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChecklistViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChecklistViewModel(
                ingresoId = ingresoId,
                repo = repo,
                dashboardRepository = dashboardRepository
            ) as T
        }

        throw IllegalArgumentException(
            "ViewModel desconocido: ${modelClass.name}"
        )
    }
}