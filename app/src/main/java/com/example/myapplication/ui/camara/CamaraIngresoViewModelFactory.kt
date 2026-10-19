package com.example.myapplication.ui.camara

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.local.repository.EvidenciaRepository

class CamaraIngresoViewModelFactory(
    private val ingresoId: Long,
    private val repository: EvidenciaRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (
            modelClass.isAssignableFrom(
                CamaraIngresoViewModel::class.java
            )
        ) {
            @Suppress("UNCHECKED_CAST")
            return CamaraIngresoViewModel(
                ingresoId = ingresoId,
                repository = repository
            ) as T
        }

        throw IllegalArgumentException(
            "ViewModel desconocido: ${modelClass.name}"
        )
    }
}