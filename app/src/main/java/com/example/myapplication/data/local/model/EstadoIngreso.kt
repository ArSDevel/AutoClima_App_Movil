package com.example.myapplication.data.local.model

// Estados de un ingreso al taller.
// El nombre de cada estado corresponde al valoralmacenado en la base de datos.
enum class EstadoIngreso {
    REGISTRADO,
    EN_REVISION,
    EN_REPARACION,
    LISTO_PARA_FIRMA,
    LISTO_PARA_ENTREGA,
    ENTREGADO,
    CANCELADO,
    // Para registros sin estado o con un valor desconocido.
    NO_DISPONIBLE;
    companion object {
        // Convierte el valor almacenado en un estado conocido.
         // Si es null o no coincide con ningún estado,devuelve NO_DISPONIBLE
        fun desdeValor(valor: String?): EstadoIngreso {
            return entries.firstOrNull { estado ->
                estado.name == valor
            } ?: NO_DISPONIBLE
        }
    }
}