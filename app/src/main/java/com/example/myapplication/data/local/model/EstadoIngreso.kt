package com.example.myapplication.data.local.model

enum class EstadoIngreso {
    REGISTRADO,
    EN_REVISION,
    EN_REPARACION,
    LISTO_PARA_FIRMA,
    LISTO_PARA_ENTREGA,
    NO_DISPONIBLE;

    companion object {
        fun desdeValor(valor: String?): EstadoIngreso {
            return entries.firstOrNull { estado ->
                estado.name == valor
            } ?: NO_DISPONIBLE
        }
    }
}