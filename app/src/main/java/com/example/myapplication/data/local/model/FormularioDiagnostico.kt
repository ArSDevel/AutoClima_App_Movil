package com.example.myapplication.data.local.model

import java.math.BigDecimal

// Una fila editable de la cotización.
// El importe permanece como texto mientras el mecánico escribe.
data class ConceptoCotizacionFormulario(
    val descripcion: String = "",
    val importe: String = ""
) {

    // Convierte el importe a centavos sin usar Float ni Double.
    // Acepta ejemplos como: 150, 150.50 o 150,50.
    // No acepta símbolos de moneda ni separadores de miles.
    fun importeEnCentavos(): Long? {
        val texto = importe.trim()

        if (!FORMATO_IMPORTE.matches(texto)) {
            return null
        }

        return try {
            BigDecimal(texto.replace(',', '.'))
                .movePointRight(2)
                .longValueExact()
        } catch (_: NumberFormatException) {
            null
        } catch (_: ArithmeticException) {
            null
        }
    }

    companion object {
        private val FORMATO_IMPORTE =
            Regex("""[0-9]+([.,][0-9]{1,2})?""")
    }
}

// Datos que el mecánico puede modificar en el diagnóstico.
// Cliente, vehículo y checklist se consultan por separado.
data class FormularioDiagnostico(
    val observaciones: String = "",
    val conceptos: List<ConceptoCotizacionFormulario> = emptyList()
) {

    // Devuelve errores asociados a cada campo del formulario.
    // Ejemplos de claves:
    // conceptos.0.descripcion
    // conceptos.0.importe
    // conceptos.total
    fun errores(): Map<String, String> {
        val errores = mutableMapOf<String, String>()

        conceptos.forEachIndexed { indice, concepto ->
            if (concepto.descripcion.isBlank()) {
                errores["conceptos.$indice.descripcion"] =
                    "Escribe el concepto del servicio o refacción."
            }

            if (concepto.importe.isBlank()) {
                errores["conceptos.$indice.importe"] =
                    "Escribe el importe."
            } else if (concepto.importeEnCentavos() == null) {
                errores["conceptos.$indice.importe"] =
                    "Ingresa un importe válido con máximo dos decimales, " +
                            "sin símbolo de moneda ni separadores de miles."
            }
        }

        // Comprueba también que la suma quepa en el tipo Long.
        // Solo evalúa el total cuando los importes individuales son válidos.
        val importesValidos = conceptos.all {
            it.importeEnCentavos() != null
        }

        if (importesValidos && totalEnCentavos() == null) {
            errores["conceptos.total"] =
                "El total de la cotización es demasiado grande."
        }

        return errores
    }

    // Calcula el total exacto.
    // null indica un importe inválido o un total fuera de rango.
    // Una cotización sin conceptos suma cero, pero sigue pendiente.
    fun totalEnCentavos(): Long? {
        var total = 0L

        for (concepto in conceptos) {
            val importeCentavos = concepto.importeEnCentavos()
                ?: return null

            total = try {
                Math.addExact(total, importeCentavos)
            } catch (_: ArithmeticException) {
                return null
            }
        }

        return total
    }

    // Distingue una cotización pendiente de una cotización
    // con conceptos cuyo importe sea cero.
    val tieneCotizacion: Boolean
        get() = conceptos.isNotEmpty()

    // Limpia espacios y unifica los importes antes de comparar
    // o guardar. Por ejemplo, 150 y 150.00 quedan iguales.
    fun normalizado(): FormularioDiagnostico {
        return copy(
            observaciones = observaciones.trim(),
            conceptos = conceptos.map { concepto ->
                val centavos = concepto.importeEnCentavos()

                concepto.copy(
                    descripcion = concepto.descripcion.trim(),
                    importe = if (centavos != null) {
                        BigDecimal.valueOf(centavos, 2).toPlainString()
                    } else {
                        concepto.importe.trim()
                    }
                )
            }
        )
    }
}