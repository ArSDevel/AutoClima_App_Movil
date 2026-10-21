package com.example.myapplication.data.reporte

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.media.ExifInterface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import androidx.core.content.res.ResourcesCompat
import com.example.myapplication.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.OutputStream
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

// Crea los PDF del reporte con el servicio de PDF de Android: reporte completo,
// cotización breve y acta de entrega. El diseño sigue el que se hizo en Figma.
class GeneradorReportePdf(private val contexto: Context) {

    private val ancho = 595
    private val alto = 842
    private val margen = 32f
    private val anchoUtil = ancho - 2 * margen
    private val altoBanda = 90f
    private val limiteInferior = alto - 52f

    private val azul = Color.parseColor("#1E3A8A")
    private val azulTexto = Color.parseColor("#1D4ED8")
    private val azulClaro = Color.parseColor("#DBEAFE")
    private val suave = Color.parseColor("#F1F5FB")
    private val borde = Color.parseColor("#C9D6EA")
    private val linea = Color.parseColor("#E2E8F0")
    private val gris = Color.parseColor("#475569")
    private val tinta = Color.parseColor("#0F172A")
    private val verde = Color.parseColor("#15803D")
    private val rojo = Color.parseColor("#B91C32")
    private val ambar = Color.parseColor("#946200")

    private val montserrat: Typeface = ResourcesCompat.getFont(contexto, R.font.montserrat_bold) ?: Typeface.DEFAULT_BOLD
    private val cuerpo: Typeface = ResourcesCompat.getFont(contexto, R.font.dm_sans) ?: Typeface.DEFAULT
    private val negrita: Typeface = Typeface.create(cuerpo, Typeface.BOLD)

    private fun pincel(tamano: Float, color: Int, fuente: Typeface = cuerpo): TextPaint {
        return TextPaint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; textSize = tamano; typeface = fuente } }

    private fun relleno(color: Int): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }

    private fun trazo(color: Int, grosor: Float): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color; style = Paint.Style.STROKE; strokeWidth = grosor
        strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND }

    private val pTexto = pincel(11f, tinta)
    private val pNegrita = pincel(11f, tinta, negrita)
    private val pEtiqueta = pincel(10f, gris)
    private val pSuave = pincel(10f, gris)
    private val pSeccion = pincel(11f, azulTexto, negrita).apply { letterSpacing = 0.08f }
    private val pEncabezadoTabla = pincel(9f, gris, negrita).apply { letterSpacing = 0.06f }

    // Escribe el PDF en destino y devuelve el mismo archivo.
    // Se dibuja dos veces: la primera solo cuenta las páginas para poder escribir "Página 1 de 2".
    suspend fun generar(datos: DatosReporte, tipo: TipoReporte, destino: File): File = withContext(Dispatchers.IO) {
        destino.parentFile?.mkdirs()
        val paginas = dibujar(datos, tipo, 0, null)
        destino.outputStream().use { dibujar(datos, tipo, paginas, it) }
        destino }

    // Dibuja el documento completo y devuelve cuántas páginas ocupó.
    private fun dibujar(datos: DatosReporte, tipo: TipoReporte, totalPaginas: Int, salida: OutputStream?): Int {
        val documento = PdfDocument()
        try {
            val pagina = Pagina(documento, datos, tipo, totalPaginas)
            when (tipo) {
                TipoReporte.COMPLETO -> pagina.completo()
                TipoReporte.COTIZACION -> pagina.cotizacion()
                TipoReporte.ACTA -> pagina.acta() }
            val total = pagina.terminar()
            if (salida != null) documento.writeTo(salida)
            return total
        } finally {
            documento.close() } }

    private fun fecha(milisegundos: Long): String {
        return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.forLanguageTag("es-MX"))
            .format(Date(milisegundos)) }

    // Ajusta un texto de una sola línea al espacio disponible.
    private fun recortar(texto: String, pincel: TextPaint, disponible: Float): String {
        return TextUtils.ellipsize(texto, pincel, disponible, TextUtils.TruncateAt.END).toString() }

    private fun capa(texto: String, pincel: TextPaint, anchoCapa: Int, maxLineas: Int = Int.MAX_VALUE): StaticLayout {
        val constructor = StaticLayout.Builder.obtain(texto, 0, texto.length, pincel, anchoCapa)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
        if (maxLineas != Int.MAX_VALUE) constructor.setMaxLines(maxLineas).setEllipsize(TextUtils.TruncateAt.END)
        return constructor.build() }

    // Imagen reducida y girada según su orientación, para no gastar memoria con la foto completa.
    private fun cargarImagen(archivo: File, anchoMaximo: Int): Bitmap? {
        val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(archivo.absolutePath, medidas)
        if (medidas.outWidth <= 0) return null
        var reduccion = 1
        while (medidas.outWidth / (reduccion * 2) >= anchoMaximo) reduccion *= 2
        val mapa = BitmapFactory.decodeFile(archivo.absolutePath,
            BitmapFactory.Options().apply { inSampleSize = reduccion }) ?: return null
        val giro = try {
            when (ExifInterface(archivo.absolutePath).getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f }
        } catch (_: Exception) { 0f }
        if (giro == 0f) return mapa
        val girado = Bitmap.createBitmap(mapa, 0, 0, mapa.width, mapa.height, Matrix().apply { postRotate(giro) }, true)
        if (girado != mapa) mapa.recycle()
        return girado }

    // Controla la página actual y abre otra cuando el contenido ya no cabe.
    private inner class Pagina(
        private val documento: PdfDocument,
        private val d: DatosReporte,
        private val tipo: TipoReporte,
        private val total: Int
    ) {
        private var numero = 0
        private lateinit var pagina: PdfDocument.Page
        private lateinit var lienzo: Canvas
        private var y = 0f
        private val logo: Bitmap? = BitmapFactory.decodeResource(contexto.resources, R.drawable.logo_autoclimas,
            BitmapFactory.Options().apply { inSampleSize = 4 })
        private val folio = String.format(Locale.ROOT, "%04d", d.ingresoId)

        init { abrir() }

        // ---- Estructura de cada tipo de documento ----

        fun completo() {
            datosDelIngreso()
            seccion(R.string.pdf_findings); hallazgos()
            if (d.checklist.isNotEmpty()) { seccion(R.string.pdf_checklist, 60f); checklist() }
            if (d.observaciones.isNotBlank()) { seccion(R.string.pdf_notes); cajaDeTexto(d.observaciones) }
            seccion(R.string.pdf_quote, 70f); cotizacionTabla()
            if (d.fotos.isNotEmpty()) { seccion(R.string.pdf_photos, 120f); fotos() } }

        fun cotizacion() {
            seccion(R.string.pdf_data)
            cuadricula(
                listOf(R.string.pdf_customer to d.cliente, R.string.pdf_phone to d.telefono),
                listOf(R.string.pdf_vehicle to listOfNotNull(d.modelo.takeIf { it.isNotBlank() },
                    d.color?.takeIf { it.isNotBlank() }).joinToString(" · "), R.string.pdf_plate to d.placa))
            seccion(R.string.pdf_found); hallazgos()
            seccion(R.string.pdf_quote, 70f); cotizacionTabla() }

        fun acta() {
            datosDelIngreso()
            val entrega = d.entrega
            if (entrega != null) {
                seccion(R.string.pdf_steps); pasos(entrega.requisitos.checklist, entrega.requisitos.diagnostico,
                    entrega.requisitos.firma) }
            if (d.conceptos.isNotEmpty()) { seccion(R.string.pdf_works); cotizacionTabla() }
            if (entrega?.nota != null) { seccion(R.string.pdf_delivery_note); cajaDeTexto(entrega.nota) }
            seccion(R.string.pdf_client_signature, 150f); firma(contexto.getString(R.string.pdf_acta_sign_text)) }

        // Cierra la última página y devuelve cuántas se escribieron.
        fun terminar(): Int {
            cerrar()
            logo?.recycle()
            return numero }

        // ---- Páginas ----

        private fun abrir() {
            numero += 1
            pagina = documento.startPage(PdfDocument.PageInfo.Builder(ancho, alto, numero).create())
            lienzo = pagina.canvas
            banda()
            y = altoBanda + 16f }

        private fun cerrar() {
            pie()
            documento.finishPage(pagina) }

        private fun asegurar(altoNecesario: Float) {
            if (y + altoNecesario > limiteInferior) { cerrar(); abrir() } }

        // Franja azul con el logo, el título del documento y la placa.
        private fun banda() {
            lienzo.drawRect(0f, 0f, ancho.toFloat(), altoBanda, relleno(azul))
            lienzo.drawRoundRect(RectF(margen, 16f, margen + 76f, 74f), 10f, 10f, relleno(Color.WHITE))
            logo?.let {
                val altoLogo = 46f
                val anchoLogo = minOf(66f, altoLogo * it.width / it.height)
                val x = margen + (76f - anchoLogo) / 2
                lienzo.drawBitmap(it, null, RectF(x, 22f, x + anchoLogo, 22f + altoLogo), Paint(Paint.FILTER_BITMAP_FLAG)) }
            val textoX = margen + 76f + 16f
            val titulo = contexto.getString(when (tipo) {
                TipoReporte.COMPLETO -> R.string.pdf_title_full
                TipoReporte.COTIZACION -> R.string.pdf_title_quote
                TipoReporte.ACTA -> R.string.pdf_title_delivery })
            val subtitulo = when {
                numero > 1 -> contexto.getString(R.string.pdf_folio_cont, folio)
                tipo == TipoReporte.ACTA && d.entrega != null ->
                    contexto.getString(R.string.pdf_folio_delivery, folio, fecha(d.entrega.fecha))
                else -> contexto.getString(R.string.pdf_folio, folio, fecha(System.currentTimeMillis())) }
            lienzo.drawText(titulo, textoX, 42f, pincel(18f, Color.WHITE, montserrat))
            lienzo.drawText(subtitulo, textoX, 60f, pincel(11f, azulClaro))
            val pPlaca = pincel(17f, Color.WHITE, montserrat)
            val anchoPlaca = pPlaca.measureText(d.placa) + 24f
            lienzo.drawRoundRect(RectF(ancho - margen - anchoPlaca, 30f, ancho - margen, 60f), 8f, 8f,
                relleno(Color.argb(60, 255, 255, 255)))
            lienzo.drawText(d.placa, ancho - margen - anchoPlaca + 12f, 51f, pPlaca) }

        // Técnico a la izquierda y número de página a la derecha.
        private fun pie() {
            lienzo.drawLine(margen, alto - 40f, ancho - margen, alto - 40f, trazo(linea, 1f))
            val izquierda = d.tecnicoNombre?.let {
                contexto.getString(if (tipo == TipoReporte.ACTA) R.string.pdf_footer_delivered_by
                else R.string.pdf_footer_tech, it) }
            if (izquierda != null) lienzo.drawText(recortar(izquierda, pSuave, anchoUtil - 120f), margen, alto - 24f, pSuave)
            val derecha = contexto.getString(R.string.pdf_footer_page, numero, if (total > 0) total else numero)
            lienzo.drawText(derecha, ancho - margen - pSuave.measureText(derecha), alto - 24f, pSuave) }

        // ---- Bloques ----

        // El título no se queda solo al final de una página: se reserva espacio para el inicio del contenido.
        private fun seccion(titulo: Int, minimoContenido: Float = 24f) {
            asegurar(24f + minimoContenido)
            y += 6f
            lienzo.drawText(contexto.getString(titulo).uppercase(Locale.ROOT), margen, y + 10f, pSeccion)
            y += 18f }

        private fun caja(alturaCaja: Float): Float {
            asegurar(alturaCaja + 8f)
            val arriba = y
            lienzo.drawRoundRect(RectF(margen, arriba, ancho - margen, arriba + alturaCaja), 10f, 10f, relleno(suave))
            return arriba }

        // Dos columnas de pares etiqueta-valor sobre fondo suave.
        private fun cuadricula(izquierda: List<Pair<Int, String?>>, derecha: List<Pair<Int, String?>>) {
            val columnaIzq = izquierda.filter { !it.second.isNullOrBlank() }
            val columnaDer = derecha.filter { !it.second.isNullOrBlank() }
            val filas = max(columnaIzq.size, columnaDer.size)
            val altura = 24f + filas * 16f
            val arriba = caja(altura)
            val anchoColumna = (anchoUtil - 24f - 16f) / 2
            fun columna(items: List<Pair<Int, String?>>, x: Float) {
                items.forEachIndexed { i, (etiqueta, valor) ->
                    val base = arriba + 12f + i * 16f + 10f
                    lienzo.drawText(contexto.getString(etiqueta), x, base, pEtiqueta)
                    lienzo.drawText(recortar(valor.orEmpty(), pTexto, anchoColumna - 70f), x + 70f, base, pTexto) } }
            columna(columnaIzq, margen + 12f)
            columna(columnaDer, margen + 12f + anchoColumna + 16f)
            y = arriba + altura + 8f }

        // Fila de ancho completo, con el valor en varias líneas si hace falta.
        private fun filaLarga(etiqueta: Int, valor: String?) {
            if (valor.isNullOrBlank()) return
            val texto = capa(valor, pTexto, (anchoUtil - 90f).toInt())
            asegurar(texto.height + 4f)
            lienzo.drawText(contexto.getString(etiqueta), margen, y + 10f, pEtiqueta)
            dibujarCapa(texto, margen + 90f, y)
            y += texto.height + 4f }

        private fun datosDelIngreso() {
            seccion(R.string.pdf_data)
            cuadricula(
                listOf(R.string.pdf_plate to d.placa, R.string.pdf_model to d.modelo,
                    R.string.pdf_color to d.color, R.string.pdf_serial to d.serie),
                listOf(R.string.pdf_customer to d.cliente, R.string.pdf_phone to d.telefono,
                    R.string.pdf_email to d.correo, R.string.pdf_entry to fecha(d.fechaEntrada)))
            filaLarga(R.string.pdf_reason, d.motivo)
            filaLarga(R.string.pdf_condition, d.condicionInicial) }

        private fun etiquetaCapsula(x: Float, arriba: Float, texto: String, fondo: Int, color: Int) {
            lienzo.drawRoundRect(RectF(x, arriba, x + 70f, arriba + 14f), 7f, 7f, relleno(fondo))
            val p = pincel(8.5f, color, negrita)
            lienzo.drawText(texto, x + (70f - p.measureText(texto)) / 2, arriba + 10.5f, p) }

        private fun hallazgos() {
            if (d.hallazgos.isEmpty()) {
                fila(R.string.pdf_badge_ok, contexto.getString(R.string.pdf_no_findings), Color.parseColor("#DCFCE7"), verde)
            } else {
                d.hallazgos.forEach { h ->
                    if (h.grave) fila(R.string.pdf_badge_fail, h.texto, Color.parseColor("#FDECEE"), rojo)
                    else fila(R.string.pdf_badge_warn, h.texto, Color.parseColor("#FEF3C7"), ambar) } }
            if (d.comentariosChecklist.isNotBlank()) parrafo(d.comentariosChecklist, pSuave) }

        private fun fila(etiqueta: Int, texto: String, fondo: Int, color: Int) {
            val capa = capa(texto, pTexto, (anchoUtil - 80f).toInt())
            val altura = max(capa.height.toFloat(), 16f) + 4f
            asegurar(altura)
            etiquetaCapsula(margen, y + 1f, contexto.getString(etiqueta), fondo, color)
            dibujarCapa(capa, margen + 80f, y)
            y += altura }

        private fun parrafo(texto: String, pincel: TextPaint) {
            val capa = capa(texto, pincel, anchoUtil.toInt())
            asegurar(capa.height + 6f)
            dibujarCapa(capa, margen, y)
            y += capa.height + 6f }

        private fun marca(x: Float, arriba: Float, ok: Boolean) {
            val camino = Path()
            if (ok) {
                camino.moveTo(x, arriba + 6.5f); camino.lineTo(x + 3.6f, arriba + 10f); camino.lineTo(x + 10.5f, arriba + 2.5f)
            } else {
                camino.moveTo(x + 1f, arriba + 1.5f); camino.lineTo(x + 9.5f, arriba + 10f)
                camino.moveTo(x + 9.5f, arriba + 1.5f); camino.lineTo(x + 1f, arriba + 10f) }
            lienzo.drawPath(camino, trazo(if (ok) verde else rojo, 1.8f)) }

        // Todos los puntos de la revisión, con su resultado.
        private fun checklist() {
            val filas = (d.checklist.size + 1) / 2
            val altura = 24f + filas * 17f
            val arriba = caja(altura)
            d.checklist.forEachIndexed { i, item ->
                val columna = if (i < filas) 0 else 1
                val fila = if (i < filas) i else i - filas
                val x = margen + 12f + columna * (anchoUtil / 2)
                val base = arriba + 12f + fila * 17f
                marca(x, base, item.ok)
                lienzo.drawText(recortar(item.texto, pTexto, anchoUtil / 2 - 40f), x + 18f, base + 9.5f, pTexto) }
            y = arriba + altura + 8f }

        private fun cajaDeTexto(texto: String) {
            val capa = capa(texto, pTexto, (anchoUtil - 24f).toInt())
            val arriba = caja(capa.height + 24f)
            dibujarCapa(capa, margen + 12f, arriba + 12f)
            y = arriba + capa.height + 24f + 8f }

        private fun pasos(checklist: Boolean, diagnostico: Boolean, firma: Boolean) {
            val arriba = caja(30f)
            val items = listOf(R.string.entrega_checklist to checklist, R.string.entrega_diagnosis to diagnostico,
                R.string.entrega_signature to firma)
            items.forEachIndexed { i, (texto, ok) ->
                val x = margen + 12f + i * (anchoUtil / 3)
                marca(x, arriba + 9f, ok)
                lienzo.drawText(recortar(contexto.getString(texto), pTexto, anchoUtil / 3 - 34f), x + 18f, arriba + 18.5f, pTexto) }
            y = arriba + 30f + 8f }

        private fun cotizacionTabla() {
            if (d.conceptos.isEmpty()) { parrafo(contexto.getString(R.string.pdf_quote_pending), pTexto); return }
            asegurar(24f)
            lienzo.drawRoundRect(RectF(margen, y, ancho - margen, y + 20f), 6f, 6f, relleno(suave))
            lienzo.drawText(contexto.getString(R.string.pdf_concept).uppercase(Locale.ROOT), margen + 10f, y + 13f, pEncabezadoTabla)
            val textoImporte = contexto.getString(R.string.pdf_amount).uppercase(Locale.ROOT)
            lienzo.drawText(textoImporte, ancho - margen - 10f - pEncabezadoTabla.measureText(textoImporte), y + 13f, pEncabezadoTabla)
            y += 20f
            d.conceptos.forEach { concepto ->
                val capa = capa(concepto.descripcion, pTexto, (anchoUtil - 130f).toInt())
                val altura = capa.height + 14f
                asegurar(altura)
                dibujarCapa(capa, margen + 10f, y + 7f)
                val importe = FormatoReporte.pesos(concepto.importeCentavos)
                lienzo.drawText(importe, ancho - margen - 10f - pNegrita.measureText(importe), y + 18f, pNegrita)
                lienzo.drawLine(margen, y + altura, ancho - margen, y + altura, trazo(linea, 1f))
                y += altura }
            val totalCentavos = d.totalCentavos ?: return
            asegurar(40f)
            y += 8f
            val izquierda = ancho - margen - 240f
            lienzo.drawRoundRect(RectF(izquierda, y, ancho - margen, y + 26f), 8f, 8f, relleno(azulClaro))
            lienzo.drawText(contexto.getString(R.string.pdf_total), izquierda + 10f, y + 17f, pincel(12f, azulTexto, negrita))
            val pMonto = pincel(15f, azulTexto, montserrat)
            val monto = FormatoReporte.pesos(totalCentavos)
            lienzo.drawText(monto, ancho - margen - 10f - pMonto.measureText(monto), y + 18f, pMonto)
            y += 34f }

        // Cuadrícula de tres fotografías por fila, con su descripción debajo.
        private fun fotos() {
            val anchoCelda = (anchoUtil - 16f) / 3
            val altoImagen = 84f
            d.fotos.take(MAXIMO_FOTOS).chunked(3).forEach { fila ->
                val pies = fila.mapIndexed { i, foto ->
                    capa(foto.pie.ifBlank { contexto.getString(R.string.pdf_photo_n, i + 1) }, pSuave, anchoCelda.toInt(), 2) }
                val altura = altoImagen + 4f + (pies.maxOfOrNull { it.height } ?: 0) + 8f
                asegurar(altura)
                fila.forEachIndexed { i, foto ->
                    val x = margen + i * (anchoCelda + 8f)
                    val destino = RectF(x, y, x + anchoCelda, y + altoImagen)
                    val mapa = cargarImagen(foto.archivo, (anchoCelda * 3).toInt())
                    if (mapa != null) {
                        val relacion = anchoCelda / altoImagen
                        val origen = if (mapa.width.toFloat() / mapa.height > relacion) {
                            val anchoOrigen = (mapa.height * relacion).toInt()
                            Rect((mapa.width - anchoOrigen) / 2, 0, (mapa.width + anchoOrigen) / 2, mapa.height)
                        } else {
                            val altoOrigen = (mapa.width / relacion).toInt()
                            Rect(0, (mapa.height - altoOrigen) / 2, mapa.width, (mapa.height + altoOrigen) / 2) }
                        lienzo.save()
                        lienzo.clipPath(Path().apply { addRoundRect(destino, 8f, 8f, Path.Direction.CW) })
                        lienzo.drawBitmap(mapa, origen, destino, Paint(Paint.FILTER_BITMAP_FLAG))
                        lienzo.restore()
                        mapa.recycle()
                    } else {
                        lienzo.drawRoundRect(destino, 8f, 8f, relleno(suave)) }
                    dibujarCapa(pies[i], x, y + altoImagen + 4f) }
                y += altura } }

        // Recuadro de firma del cliente; solo se usa en el acta de entrega, que ya está firmada.
        private fun firma(texto: String) {
            val capaTexto = capa(texto, pNegrita, (anchoUtil - 28f).toInt())
            val altoFirma = 84f
            val altura = 14f + capaTexto.height + 8f + altoFirma + 8f + 14f + (if (d.firma != null) 14f else 0f) + 14f
            asegurar(altura + 8f)
            val arriba = y
            lienzo.drawRoundRect(RectF(margen, arriba, ancho - margen, arriba + altura), 10f, 10f, trazo(borde, 1f))
            dibujarCapa(capaTexto, margen + 14f, arriba + 14f)
            val zonaFirma = arriba + 14f + capaTexto.height + 8f
            d.firma?.let { f ->
                val mapa = cargarImagen(f.archivo, 800)
                if (mapa != null) {
                    val anchoImagen = altoFirma * mapa.width / mapa.height
                    lienzo.drawBitmap(mapa, null, RectF(margen + 14f, zonaFirma, margen + 14f + anchoImagen, zonaFirma + altoFirma),
                        Paint(Paint.FILTER_BITMAP_FLAG))
                    mapa.recycle() } }
            lienzo.drawLine(margen + 14f, zonaFirma + altoFirma + 2f, margen + 14f + 300f, zonaFirma + altoFirma + 2f, trazo(gris, 1f))
            val baseNombre = zonaFirma + altoFirma + 8f + 12f
            lienzo.drawText(d.cliente, margen + 14f, baseNombre, pNegrita)
            d.firma?.let { lienzo.drawText(contexto.getString(R.string.pdf_signed_on, fecha(it.fecha)), margen + 14f, baseNombre + 14f, pSuave) }
            y = arriba + altura + 8f }

        private fun dibujarCapa(capa: StaticLayout, x: Float, arriba: Float) {
            lienzo.save()
            lienzo.translate(x, arriba)
            capa.draw(lienzo)
            lienzo.restore() } }

    private companion object {
        const val MAXIMO_FOTOS = 9 } }
