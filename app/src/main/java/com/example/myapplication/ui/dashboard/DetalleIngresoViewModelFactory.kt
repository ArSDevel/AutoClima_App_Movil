package com.example.myapplication.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.local.repository.DashboardRepository

// Crea el ViewModel del detalle con el identificador
// del ingreso y el repositorio correspondiente.
class DetalleIngresoViewModelFactory(
    private val ingresoId: Long,
    private val repository: DashboardRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetalleIngresoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DetalleIngresoViewModel(ingresoId = ingresoId, repository = repository) as T
        }

        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}