package com.example.myapplication.ui.camara

import com.example.myapplication.ui.components.EncabezadoAutoClimas
import com.example.myapplication.ui.components.DialogoAviso
import com.example.myapplication.ui.components.TipoAviso
import com.example.myapplication.ui.components.BarraAccionInferior
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.ui.theme.AutoClimasGradients
import java.io.File
import com.example.myapplication.ui.camara.components.GaleriaIngresoConectada

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvidenciasIngresoScreen(
    viewModel: CamaraIngresoViewModel,
    tecnicoId: Long,
    onVolver: () -> Unit
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme
    val snackbar = remember { SnackbarHostState() }

    var camaraAbierta by rememberSaveable {
        mutableStateOf(false)
    }

    var confirmarDescarte by rememberSaveable {
        mutableStateOf(false)
    }

    // Identifica si el diálogo se abrió para salir de la pantalla.
    var salidaSolicitada by rememberSaveable {
        mutableStateOf(false)
    }

    // Se activa únicamente después de confirmar el descarte.
    var salirTrasDescartar by rememberSaveable {
        mutableStateOf(false)
    }

    var fotoAmpliada by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    val mensajeGuardado = stringResource(
        R.string.evidence_saved
    )

    LaunchedEffect(estado.guardadoExitoso) {
        if (estado.guardadoExitoso) {
            viewModel.consumirGuardadoExitoso()
            snackbar.showSnackbar(mensajeGuardado)
        }
    }

    // Espera a que termine el descarte antes de salir.
    // Si falla, conserva la pantalla y muestra el error.
    LaunchedEffect(
        salirTrasDescartar,
        estado.guardando,
        estado.rutaPendiente,
        estado.errorGuardado
    ) {
        if (salirTrasDescartar && !estado.guardando) {
            when {
                estado.rutaPendiente == null -> {
                    salirTrasDescartar = false
                    onVolver()
                }

                estado.errorGuardado != null -> {
                    salirTrasDescartar = false
                }
            }
        }
    }

    fun solicitarSalida() {
        if (estado.guardando) return

        when {
            camaraAbierta -> {
                camaraAbierta = false
            }

            estado.rutaPendiente != null -> {
                salidaSolicitada = true
                confirmarDescarte = true
            }

            else -> onVolver()
        }
    }

    BackHandler {
        solicitarSalida()
    }

    // Reutiliza la cámara y devuelve la captura para revisarla.
    if (camaraAbierta) {
        PantallaCamara(
            onNavigateToGallery = {
                camaraAbierta = false
            },
            onPhotoTaken = { archivo ->
                viewModel.recibirFoto(archivo)
                camaraAbierta = false
            },
            onVolver = {
                camaraAbierta = false
            }
        )

        return
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbar)
        },
        topBar = {
            EncabezadoAutoClimas(
                titulo = stringResource(R.string.evidence_screen_title),
                subtitulo = estado.ingreso?.placa,
                onVolver = { solicitarSalida() },
                habilitado = !estado.guardando
            )
        },
        bottomBar = {
            // Tomar fotografía es la acción principal de esta pantalla.
            if (estado.permiteAgregar && estado.rutaPendiente == null) {
                BarraAccionInferior {
                    Button(
                        onClick = { camaraAbierta = true },
                        enabled = estado.puedeCapturar,
                        modifier = Modifier.weight(1f).heightIn(min = 56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null)
                        Text(
                            text = "  " + stringResource(R.string.evidence_take_photo),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 900.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (estado.cargando || estado.errorCarga != null || estado.ingresoNoEncontrado || !estado.permiteAgregar) item(key = "expediente") {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(12.dp)
                        ) {
                            when {
                                estado.cargando -> {
                                    CircularProgressIndicator()

                                    Text(
                                        stringResource(
                                            R.string.evidence_loading
                                        )
                                    )
                                }

                                estado.errorCarga != null -> {
                                    Text(
                                        text = estado.errorCarga.orEmpty(),
                                        color = colors.error
                                    )

                                    Button(
                                        onClick = viewModel::reintentarCarga,
                                        enabled = !estado.guardando
                                    ) {
                                        Text(
                                            stringResource(
                                                R.string.camera_retry
                                            )
                                        )
                                    }
                                }

                                estado.ingresoNoEncontrado -> {
                                    Text(
                                        stringResource(
                                            R.string.evidence_missing_ingreso
                                        )
                                    )
                                }

                                else -> {
                                    if (!estado.permiteAgregar) {
                                        Text(
                                            text = stringResource(
                                                R.string.evidence_read_only
                                            ),
                                            color = colors.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // La captura pendiente permanece visible
                // hasta que se guarda o se descarta.
                estado.rutaPendiente?.let { ruta ->
                    item(key = "captura_pendiente") {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement =
                                    Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = stringResource(
                                        R.string.evidence_review_photo
                                    ),
                                    style = MaterialTheme.typography.titleMedium
                                )

                                FotoEvidencia(
                                    ruta = ruta,
                                    onAbrir = {
                                        fotoAmpliada = ruta
                                    }
                                )

                                OutlinedTextField(
                                    value = estado.descripcion,
                                    onValueChange =
                                        viewModel::cambiarDescripcion,
                                    enabled = !estado.guardando,
                                    label = {
                                        Text(
                                            stringResource(
                                                R.string.evidence_description
                                            )
                                        )
                                    },
                                    supportingText = {
                                        Text(
                                            stringResource(
                                                R.string.evidence_description_count,
                                                estado.descripcion.length
                                            )
                                        )
                                    },
                                    isError = estado.descripcion
                                        .trim().length > 300,
                                    minLines = 2,
                                    maxLines = 4,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                estado.errorGuardado?.let { error ->
                                    Text(
                                        text = error,
                                        color = colors.error
                                    )
                                }

                                if (estado.guardando) {
                                    CircularProgressIndicator()

                                    Text(
                                        stringResource(
                                            R.string.evidence_processing
                                        )
                                    )
                                }

                                Button(
                                    onClick = {
                                        viewModel.guardar(tecnicoId)
                                    },
                                    enabled = estado.puedeGuardar,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        stringResource(
                                            R.string.evidence_save
                                        )
                                    )
                                }

                                TextButton(
                                    onClick = {
                                        salidaSolicitada = false
                                        confirmarDescarte = true
                                    },
                                    enabled = !estado.guardando,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        stringResource(
                                            R.string.evidence_discard
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                item(key = "galeria_guardada") {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                stringResource(R.string.evidence_saved_count, estado.evidencias.size),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            GaleriaIngresoConectada(viewModel, tecnicoId)
                        }
                    }
                }
            }
        }
    }

    if (confirmarDescarte) {
        DialogoAviso(
            tipo = TipoAviso.ADVERTENCIA,
            titulo = stringResource(R.string.evidence_discard_title),
            mensaje = stringResource(R.string.evidence_discard_message),
            textoSecundario = stringResource(R.string.evidence_discard),
            onSecundario = {
                val debeSalir = salidaSolicitada
                confirmarDescarte = false
                salidaSolicitada = false
                viewModel.descartarFoto()
                salirTrasDescartar = debeSalir
            },
            textoPrincipal = stringResource(R.string.evidence_keep),
            onPrincipal = {
                confirmarDescarte = false
                salidaSolicitada = false
            },
            onDismiss = {
                confirmarDescarte = false
                salidaSolicitada = false
            }
        )
    }

    fotoAmpliada?.let { ruta ->
        Dialog(
            onDismissRequest = {
                fotoAmpliada = null
            }
        ) {
            Surface(
                shape = MaterialTheme.shapes.large
            ) {
                // Permite desplazarse en pantallas pequeñas
                // o cuando el teléfono está en horizontal.
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        FotoEvidencia(
                            ruta = ruta,
                            altura = 420.dp
                        )
                    }

                    item {
                        TextButton(
                            onClick = {
                                fotoAmpliada = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                stringResource(
                                    R.string.evidence_close
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

// Vista de la captura pendiente y su ampliación.
@Composable
private fun FotoEvidencia(
    ruta: String,
    altura: Dp = 220.dp,
    onAbrir: (() -> Unit)? = null
) {
    var errorImagen by remember(ruta) {
        mutableStateOf(false)
    }

    val etiquetaAbrir = stringResource(
        R.string.evidence_open_photo
    )

    val modificador = Modifier
        .fillMaxWidth()
        .height(altura)

    Box(
        modifier = if (onAbrir != null) {
            modificador.clickable(
                onClickLabel = etiquetaAbrir,
                onClick = onAbrir
            )
        } else {
            modificador
        },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = File(ruta),
            contentDescription = stringResource(
                R.string.evidence_photo_description
            ),
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
            onSuccess = {
                errorImagen = false
            },
            onError = {
                errorImagen = true
            }
        )

        if (errorImagen) {
            Text(
                text = stringResource(
                    R.string.evidence_image_unavailable
                ),
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}