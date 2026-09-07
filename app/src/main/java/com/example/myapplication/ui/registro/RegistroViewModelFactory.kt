package com.example.myapplication.ui.registro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.repository.RegistroRepository

class RegistroViewModelFactory(private val repo: RegistroRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegistroViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegistroViewModel(repo) as T
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}