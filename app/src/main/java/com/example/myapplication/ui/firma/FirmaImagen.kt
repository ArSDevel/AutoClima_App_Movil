package com.example.myapplication.ui.firma

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.ui.geometry.Offset
import java.io.ByteArrayOutputStream

// Proporción ancho/alto del recuadro de firma en pantalla y de la imagen guardada.
const val ASPECTO_FIRMA = 1.25f

// Dibuja los trazos en tinta oscura sobre fondo blanco y devuelve un PNG.
// Así la firma se ve igual en el reporte, sin importar el tema de la aplicación.
// Los puntos llegan en proporción (de 0 a 1); 1000 por 800 mantiene la proporción ASPECTO_FIRMA del recuadro.
fun renderizarFirma(trazos: List<List<Offset>>, ancho: Int = 1000, alto: Int = 800): ByteArray {
    val mapa = Bitmap.createBitmap(ancho, alto, Bitmap.Config.ARGB_8888)
    val lienzo = Canvas(mapa)
    lienzo.drawColor(Color.WHITE)
    val pincel = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0F172A")
        style = Paint.Style.STROKE
        strokeWidth = 14f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND }
    // Un trazo de un solo punto no dibuja nada, por eso se descarta.
    trazos.filter { it.size > 1 }.forEach { puntos ->
        val camino = Path()
        camino.moveTo(puntos.first().x * ancho, puntos.first().y * alto)
        puntos.drop(1).forEach { camino.lineTo(it.x * ancho, it.y * alto) }
        lienzo.drawPath(camino, pincel) }
    val salida = ByteArrayOutputStream()
    mapa.compress(Bitmap.CompressFormat.PNG, 100, salida)
    mapa.recycle()
    return salida.toByteArray() }
