package com.example.myapplication.ui.camara

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.myapplication.R
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

// Lee la placa con la cámara: el texto de cada fotograma se filtra hasta quedar solo con lo que parece una placa.
// La placa se ofrece cuando la cámara la lee igual varias veces seguidas; el técnico la confirma con el botón.
@OptIn(markerClass = [ExperimentalGetImage::class])
@Composable
fun PantallaEscanerPlaca(
    onPlacaConfirmada: (String) -> Unit,
    onVolver: () -> Unit) {
    val contexto = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var permisoConcedido by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                contexto, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }

    // Placa confirmada tras varias lecturas iguales; null mientras no se confirme ninguna.
    var placaDetectada by remember { mutableStateOf<String?>(null) }
    // La cámara no pudo abrirse: se avisa en pantalla para que el técnico capture la placa a mano.
    var errorCamara by remember { mutableStateOf(false) }

    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val estabilizador = remember { EstabilizadorPlaca() }
    // Impide analizar un fotograma nuevo mientras el anterior sigue en proceso.
    val procesando = remember { AtomicBoolean(false) }

    DisposableEffect(Unit) {
        onDispose {
            recognizer.close()
            executor.shutdown() } }

    val permisoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()) { concedido ->
        permisoConcedido = concedido }

    LaunchedEffect(Unit) {
        if (!permisoConcedido) { permisoLauncher.launch(Manifest.permission.CAMERA) } }

    // La app dibuja bajo las barras del sistema: se deja su espacio para que el título no quede debajo del reloj.
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = stringResource(R.string.scanner_title), style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(12.dp))

        if (permisoConcedido && errorCamara) {
            Text(
                text = stringResource(R.string.scanner_camera_error),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f))
        } else if (permisoConcedido) {
            AndroidView(
                modifier = Modifier.fillMaxWidth().weight(1f),
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()

                            val preview = Preview.Builder().build()
                                .also { it.surfaceProvider = previewView.surfaceProvider }

                            val analisis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            analisis.setAnalyzer(executor) { imageProxy ->
                                val mediaImage = imageProxy.image
                                if (!procesando.compareAndSet(false, true)) {
                                    imageProxy.close()
                                } else if (mediaImage == null) {
                                    procesando.set(false)
                                    imageProxy.close()
                                } else {
                                    try {
                                        val image = InputImage.fromMediaImage(
                                            mediaImage, imageProxy.imageInfo.rotationDegrees)
                                        recognizer.process(image)
                                            .addOnSuccessListener { resultado ->
                                                // Cada lectura cuenta aunque no haya placa, para reiniciar la confirmación.
                                                val confirmada = estabilizador.registrar(
                                                    ExtractorPlaca.extraer(resultado.text))
                                                if (confirmada != null) { placaDetectada = confirmada } }
                                            .addOnCompleteListener {
                                                procesando.set(false)
                                                imageProxy.close() }
                                    } catch (_: Exception) {
                                        procesando.set(false)
                                        imageProxy.close() } } }

                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analisis)
                        } catch (_: Exception) {
                            errorCamara = true } }, ContextCompat.getMainExecutor(ctx))

                    previewView })

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = placaDetectada?.let { stringResource(R.string.scanner_detected, it) }
                    ?: stringResource(R.string.scanner_aim),
                style = MaterialTheme.typography.bodyLarge)

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { placaDetectada?.let { onPlacaConfirmada(it) } },
                enabled = placaDetectada != null,
                modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.scanner_use)) }
        } else {
            Text(stringResource(R.string.scanner_permission))

            Button(onClick = { permisoLauncher.launch(Manifest.permission.CAMERA) }) {
                Text(stringResource(R.string.scanner_allow)) } }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.scanner_back)) } } }
