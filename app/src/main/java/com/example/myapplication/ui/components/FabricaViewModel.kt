package com.example.myapplication.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

// Crea un ViewModel con los datos que ya tiene la pantalla, sin una fábrica por cada clase.
inline fun <reified T : ViewModel> fabricaViewModel(crossinline crear: () -> T): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        override fun <V : ViewModel> create(modelClass: Class<V>): V {
            require(modelClass.isAssignableFrom(T::class.java)) { "ViewModel desconocido: ${modelClass.name}" }
            @Suppress("UNCHECKED_CAST")
            return crear() as V } } }
