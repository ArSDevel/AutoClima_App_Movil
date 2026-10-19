package com.example.myapplication.ui.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.local.repository.HistorialRepository

// Crea el ViewModel del historial.
class HistorialViewModelFactory(
    private val repository: HistorialRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(HistorialViewModel::class.java)) {
            "ViewModel desconocido: ${modelClass.name}" }
        @Suppress("UNCHECKED_CAST")
        return HistorialViewModel(repository) as T } }
