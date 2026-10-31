package com.example.myapplication.data.respaldo

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.R
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.util.PasswordHasher
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

// Crea y restaura respaldos. Un respaldo es un .zip con el manifiesto, la base de datos y las fotos y firmas que esa base
// usa. Todas las funciones son bloqueantes: se llaman fuera del hilo principal.
class ServicioRespaldo(private val contexto: Context) {

    private val directorioArchivos: File get() = contexto.filesDir
    private val directorioTemporal: File get() = File(contexto.cacheDir, "restaurar")
    private val directorioCompartir: File get() = File(contexto.cacheDir, "respaldos")
    private val directorioAutomaticos: File get() = File(directorioArchivos, CARPETA_AUTOMATICOS)
    private val preferencias get() = contexto.getSharedPreferences("respaldo", Context.MODE_PRIVATE)

    // Cantidades de datos que tiene este celular.
    fun conteosActuales(): ConteosDatos {
        val bd = AppDatabase.getInstance(contexto).openHelper.readableDatabase
        return ConteosDatos(contar(bd, "ingreso"), contar(bd, "vehiculo"), contar(bd, "cliente"), contar(bd, "tecnico")) }

    // El celular está vacío cuando no tiene ingresos, vehículos ni clientes; los usuarios no cuentan porque el primer
    // uso siempre crea uno.
    fun celularVacio(): Boolean = conteosActuales().let { it.ingresos == 0 && it.vehiculos == 0 && it.clientes == 0 }

    // Cuándo se guardó o compartió el último respaldo, o null si nunca.
    fun ultimoRespaldoMs(): Long? = preferencias.getLong(CLAVE_ULTIMO, 0L).takeIf { it > 0L }

    private fun marcarRespaldoHecho() { preferencias.edit().putLong(CLAVE_ULTIMO, System.currentTimeMillis()).apply() }

    fun nombreArchivo(ahora: Long = System.currentTimeMillis()): String =
        "AutoClimas_respaldo_" + SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.US).format(Date(ahora)) + ".zip"

    // Escribe el respaldo completo en la salida indicada.
    fun exportar(salida: OutputStream) {
        val base = AppDatabase.getInstance(contexto)
        val sqlite = base.openHelper.writableDatabase
        // Pasa lo pendiente del registro de escritura al archivo principal para copiar la base completa.
        sqlite.query("PRAGMA wal_checkpoint(TRUNCATE)").use { it.moveToFirst() }
        val archivos = archivosUsados(sqlite)
        val manifiesto = ManifiestoRespaldo(
            versionApp = nombreVersion(), versionBd = sqlite.version, fechaMs = System.currentTimeMillis(),
            ingresos = contar(sqlite, "ingreso"), vehiculos = contar(sqlite, "vehiculo"),
            clientes = contar(sqlite, "cliente"), usuarios = contar(sqlite, "tecnico"), archivos = archivos.size)
        val zip = ZipOutputStream(salida.buffered())
        zip.putNextEntry(ZipEntry(ManifiestoRespaldo.NOMBRE_ENTRADA))
        zip.write(manifiesto.aTexto().toByteArray())
        zip.closeEntry()
        // La copia se hace dentro de una transacción para que nadie escriba en la base mientras se copia.
        base.runInTransaction {
            zip.putNextEntry(ZipEntry(RutasRespaldo.ARCHIVO_BD))
            contexto.getDatabasePath(AppDatabase.NOMBRE).inputStream().use { it.copyTo(zip) }
            zip.closeEntry() }
        archivos.forEach { (entrada, archivo) ->
            zip.putNextEntry(ZipEntry(entrada))
            archivo.inputStream().use { it.copyTo(zip) }
            zip.closeEntry() }
        zip.finish()
        zip.flush() }

    // Guarda el respaldo que el usuario pidió y lo anota como el último.
    fun exportarManual(salida: OutputStream) {
        exportar(salida)
        marcarRespaldoHecho() }

    // Arma el respaldo en la carpeta temporal de compartir; antes borra los anteriores.
    fun exportarParaCompartir(): File {
        directorioCompartir.deleteRecursively()
        directorioCompartir.mkdirs()
        val archivo = File(directorioCompartir, nombreArchivo())
        archivo.outputStream().use { exportar(it) }
        marcarRespaldoHecho()
        return archivo }

    // Copias automáticas: una por semana, dentro del teléfono, y se guardan las últimas 4. No sustituyen a un respaldo
    // fuera del teléfono, pero protegen de un error o una base dañada. Solo se crean si ya hay ingresos.
    fun copiasAutomaticas(): List<File> =
        directorioAutomaticos.listFiles { archivo -> archivo.isFile && archivo.name.endsWith(".zip") }
            ?.sortedByDescending { it.lastModified() }.orEmpty()

    fun crearCopiaAutomaticaSiToca(ahora: Long = System.currentTimeMillis()) {
        val ultima = preferencias.getLong(CLAVE_AUTOMATICA, 0L)
        if (ahora - ultima < SEMANA_MS || conteosActuales().ingresos == 0) return
        directorioAutomaticos.mkdirs()
        val archivo = File(directorioAutomaticos, nombreArchivo(ahora).replace("respaldo", "auto"))
        try { archivo.outputStream().use { exportar(it) } } catch (e: Exception) { archivo.delete(); throw e }
        preferencias.edit().putLong(CLAVE_AUTOMATICA, ahora).apply()
        copiasAutomaticas().drop(MAXIMO_COPIAS).forEach { it.delete() } }

    // Descomprime y valida un respaldo sin tocar los datos actuales. Si algo está mal lanza RespaldoException.
    fun prepararRestauracion(entrada: InputStream): ManifiestoRespaldo {
        descartarRestauracion()
        try {
            ExtractorZip.extraer(entrada, directorioTemporal)
            val manifiesto = File(directorioTemporal, ManifiestoRespaldo.NOMBRE_ENTRADA).takeIf { it.isFile }
                ?.readText()?.let { ManifiestoRespaldo.desdeTexto(it) }
                ?: throw RespaldoException(R.string.respaldo_error_no_valido)
            if (manifiesto.formato != ManifiestoRespaldo.FORMATO_ACTUAL) {
                throw RespaldoException(R.string.respaldo_error_no_valido) }
            val archivoBd = File(directorioTemporal, RutasRespaldo.ARCHIVO_BD)
            if (!archivoBd.isFile) throw RespaldoException(R.string.respaldo_error_no_valido)
            val versionActual = AppDatabase.getInstance(contexto).openHelper.readableDatabase.version
            val versionRespaldo = verificarBase(archivoBd)
            if (versionRespaldo > versionActual) throw RespaldoException(R.string.respaldo_error_version_nueva)
            if (versionRespaldo < VERSION_MINIMA) throw RespaldoException(R.string.respaldo_error_version_vieja)
            return manifiesto
        } catch (e: Throwable) {
            descartarRestauracion()
            throw e } }

    // Reemplaza los datos actuales por los del respaldo ya preparado. Si algo falla a la mitad devuelve todo a como
    // estaba. Después de restaurar con éxito la app debe cerrarse y abrirse de nuevo.
    fun aplicarRestauracion() {
        val nuevaBd = File(directorioTemporal, RutasRespaldo.ARCHIVO_BD)
        if (!nuevaBd.isFile) throw RespaldoException(R.string.respaldo_error_no_valido)
        val destinoBd = contexto.getDatabasePath(AppDatabase.NOMBRE)
        val previos = mutableListOf<Pair<File, File>>()
        val copiados = mutableListOf<File>()
        AppDatabase.cerrar()
        try {
            // La base actual se aparta con otro nombre, no se borra, hasta saber que la nueva abre bien.
            for (sufijo in listOf("", "-wal", "-shm", "-journal")) {
                val actual = File(destinoBd.path + sufijo)
                if (actual.exists()) {
                    val previo = File(actual.path + ".previo")
                    previo.delete()
                    check(actual.renameTo(previo)) { "No se pudo apartar ${actual.name}" }
                    previos += actual to previo } }
            destinoBd.parentFile?.mkdirs()
            mover(nuevaBd, destinoBd)
            for (carpeta in listOf(RutasRespaldo.CARPETA_FOTOS, RutasRespaldo.CARPETA_FIRMAS)) {
                File(directorioTemporal, carpeta).listFiles()?.forEach { origen ->
                    val destino = RutasRespaldo.destinoDe("$carpeta/${origen.name}", directorioArchivos)
                    destino.parentFile?.mkdirs()
                    mover(origen, destino)
                    copiados += destino } }
            // Abrir la base aplica las migraciones si el respaldo es de una versión anterior.
            val sqlite = AppDatabase.getInstance(contexto).openHelper.writableDatabase
            reubicarRutas(sqlite)
            previos.forEach { it.second.delete() }
        } catch (e: Throwable) {
            AppDatabase.cerrar()
            for (sufijo in listOf("", "-wal", "-shm", "-journal")) File(destinoBd.path + sufijo).delete()
            copiados.forEach { it.delete() }
            previos.forEach { (original, previo) -> original.delete(); previo.renameTo(original) }
            throw e
        } finally {
            directorioTemporal.deleteRecursively() } }

    // Borra lo que quedó de una restauración preparada y no aplicada.
    fun descartarRestauracion() { directorioTemporal.deleteRecursively() }

    // ---------------------------------------------------------------------------------------------------------------
    // Combinar
    // ---------------------------------------------------------------------------------------------------------------

    // Compara el respaldo ya preparado con los datos del celular, sin cambiar nada.
    fun analizarCombinacion(): PlanCombinacion {
        val local = AppDatabase.getInstance(contexto).openHelper.readableDatabase
        abrirOrigen().use { origen ->
            val vehiculosLocales = placasLocales(local)
            val clavesLocales = ingresosDe(local.consultar(SQL_INGRESOS)).associateBy { it.clave }
            val placasOrigen = origen.consultar("SELECT vehiculoId, placa FROM vehiculo")
                .associate { it.getAsLong("vehiculoId") to it.getAsString("placa").orEmpty() }
            val comparados = ingresosDe(origen.consultar(SQL_INGRESOS)).map { fila ->
                val placa = placasOrigen[fila.vehiculoId].orEmpty()
                val vehiculoLocal = vehiculosLocales[ReglasCombinacion.normalizarPlaca(placa)]
                val equivalente = vehiculoLocal?.let { clavesLocales[ReglasCombinacion.claveIngreso(it, fila.fecha, fila.motivo)] }
                IngresoComparado(
                    idRespaldo = fila.id, idCelular = equivalente?.id, placa = placa, fecha = fila.fecha,
                    motivo = fila.motivo, celular = equivalente?.estado, respaldo = fila.estado) }
            // Vehículos y clientes que llegarían con los ingresos nuevos.
            val vehiculosNuevos = comparados.filter { it.esNuevo }.map { ReglasCombinacion.normalizarPlaca(it.placa) }
                .filter { it !in vehiculosLocales }.toSet()
            val clientesLocales = local.consultar("SELECT nombre, telefono FROM cliente")
            val clientesNuevos = origen.consultar(
                "SELECT c.clienteId, c.nombre, c.telefono, v.placa FROM vehiculo v JOIN cliente c ON c.clienteId = v.clienteId")
                .filter { ReglasCombinacion.normalizarPlaca(it.getAsString("placa")) in vehiculosNuevos }
                .distinctBy { it.getAsLong("clienteId") }
                .count { c -> clientesLocales.none { l ->
                    ReglasCombinacion.mismoCliente(c.getAsString("nombre"), c.getAsString("telefono"),
                        l.getAsString("nombre"), l.getAsString("telefono")) } }
            return PlanCombinacion(comparados, vehiculosNuevos.size, clientesNuevos) } }

    // Aplica la combinación en un solo paso: o entra todo o no entra nada. Los ingresos nuevos se agregan completos; en
    // los que están en los dos, el lado ganador se queda completo y el otro se descarta. actorId es el usuario que se
    // usa si un registro del respaldo hace referencia a un usuario que ya no existe en él.
    fun aplicarCombinacion(plan: PlanCombinacion, ganadores: Map<Long, Lado>, actorId: Long): ResultadoCombinacion {
        val base = AppDatabase.getInstance(contexto)
        val copiados = mutableListOf<File>()
        val obsoletos = mutableSetOf<String>()
        val conteo = IntArray(6)   // agregados, actualizados, sin cambios, vehículos, clientes, usuarios
        try {
            abrirOrigen().use { origen ->
                val copiarRuta = fabricaCopiaDeArchivos(copiados)
                base.runInTransaction {
                    val bd = base.openHelper.writableDatabase
                    val vehiculosLocales = placasLocales(bd).toMutableMap()
                    val clientesDeOrigen = mutableMapOf<Long, Long>()
                    val vehiculosDeOrigen = mutableMapOf<Long, Long>()
                    val usuariosDeOrigen = mutableMapOf<Long, Long>()

                    fun usuarioLocal(idOrigen: Long): Long = usuariosDeOrigen.getOrPut(idOrigen) {
                        val fila = origen.consultar("SELECT * FROM tecnico WHERE tecnicoId = ?", idOrigen.toString()).firstOrNull()
                            ?: return@getOrPut actorId
                        val existente = bd.consultar("SELECT tecnicoId FROM tecnico WHERE LOWER(usuario) = LOWER(?)",
                            fila.getAsString("usuario")).firstOrNull()?.getAsLong("tecnicoId")
                        existente ?: run {
                            fila.remove("tecnicoId")
                            conteo[5]++
                            insertar(bd, "tecnico", fila) } }

                    fun clienteLocal(idOrigen: Long): Long = clientesDeOrigen.getOrPut(idOrigen) {
                        val fila = origen.consultar("SELECT * FROM cliente WHERE clienteId = ?", idOrigen.toString()).first()
                        val existente = bd.consultar("SELECT clienteId, nombre, telefono FROM cliente").firstOrNull { l ->
                            ReglasCombinacion.mismoCliente(fila.getAsString("nombre"), fila.getAsString("telefono"),
                                l.getAsString("nombre"), l.getAsString("telefono")) }?.getAsLong("clienteId")
                        existente ?: run {
                            fila.remove("clienteId")
                            conteo[4]++
                            insertar(bd, "cliente", fila) } }

                    fun vehiculoLocal(idOrigen: Long): Long = vehiculosDeOrigen.getOrPut(idOrigen) {
                        val fila = origen.consultar("SELECT * FROM vehiculo WHERE vehiculoId = ?", idOrigen.toString()).first()
                        val placa = ReglasCombinacion.normalizarPlaca(fila.getAsString("placa"))
                        vehiculosLocales[placa] ?: run {
                            fila.remove("vehiculoId")
                            fila.put("clienteId", clienteLocal(fila.getAsLong("clienteId")))
                            conteo[3]++
                            insertar(bd, "vehiculo", fila).also { vehiculosLocales[placa] = it } } }

                    fun rutaNueva(ruta: String?): String? = ruta?.let { copiarRuta(it) }?.path

                    fun copiarHijos(idOrigen: Long, idLocal: Long) {
                        origen.consultar("SELECT * FROM checklist_diagnostico WHERE ingresoId = ?", idOrigen.toString()).forEach { fila ->
                            fila.remove("checklistId"); fila.put("ingresoId", idLocal)
                            insertar(bd, "checklist_diagnostico", fila) }
                        origen.consultar("SELECT * FROM diagnostico WHERE ingresoId = ?", idOrigen.toString()).forEach { fila ->
                            val idViejo = fila.getAsLong("diagnosticoId")
                            fila.remove("diagnosticoId"); fila.put("ingresoId", idLocal)
                            fila.put("tecnicoId", usuarioLocal(fila.getAsLong("tecnicoId")))
                            val idNuevo = insertar(bd, "diagnostico", fila)
                            origen.consultar("SELECT * FROM concepto_cotizacion WHERE diagnosticoId = ?", idViejo.toString()).forEach { c ->
                                c.remove("conceptoId"); c.put("diagnosticoId", idNuevo)
                                insertar(bd, "concepto_cotizacion", c) } }
                        origen.consultar("SELECT * FROM firma WHERE ingresoId = ?", idOrigen.toString()).forEach { fila ->
                            val ruta = rutaNueva(fila.getAsString("rutaImagen")) ?: return@forEach
                            fila.remove("firmaId"); fila.put("ingresoId", idLocal); fila.put("rutaImagen", ruta)
                            fila.put("tecnicoId", usuarioLocal(fila.getAsLong("tecnicoId")))
                            insertar(bd, "firma", fila) }
                        origen.consultar("SELECT * FROM evento_ingreso WHERE ingresoId = ?", idOrigen.toString()).forEach { fila ->
                            fila.remove("eventoId"); fila.put("ingresoId", idLocal)
                            fila.put("tecnicoId", usuarioLocal(fila.getAsLong("tecnicoId")))
                            insertar(bd, "evento_ingreso", fila) }
                        origen.consultar("SELECT * FROM tabla_evidencias WHERE ingresoId = ?", idOrigen.toString()).forEach { fila ->
                            val ruta = rutaNueva(fila.getAsString("rutaImagen")) ?: return@forEach
                            fila.remove("id"); fila.put("ingresoId", idLocal); fila.put("rutaImagen", ruta)
                            if (fila.getAsLong("tecnicoId") != null) fila.put("tecnicoId", usuarioLocal(fila.getAsLong("tecnicoId")))
                            insertar(bd, "tabla_evidencias", fila) } }

                    for (ingreso in plan.ingresos) {
                        val ganador = if (ingreso.esNuevo) Lado.RESPALDO else ganadores[ingreso.idRespaldo]
                            ?: ingreso.ganadorSegun(ModoCombinacion.MAS_RECIENTE)
                        if (ingreso.esIgual || (!ingreso.esNuevo && ganador == Lado.CELULAR)) { conteo[2]++; continue }
                        val fila = origen.consultar("SELECT * FROM ingreso WHERE ingresoId = ?", ingreso.idRespaldo.toString()).first()
                        val vehiculoDeOrigen = fila.getAsLong("vehiculoId")
                        fila.remove("ingresoId")
                        fila.put("vehiculoId", vehiculoLocal(vehiculoDeOrigen))
                        fila.put("tecnicoId", usuarioLocal(fila.getAsLong("tecnicoId")))
                        fila.put("fotoPath", rutaNueva(fila.getAsString("fotoPath")))
                        if (ingreso.idCelular == null) {
                            val idNuevo = insertar(bd, "ingreso", fila)
                            copiarHijos(ingreso.idRespaldo, idNuevo)
                            conteo[0]++
                        } else {
                            val id = ingreso.idCelular
                            obsoletos += rutasDeIngreso(bd, id)
                            bd.execSQL("DELETE FROM concepto_cotizacion WHERE diagnosticoId IN " +
                                "(SELECT diagnosticoId FROM diagnostico WHERE ingresoId = ?)", arrayOf<Any>(id))
                            for (tabla in listOf("evento_ingreso", "checklist_diagnostico", "diagnostico", "firma", "tabla_evidencias")) {
                                bd.execSQL("DELETE FROM $tabla WHERE ingresoId = ?", arrayOf<Any>(id)) }
                            fila.remove("vehiculoId")
                            bd.update("ingreso", SQLiteDatabase.CONFLICT_NONE, fila, "ingresoId = ?", arrayOf(id.toString()))
                            copiarHijos(ingreso.idRespaldo, id)
                            conteo[1]++ } } } }
        } catch (e: Throwable) {
            copiados.forEach { it.delete() }
            throw e
        } finally {
            directorioTemporal.deleteRecursively() }
        // Con todo guardado, se borran las fotos y firmas que los ingresos reemplazados ya no usan.
        val bd = AppDatabase.getInstance(contexto).openHelper.readableDatabase
        obsoletos.filterNot { rutaEnUso(bd, it) }.forEach { File(it).delete() }
        return ResultadoCombinacion(conteo[0], conteo[1], conteo[2], conteo[3], conteo[4], conteo[5]) }

    // Copia cada archivo del respaldo una sola vez a la carpeta de la app con un nombre nuevo, para que nunca choque con
    // uno que ya exista. Devuelve null si el archivo no vino en el respaldo.
    private fun fabricaCopiaDeArchivos(copiados: MutableList<File>): (String) -> File? {
        val hechos = mutableMapOf<String, File?>()
        var contador = 0
        val marca = System.currentTimeMillis()
        return { rutaGuardada ->
            val entrada = RutasRespaldo.entradaDeRutaGuardada(rutaGuardada)
            hechos.getOrPut(entrada) {
                val origen = File(directorioTemporal, entrada)
                if (!origen.isFile) null else {
                    val enFirmas = entrada.startsWith("${RutasRespaldo.CARPETA_FIRMAS}/")
                    val carpeta = if (enFirmas) File(directorioArchivos, RutasRespaldo.CARPETA_FIRMAS) else directorioArchivos
                    carpeta.mkdirs()
                    val destino = File(carpeta, "imp_${marca}_${contador++}_${origen.name}")
                    origen.copyTo(destino)
                    copiados += destino
                    destino } } } }

    private fun rutasDeIngreso(bd: SupportSQLiteDatabase, id: Long): List<String> {
        val rutas = mutableListOf<String>()
        for (consulta in listOf(
            "SELECT fotoPath FROM ingreso WHERE ingresoId = ?",
            "SELECT rutaImagen FROM tabla_evidencias WHERE ingresoId = ?",
            "SELECT rutaImagen FROM firma WHERE ingresoId = ?")) {
            bd.query(consulta, arrayOf<Any>(id)).use { while (it.moveToNext()) it.getString(0)?.let(rutas::add) } }
        return rutas }

    private fun rutaEnUso(bd: SupportSQLiteDatabase, ruta: String): Boolean =
        listOf("SELECT 1 FROM ingreso WHERE fotoPath = ?", "SELECT 1 FROM tabla_evidencias WHERE rutaImagen = ?",
            "SELECT 1 FROM firma WHERE rutaImagen = ?").any { consulta ->
            bd.query(consulta, arrayOf<Any>(ruta)).use { it.moveToFirst() } }

    // Abre el respaldo preparado para leerlo. Si es de una versión anterior, antes se migra una copia con las mismas
    // migraciones que usa la app, para que sus tablas tengan las mismas columnas que las del celular.
    private fun abrirOrigen(): SQLiteDatabase {
        var archivo = File(directorioTemporal, RutasRespaldo.ARCHIVO_BD)
        val versionActual = AppDatabase.getInstance(contexto).openHelper.readableDatabase.version
        if (versionDe(archivo) < versionActual) {
            val copia = File(directorioTemporal, "migrado.db")
            archivo.copyTo(copia, overwrite = true)
            val migrada = AppDatabase.construir(contexto, copia.path)
            try { migrada.openHelper.writableDatabase } finally { migrada.close() }
            archivo = copia }
        return SQLiteDatabase.openDatabase(archivo.path, null, SQLiteDatabase.OPEN_READONLY) }

    private fun versionDe(archivo: File): Int =
        SQLiteDatabase.openDatabase(archivo.path, null, SQLiteDatabase.OPEN_READONLY).use { it.version }

    // Vehículos que ya tiene el celular, por placa normalizada.
    private fun placasLocales(bd: SupportSQLiteDatabase): Map<String, Long> =
        bd.consultar("SELECT vehiculoId, placa FROM vehiculo")
            .associate { ReglasCombinacion.normalizarPlaca(it.getAsString("placa")) to it.getAsLong("vehiculoId") }

    private class FilaIngreso(val id: Long, val vehiculoId: Long, val fecha: Long, val motivo: String, val estado: EstadoDeIngreso) {
        val clave get() = ReglasCombinacion.claveIngreso(vehiculoId, fecha, motivo) }

    private fun ingresosDe(filas: List<ContentValues>): List<FilaIngreso> = filas.map {
        FilaIngreso(it.getAsLong("ingresoId"), it.getAsLong("vehiculoId"), it.getAsLong("fecha"),
            it.getAsString("motivo").orEmpty(),
            EstadoDeIngreso(it.getAsString("estado").orEmpty(), it.getAsLong("ultimo"), it.getAsInteger("fotos"),
                it.getAsInteger("firmas") > 0)) }

    private fun insertar(bd: SupportSQLiteDatabase, tabla: String, valores: ContentValues): Long {
        val id = bd.insert(tabla, SQLiteDatabase.CONFLICT_NONE, valores)
        check(id >= 0) { "No se pudo agregar un registro a $tabla" }
        return id }

    // ---------------------------------------------------------------------------------------------------------------
    // Borrar todo
    // ---------------------------------------------------------------------------------------------------------------

    // Comprueba la contraseña del usuario de la sesión, que se pide antes de las acciones que no se pueden deshacer.
    fun contrasenaCorrecta(tecnicoId: Long, password: String): Boolean {
        val tecnico = AppDatabase.getInstance(contexto).tecnicoDao().let { kotlinx.coroutines.runBlocking { it.obtener(tecnicoId) } }
        return tecnico != null && PasswordHasher.verificar(password, tecnico.passwordHash) }

    // Borra ingresos, clientes, vehículos, fotos, firmas, reportes y copias automáticas, y todos los usuarios menos el
    // indicado, que conserva su contraseña y su código de recuperación. Los contadores de números vuelven a empezar.
    fun borrarTodoExcepto(tecnicoId: Long) {
        val base = AppDatabase.getInstance(contexto)
        base.runInTransaction {
            val bd = base.openHelper.writableDatabase
            for (tabla in listOf("tabla_evidencias", "evento_ingreso", "firma", "concepto_cotizacion", "diagnostico",
                "checklist_diagnostico", "ingreso", "vehiculo", "cliente")) { bd.execSQL("DELETE FROM $tabla") }
            bd.execSQL("DELETE FROM tecnico WHERE tecnicoId <> ?", arrayOf<Any>(tecnicoId))
            try { bd.execSQL("DELETE FROM sqlite_sequence WHERE name <> 'tecnico'") } catch (_: Exception) { } }
        val bd = base.openHelper.writableDatabase
        // Compacta la base para que los datos borrados no queden dentro del archivo.
        bd.query("PRAGMA wal_checkpoint(TRUNCATE)").use { it.moveToFirst() }
        bd.execSQL("VACUUM")
        val extensiones = setOf("jpg", "jpeg", "png", "webp")
        directorioArchivos.listFiles()?.filter { it.isFile && it.extension.lowercase() in extensiones }?.forEach { it.delete() }
        File(directorioArchivos, RutasRespaldo.CARPETA_FIRMAS).deleteRecursively()
        directorioAutomaticos.deleteRecursively()
        directorioCompartir.deleteRecursively()
        File(contexto.cacheDir, "reportes").deleteRecursively()
        preferencias.edit().remove(CLAVE_ULTIMO).remove(CLAVE_AUTOMATICA).apply() }

    // Vuelve a apuntar las fotos y firmas a la carpeta de esta app.
    private fun reubicarRutas(sqlite: SupportSQLiteDatabase) {
        sqlite.beginTransaction()
        try {
            reubicar(sqlite, "ingreso", "ingresoId", "fotoPath")
            reubicar(sqlite, "tabla_evidencias", "id", "rutaImagen")
            reubicar(sqlite, "firma", "firmaId", "rutaImagen")
            sqlite.setTransactionSuccessful()
        } finally { sqlite.endTransaction() } }

    private fun reubicar(sqlite: SupportSQLiteDatabase, tabla: String, columnaId: String, columnaRuta: String) {
        val cambios = mutableListOf<Pair<Long, String>>()
        sqlite.query("SELECT $columnaId, $columnaRuta FROM $tabla WHERE $columnaRuta IS NOT NULL").use { cursor ->
            while (cursor.moveToNext()) {
                val guardada = cursor.getString(1)
                val nueva = RutasRespaldo.reubicar(guardada, directorioArchivos)
                if (nueva != guardada) cambios += cursor.getLong(0) to nueva } }
        cambios.forEach { (id, ruta) ->
            sqlite.execSQL("UPDATE $tabla SET $columnaRuta = ? WHERE $columnaId = ?", arrayOf<Any>(ruta, id)) } }

    // Fotos y firmas que la base usa y que existen en el celular, con el nombre que llevarán en el .zip.
    private fun archivosUsados(sqlite: SupportSQLiteDatabase): Map<String, File> {
        val resultado = linkedMapOf<String, File>()
        for ((tabla, columna) in listOf("ingreso" to "fotoPath", "tabla_evidencias" to "rutaImagen", "firma" to "rutaImagen")) {
            sqlite.query("SELECT $columna FROM $tabla WHERE $columna IS NOT NULL").use { cursor ->
                while (cursor.moveToNext()) {
                    val archivo = File(cursor.getString(0))
                    val entrada = RutasRespaldo.entradaDe(archivo.path, directorioArchivos) ?: continue
                    if (archivo.isFile) resultado.putIfAbsent(entrada, archivo) } } }
        return resultado }

    // Comprueba que el archivo sea una base SQLite íntegra de AutoClimas y devuelve su versión.
    private fun verificarBase(archivo: File): Int {
        val base = try {
            SQLiteDatabase.openDatabase(archivo.path, null, SQLiteDatabase.OPEN_READONLY)
        } catch (_: Exception) { throw RespaldoException(R.string.respaldo_error_no_valido) }
        base.use { bd ->
            val integra = bd.rawQuery("PRAGMA integrity_check", null).use { it.moveToFirst() && it.getString(0) == "ok" }
            val tieneUsuarios = bd.rawQuery(
                "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = 'tecnico'", null).use { it.moveToFirst() }
            if (!integra || !tieneUsuarios) throw RespaldoException(R.string.respaldo_error_no_valido)
            return bd.version } }

    // Se copia en lugar de renombrar: un archivo que viene de la carpeta temporal conserva su grupo de "caché" y el
    // sistema podría contarlo como tal; una copia nueva nace con los permisos de la carpeta de datos.
    private fun mover(origen: File, destino: File) {
        origen.copyTo(destino, overwrite = true)
        origen.delete() }

    private fun contar(sqlite: SupportSQLiteDatabase, tabla: String): Int =
        sqlite.query("SELECT COUNT(*) FROM $tabla").use { if (it.moveToFirst()) it.getInt(0) else 0 }

    @Suppress("DEPRECATION")
    private fun nombreVersion(): String =
        try { contexto.packageManager.getPackageInfo(contexto.packageName, 0).versionName.orEmpty() } catch (_: Exception) { "" }

    private fun SupportSQLiteDatabase.consultar(sql: String, vararg args: String): List<ContentValues> =
        query(sql, args.map { it as Any }.toTypedArray()).use { it.aFilas() }

    private fun SQLiteDatabase.consultar(sql: String, vararg args: String): List<ContentValues> =
        rawQuery(sql, args.takeIf { it.isNotEmpty() }).use { it.aFilas() }

    // Cada fila como un ContentValues con todas sus columnas; sirve para copiar un registro tal cual.
    private fun Cursor.aFilas(): List<ContentValues> {
        val filas = mutableListOf<ContentValues>()
        while (moveToNext()) {
            val fila = ContentValues()
            for (i in 0 until columnCount) {
                val nombre = getColumnName(i)
                when (getType(i)) {
                    Cursor.FIELD_TYPE_NULL -> fila.putNull(nombre)
                    Cursor.FIELD_TYPE_INTEGER -> fila.put(nombre, getLong(i))
                    Cursor.FIELD_TYPE_FLOAT -> fila.put(nombre, getDouble(i))
                    Cursor.FIELD_TYPE_BLOB -> fila.put(nombre, getBlob(i))
                    else -> fila.put(nombre, getString(i)) } }
            filas += fila }
        return filas }

    companion object {
        // La migración más antigua que existe parte de la versión 5.
        const val VERSION_MINIMA = 5
        const val CARPETA_AUTOMATICOS = "respaldos_auto"
        const val MAXIMO_COPIAS = 4
        const val SEMANA_MS = 7L * 24 * 60 * 60 * 1000
        private const val CLAVE_ULTIMO = "ultimoRespaldoMs"
        private const val CLAVE_AUTOMATICA = "ultimaAutomaticaMs"

        // Un ingreso con su último movimiento (lo más reciente entre sus eventos, checklist, diagnóstico y firma),
        // su cantidad de fotos y si tiene firma.
        private const val SQL_INGRESOS = """
            SELECT i.ingresoId AS ingresoId, i.vehiculoId AS vehiculoId, i.fecha AS fecha,
                COALESCE(i.motivoIngreso, '') AS motivo, i.estado AS estado,
                MAX(i.fecha,
                    COALESCE((SELECT MAX(e.fecha) FROM evento_ingreso e WHERE e.ingresoId = i.ingresoId), 0),
                    COALESCE((SELECT c.fecha FROM checklist_diagnostico c WHERE c.ingresoId = i.ingresoId), 0),
                    COALESCE((SELECT d.fechaActualizacion FROM diagnostico d WHERE d.ingresoId = i.ingresoId), 0),
                    COALESCE((SELECT f.fecha FROM firma f WHERE f.ingresoId = i.ingresoId), 0)) AS ultimo,
                (SELECT COUNT(*) FROM tabla_evidencias v WHERE v.ingresoId = i.ingresoId) AS fotos,
                (SELECT COUNT(*) FROM firma f WHERE f.ingresoId = i.ingresoId) AS firmas
            FROM ingreso i""" } }
