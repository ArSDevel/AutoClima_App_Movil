package com.example.myapplication.ui.camara

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.myapplication.R
import java.io.File

// Cámara con permiso, flash y captura; entrega la foto al llamador.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCamara(
    onNavigateToGallery: () -> Unit,
    onPhotoTaken: (File) -> Unit,
    onVolver: () -> Unit
) {
    val contexto = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val colors = MaterialTheme.colorScheme

    val executor = remember(contexto) {
        ContextCompat.getMainExecutor(contexto) }

    val previewView = remember(contexto) {
        PreviewView(contexto) }

    val preview = remember {
        Preview.Builder().build() }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build() }

    var permisoConcedido by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                contexto,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED) }

    var tieneFlash by remember { mutableStateOf(false) }
    var modoFlash by rememberSaveable { mutableStateOf(ImageCapture.FLASH_MODE_OFF) }
    var menuFlash by remember { mutableStateOf(false) }

    var camaraLista by remember { mutableStateOf(false) }
    var capturando by remember { mutableStateOf(false) }
    var pantallaActiva by remember { mutableStateOf(true) }
    var intentoConexion by remember { mutableStateOf(0) }

    var errorCamara by remember { mutableStateOf(false) }
    var errorCaptura by remember { mutableStateOf(false) }
    var fotoCapturada by remember { mutableStateOf(false) }
    var errorAjustes by remember { mutableStateOf(false) }

    val entregarFoto by rememberUpdatedState(onPhotoTaken)

    val solicitarPermiso = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        permisoConcedido = concedido }

    DisposableEffect(lifecycleOwner, contexto) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) {
                permisoConcedido = ContextCompat.checkSelfPermission(
                    contexto,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED } }

        lifecycleOwner.lifecycle.addObserver(observador)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observador) } }
    DisposableEffect(Unit) {
        pantallaActiva = true
        onDispose { pantallaActiva = false } }

    DisposableEffect(permisoConcedido, lifecycleOwner,
        intentoConexion) {
        var efectoActivo = true
        var proveedor: ProcessCameraProvider? = null

        camaraLista = false
        tieneFlash = false
        errorCamara = false

        if (permisoConcedido) {
            try {
                val futuro = ProcessCameraProvider.getInstance(contexto)

                futuro.addListener({
                    if (efectoActivo) {
                        try {
                            val actual = futuro.get()
                            proveedor = actual

                            val selector = when {
                                actual.hasCamera(
                                    CameraSelector.DEFAULT_BACK_CAMERA
                                ) -> CameraSelector.DEFAULT_BACK_CAMERA

                                actual.hasCamera(
                                    CameraSelector.DEFAULT_FRONT_CAMERA
                                ) -> CameraSelector.DEFAULT_FRONT_CAMERA

                                else -> null }

                            if (selector == null) {
                                errorCamara = true
                            } else {
                                preview.setSurfaceProvider(
                                    previewView.surfaceProvider)

                                val camara = actual.bindToLifecycle(
                                    lifecycleOwner,
                                    selector,
                                    preview,
                                    imageCapture)

                                tieneFlash = camara.cameraInfo.hasFlashUnit()
                                imageCapture.flashMode = if (tieneFlash) modoFlash else ImageCapture.FLASH_MODE_OFF
                                camaraLista = true }
                        } catch (_: Exception) {
                            errorCamara = true
                            camaraLista = false } }
                }, executor)
            } catch (_: Exception) {
                errorCamara = true } }

        onDispose {
            efectoActivo = false
            proveedor?.unbind(preview, imageCapture) } }

    LaunchedEffect(modoFlash, tieneFlash, camaraLista) {
        if (camaraLista) {
            imageCapture.flashMode = if (tieneFlash) modoFlash else ImageCapture.FLASH_MODE_OFF } }
    LaunchedEffect(capturando, camaraLista) {
        if (capturando || !camaraLista) menuFlash = false }

    // Evita salir mediante el botón del sistema durante la captura.
    BackHandler(enabled = capturando) {}

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.camera_screen_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = onVolver,
                        enabled = !capturando
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(
                                R.string.camera_back)) } },
                actions = {
                    Box {
                        TextButton(
                            onClick = { menuFlash = true },
                            enabled = camaraLista && tieneFlash && !capturando
                        ) {
                            val efectivo = if (tieneFlash) modoFlash else ImageCapture.FLASH_MODE_OFF
                            Icon(
                                imageVector = when (efectivo) {
                                    ImageCapture.FLASH_MODE_ON -> Icons.Default.FlashOn
                                    ImageCapture.FLASH_MODE_AUTO -> Icons.Default.FlashAuto
                                    else -> Icons.Default.FlashOff },
                                contentDescription = null,
                                tint = colors.onTertiary)
                            Text(stringResource(if (!camaraLista) R.string.camera_flash_label
                            else if (!tieneFlash) R.string.camera_flash_unavailable
                            else etiquetaFlash(efectivo)), color = colors.onTertiary) }
                        DropdownMenu(expanded = menuFlash, onDismissRequest = { menuFlash = false }) {
                            listOf(ImageCapture.FLASH_MODE_OFF, ImageCapture.FLASH_MODE_AUTO,
                                ImageCapture.FLASH_MODE_ON).forEach { modo ->
                                DropdownMenuItem(
                                    text = { Text(stringResource(etiquetaFlash(modo))) },
                                    onClick = {
                                        if (camaraLista && tieneFlash && !capturando) {
                                            imageCapture.flashMode = modo
                                            modoFlash = modo }
                                        menuFlash = false }) } } } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.tertiary,
                    titleContentColor = colors.onTertiary,
                    navigationIconContentColor = colors.onTertiary)) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    !permisoConcedido -> {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = colors.primary)

                            Text(
                                text = stringResource(
                                    R.string.camera_permission_message),
                                textAlign = TextAlign.Center)

                            Button(
                                onClick = {
                                    solicitarPermiso.launch(
                                        Manifest.permission.CAMERA) }
                            ) {
                                Text(
                                    stringResource(
                                        R.string.camera_allow_permission)) }

                            TextButton(
                                onClick = {
                                    errorAjustes = false

                                    try {
                                        contexto.startActivity(
                                            Intent(
                                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                Uri.parse(
                                                    "package:${contexto.packageName}")))
                                    } catch (_: Exception) {
                                        errorAjustes = true } }
                            ) {
                                Text(
                                    stringResource(
                                        R.string.camera_open_settings)) }

                            if (errorAjustes) {
                                Text(
                                    text = stringResource(
                                        R.string.camera_settings_error),
                                    color = colors.error,
                                    textAlign = TextAlign.Center) } } }

                    errorCamara -> {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.camera_connection_error),
                                textAlign = TextAlign.Center)

                            Button(
                                onClick = { intentoConexion++ }
                            ) {
                                Text(
                                    stringResource(R.string.camera_retry)) } } }

                    else -> {
                        AndroidView(
                            factory = { previewView },
                            modifier = Modifier.fillMaxSize())

                        if (!camaraLista) {
                            CircularProgressIndicator() } } } }

            // Describe el resultado de la captura.
            // La confirmación del guardado en Room corresponde al ViewModel.
            when {
                capturando -> {
                    Text(
                        text = stringResource(R.string.camera_capturing),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        textAlign = TextAlign.Center) }

                errorCaptura -> {
                    Text(
                        text = stringResource(
                            R.string.camera_capture_error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        color = colors.error,
                        textAlign = TextAlign.Center) }

                fotoCapturada -> {
                    Text(
                        text = stringResource(R.string.camera_captured),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        textAlign = TextAlign.Center) } }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onNavigateToGallery,
                    enabled = !capturando,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Collections,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(20.dp))

                    Text(stringResource(R.string.camera_gallery)) }

                Button(
                    enabled = permisoConcedido &&
                            camaraLista &&
                            !capturando,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        capturando = true
                        errorCaptura = false
                        fotoCapturada = false

                        var archivo: File? = null

                        try {
                            val destino = File.createTempFile(
                                "evidencia_",
                                ".jpg",
                                contexto.filesDir)

                            archivo = destino

                            previewView.display?.let { display ->
                                imageCapture.targetRotation = display.rotation }

                            val opciones = ImageCapture.OutputFileOptions
                                .Builder(destino)
                                .build()

                            imageCapture.takePicture(
                                opciones,
                                executor,
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(
                                        output: ImageCapture.OutputFileResults
                                    ) {
                                        if (!pantallaActiva) {
                                            destino.delete()
                                            return }

                                        capturando = false
                                        fotoCapturada = true
                                        entregarFoto(destino) }

                                    override fun onError(
                                        exception: ImageCaptureException
                                    ) {
                                        destino.delete()

                                        if (pantallaActiva) {
                                            capturando = false
                                            errorCaptura = true } } })
                        } catch (_: Exception) {
                            archivo?.delete()
                            capturando = false
                            errorCaptura = true } }
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(20.dp))

                    Text(stringResource(R.string.camera_capture)) } } } } }

// Texto del modo de flash seleccionado.
@androidx.annotation.StringRes
private fun etiquetaFlash(modo: Int): Int = when (modo) {
    ImageCapture.FLASH_MODE_ON -> R.string.camera_flash_on
    ImageCapture.FLASH_MODE_AUTO -> R.string.camera_flash_auto
    else -> R.string.camera_flash_off }
