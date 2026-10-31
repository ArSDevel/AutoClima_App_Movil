package com.example.myapplication.data.respaldo

import androidx.annotation.StringRes

// Problema del respaldo que se le puede explicar al usuario con un texto.
class RespaldoException(@StringRes val mensaje: Int) : Exception()
