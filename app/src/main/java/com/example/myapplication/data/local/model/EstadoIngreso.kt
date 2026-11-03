package com.example.myapplication.data.local.model

// Estados de un ingreso al taller.
// El nombre de cada estado corresponde al valor almacenado en la base de datos.
enum class EstadoIngreso {
    REGISTRADO,
    EN_REVISION,
    EN_REPARACION,
    LISTO_PARA_FIRMA,
    LISTO_PARA_ENTREGA,
    ENTREGADO,
    CANCELADO;
    companion object {
        // Convierte el valor almacenado en un estado.
        // La app solo guarda estos siete valores. Si llegara otro, por ejemplo desde un archivo modificado a mano,
        // se trata como Registrado para que el ingreso se pueda abrir y seguir su flujo normal.
        fun desdeValor(valor: String?): EstadoIngreso {
            return entries.firstOrNull { estado ->
                estado.name == valor
            } ?: REGISTRADO } } }