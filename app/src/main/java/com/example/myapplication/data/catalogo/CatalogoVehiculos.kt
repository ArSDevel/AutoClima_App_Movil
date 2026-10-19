package com.example.myapplication.data.catalogo

import java.text.Normalizer

// Marcas y modelos frecuentes en el taller para sugerir al registrar un vehículo.
// Es una ayuda: el campo Modelo sigue admitiendo cualquier texto.
object CatalogoVehiculos {

    private val modelosPorMarca: Map<String, List<String>> = linkedMapOf(
        "Nissan" to listOf("Versa", "Sentra", "March", "Kicks", "Tsuru", "NP300", "Frontier", "X-Trail", "Tiida", "Note", "Altima"),
        "Chevrolet" to listOf("Aveo", "Spark", "Onix", "Beat", "Cavalier", "Silverado", "Tahoe", "Suburban", "Equinox", "Trax", "Tornado", "Malibu"),
        "Volkswagen" to listOf("Jetta", "Vento", "Polo", "Gol", "Virtus", "Tiguan", "Taos", "Saveiro", "Golf", "Sedán", "Passat"),
        "Toyota" to listOf("Corolla", "Yaris", "Hilux", "RAV4", "Camry", "Avanza", "Tacoma", "Tundra", "Prius", "Sienna"),
        "Honda" to listOf("Civic", "City", "CR-V", "HR-V", "Accord", "Fit", "Pilot", "Odyssey"),
        "Ford" to listOf("Fiesta", "Focus", "Figo", "Ranger", "F-150", "Escape", "Explorer", "Lobo", "EcoSport", "Mustang"),
        "Mazda" to listOf("Mazda 2", "Mazda 3", "Mazda 6", "CX-3", "CX-5", "CX-30", "CX-9"),
        "Hyundai" to listOf("Accent", "Elantra", "Grand i10", "Tucson", "Creta", "Santa Fe"),
        "Kia" to listOf("Rio", "Forte", "Sportage", "Soul", "Seltos", "Sorento"),
        "SEAT" to listOf("Ibiza", "León", "Arona", "Ateca"),
        "Dodge" to listOf("Attitude", "Ram", "Journey", "Charger"),
        "Renault" to listOf("Kwid", "Duster", "Logan", "Sandero", "Oroch"),
        "Suzuki" to listOf("Swift", "Vitara", "Ertiga", "Ignis"),
        "Mitsubishi" to listOf("L200", "Outlander", "Mirage", "ASX"),
        "Jeep" to listOf("Wrangler", "Compass", "Grand Cherokee", "Renegade"),
        "BMW" to listOf("Serie 1", "Serie 3", "Serie 5", "X1", "X3"),
        "Mercedes-Benz" to listOf("Clase A", "Clase C", "GLA", "GLC"))

    // Todas las combinaciones, por ejemplo "Nissan Versa".
    val vehiculos: List<String> = modelosPorMarca.flatMap { (marca, modelos) ->
        modelos.map { modelo -> "$marca $modelo" } }

    // Devuelve hasta [limite] sugerencias para lo que el técnico escribe.
    // Ignora mayúsculas y acentos; cada palabra escrita debe aparecer en la sugerencia.
    // No sugiere nada cuando el texto ya coincide exactamente con una opción.
    fun sugerencias(texto: String, limite: Int = 6): List<String> {
        val consulta = normalizar(texto)
        if (consulta.isBlank()) return emptyList()
        val palabras = consulta.split(' ').filter { it.isNotBlank() }

        val coincidencias = vehiculos.filter { vehiculo ->
            val clave = normalizar(vehiculo)
            clave != consulta && palabras.all { it in clave } }
        // Primero las que empiezan con lo escrito.
        return coincidencias
            .sortedByDescending { normalizar(it).startsWith(consulta) }
            .take(limite) }

    // Pasa a minúsculas, quita acentos y espacios repetidos para comparar textos.
    private fun normalizar(texto: String): String {
        return Normalizer.normalize(texto.trim().lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .replace(Regex("\\s+"), " ") } }
