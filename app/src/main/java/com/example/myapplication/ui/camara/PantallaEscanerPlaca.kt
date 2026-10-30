
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
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@OptIn(markerClass = [ExperimentalGetImage::class])
@Composable
fun PantallaEscanerPlaca(
    onPlacaConfirmada: (String) -> Unit,
    onVolver: () -> Unit
) {
    val contexto = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var permisoConcedido by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                contexto,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var textoDetectado by remember { mutableStateOf("") }

    val recognizer = remember {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    val executor = remember {
        Executors.newSingleThreadExecutor()
    }

    val procesando = remember {
        AtomicBoolean(false)
    }

    DisposableEffect(Unit) {
        onDispose {
            recognizer.close()
            executor.shutdown()
        }
    }

    val permisoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        permisoConcedido = concedido
    }

    LaunchedEffect(Unit) {
        if (!permisoConcedido) {
            permisoLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Escanear matrícula",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (permisoConcedido) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                factory = { ctx ->
                    val previewView = PreviewView(ctx)

                    val cameraProviderFuture =
                        ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()

                            val preview = Preview.Builder()
                                .build()
                                .also {
                                    it.surfaceProvider = previewView.surfaceProvider
                                }

                            val analisis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(
                                    ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                                )
                                .build()

                            analisis.setAnalyzer(executor) { imageProxy ->
                                if (!procesando.compareAndSet(false, true)) {
                                    imageProxy.close()
                                } else {
                                    val mediaImage = imageProxy.image

                                    if (mediaImage == null) {
                                        procesando.set(false)
                                        imageProxy.close()
                                    } else {
                                        try {
                                            val image = InputImage.fromMediaImage(
                                                mediaImage,
                                                imageProxy.imageInfo.rotationDegrees
                                            )

                                            recognizer.process(image)
                                                .addOnSuccessListener { resultado ->
                                                    val texto = resultado.text
                                                        .uppercase()
                                                        .replace("\n", " ")
                                                        .trim()

                                                    if (texto.isNotBlank()) {
                                                        textoDetectado = texto
                                                    }
                                                }
                                                .addOnCompleteListener {
                                                    procesando.set(false)
                                                    imageProxy.close()
                                                }
                                        } catch (e: Exception) {
                                            procesando.set(false)
                                            imageProxy.close()
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            }

                            cameraProvider.unbindAll()

                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                analisis
                            )

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (textoDetectado.isBlank()) {
                    "Apunta la cámara hacia la matrícula"
                } else {
                    "Texto detectado: $textoDetectado"
                },
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val placa = textoDetectado
                        .replace(" ", "")
                        .filter { it.isLetterOrDigit() }

                    if (placa.isNotBlank()) {
                        onPlacaConfirmada(placa)
                    }
                },
                enabled = textoDetectado.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Usar matrícula")
            }

        } else {
            Text("Se necesita permiso para utilizar la cámara.")

            Button(
                onClick = {
                    permisoLauncher.launch(Manifest.permission.CAMERA)
                }
            ) {
                Text("Dar permiso")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}
