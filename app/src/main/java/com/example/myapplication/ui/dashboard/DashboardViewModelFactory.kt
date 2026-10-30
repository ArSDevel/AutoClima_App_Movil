package com.example.myapplication.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.local.repository.DashboardRepository

// Crea el ViewModel del listado con el repositorio compartido.
class DashboardViewModelFactory(
    private val repository: DashboardRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository) as T }
        throw IllegalArgumentException(
            "ViewModel desconocido: ${modelClass.name}") } }