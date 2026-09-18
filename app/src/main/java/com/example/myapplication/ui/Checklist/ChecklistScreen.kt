package com.example.myapplication.ui.checklist

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.myapplication.ui.theme.AutoClimasGradients
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.R
import com.example.myapplication.ui.checklist.components.PreguntaChecklist
import com.example.myapplication.ui.checklist.components.SeccionChecklist

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistScreen(
    viewModel: ChecklistViewModel,
    tecnicoId: Long,
    onVolver: () -> Unit
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme
    var confirmarSalida by rememberSaveable { mutableStateOf(false) }

    // Se utiliza tanto desde la barra superior como
    // desde el botón o gesto de regreso del dispositivo.
    val solicitarSalida: () -> Unit = {
        if (!estado.guardando) {
            if (estado.tieneCambiosSinGuardar) {
                confirmarSalida = true
            } else {
                onVolver()
            }
        }
    }

    // Durante el guardado también intercepta el regreso
    // para evitar abandonar la operación.
    BackHandler {
        solicitarSalida()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.checklist_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = solicitarSalida,
                        enabled = !estado.guardando
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(
                                R.string.checklist_back
                            )
                        )
                    }
                },
                actions = {
                    Image(
                        painter = painterResource(R.drawable.logo_autoclimas),
                        contentDescription = stringResource(
                            R.string.dashboard_logo_description
                        ),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .width(72.dp)
                            .height(56.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.tertiary,
                    titleContentColor = colors.onTertiary,
                    navigationIconContentColor = colors.onTertiary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize()
                .background(AutoClimasGradients.loginBackground)
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            when {
                estado.cargando -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(40.dp)
                    )
                }

                estado.errorCarga != null || estado.ingresoNoEncontrado -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .widthIn(max = 480.dp)
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = estado.errorCarga
                                ?: stringResource(
                                    R.string.checklist_not_found
                                ),
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Button(onClick = viewModel::reintentarCarga) {
                            Text(stringResource(R.string.checklist_retry))
                        }
                    }
                }

                estado.formularioDisponible -> {
                    LazyColumn(
                        modifier = Modifier
                            .widthIn(max = 840.dp)
                            .fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        item(key = "encabezado") {
                            EncabezadoChecklist(estado)
                        }

                        if (!estado.permiteEdicion) {
                            item(key = "consulta") {
                                Surface(
                                    shape = MaterialTheme.shapes.medium,
                                    color = MaterialTheme.colorScheme
                                        .secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme
                                        .onSecondaryContainer
                                ) {
                                    Text(
                                        text = stringResource(
                                            R.string.checklist_read_only
                                        ),
                                        modifier = Modifier.padding(16.dp),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        item(key = "refrigerante") {
                            SeccionChecklist(
                                titulo = stringResource(
                                    R.string.checklist_section_refrigerant_title
                                ),
                                descripcion = stringResource(
                                    R.string.checklist_section_refrigerant_description
                                ),
                                icono = Icons.Default.Search,
                                preguntasRespondidas = listOf(
                                    estado.faltaGasRefrigerante,
                                    estado.fugaVisibleMangueras,
                                    estado.fugasDetectadasLuzUV
                                ).count { it != null },
                                totalPreguntas = 3
                            ) {
                                PreguntaChecklist(
                                    numero = 1,
                                    pregunta = stringResource(
                                        R.string.checklist_question_refrigerant
                                    ),
                                    respuesta = estado.faltaGasRefrigerante,
                                    onRespuestaChange =
                                        viewModel::onFaltaGasRefrigeranteChange,
                                    habilitada = estado.puedeModificar,
                                    mostrarPendiente =
                                        estado.mostrarPreguntasPendientes
                                )

                                PreguntaChecklist(
                                    numero = 2,
                                    pregunta = stringResource(
                                        R.string.checklist_question_hoses
                                    ),
                                    respuesta = estado.fugaVisibleMangueras,
                                    onRespuestaChange =
                                        viewModel::onFugaVisibleManguerasChange,
                                    habilitada = estado.puedeModificar,
                                    mostrarPendiente =
                                        estado.mostrarPreguntasPendientes
                                )

                                PreguntaChecklist(
                                    numero = 3,
                                    pregunta = stringResource(
                                        R.string.checklist_question_uv
                                    ),
                                    respuesta = estado.fugasDetectadasLuzUV,
                                    onRespuestaChange =
                                        viewModel::onFugasDetectadasLuzUVChange,
                                    habilitada = estado.puedeModificar,
                                    mostrarPendiente =
                                        estado.mostrarPreguntasPendientes
                                )
                            }
                        }

                        item(key = "compresor") {
                            SeccionChecklist(
                                titulo = stringResource(
                                    R.string.checklist_section_compressor_title
                                ),
                                descripcion = stringResource(
                                    R.string.checklist_section_compressor_description
                                ),
                                icono = Icons.Default.Build,
                                preguntasRespondidas = listOf(
                                    estado.compresorEmbragaAlEncender,
                                    estado.compresorDanadoOAmarrado,
                                    estado.bandaCompresorDesgastada
                                ).count { it != null },
                                totalPreguntas = 3
                            ) {
                                PreguntaChecklist(
                                    numero = 4,
                                    pregunta = stringResource(
                                        R.string.checklist_question_engagement
                                    ),
                                    respuesta = estado.compresorEmbragaAlEncender,
                                    onRespuestaChange =
                                        viewModel::onCompresorEmbragaAlEncenderChange,
                                    habilitada = estado.puedeModificar,
                                    mostrarPendiente =
                                        estado.mostrarPreguntasPendientes
                                )

                                PreguntaChecklist(
                                    numero = 5,
                                    pregunta = stringResource(
                                        R.string.checklist_question_compressor_damage
                                    ),
                                    respuesta = estado.compresorDanadoOAmarrado,
                                    onRespuestaChange =
                                        viewModel::onCompresorDanadoOAmarradoChange,
                                    habilitada = estado.puedeModificar,
                                    mostrarPendiente =
                                        estado.mostrarPreguntasPendientes
                                )

                                PreguntaChecklist(
                                    numero = 6,
                                    pregunta = stringResource(
                                        R.string.checklist_question_belt
                                    ),
                                    respuesta = estado.bandaCompresorDesgastada,
                                    onRespuestaChange =
                                        viewModel::onBandaCompresorDesgastadaChange,
                                    habilitada = estado.puedeModificar,
                                    mostrarPendiente =
                                        estado.mostrarPreguntasPendientes
                                )
                            }
                        }

                        item(key = "ventilacion") {
                            SeccionChecklist(
                                titulo = stringResource(
                                    R.string.checklist_section_ventilation_title
                                ),
                                descripcion = stringResource(
                                    R.string.checklist_section_ventilation_description
                                ),
                                icono = Icons.Default.Settings,
                                preguntasRespondidas = listOf(
                                    estado.funcionanAbanicosRadiador,
                                    estado.filtroCabinaSucioUObstruido,
                                    estado.condensadorObstruido
                                ).count { it != null },
                                totalPreguntas = 3
                            ) {
                                PreguntaChecklist(
                                    numero = 7,
                                    pregunta = stringResource(
                                        R.string.checklist_question_fans
                                    ),
                                    respuesta = estado.funcionanAbanicosRadiador,
                                    onRespuestaChange =
                                        viewModel::onFuncionanAbanicosRadiadorChange,
                                    habilitada = estado.puedeModificar,
                                    mostrarPendiente =
                                        estado.mostrarPreguntasPendientes
                                )

                                PreguntaChecklist(
                                    numero = 8,
                                    pregunta = stringResource(
                                        R.string.checklist_question_filter
                                    ),
                                    respuesta = estado.filtroCabinaSucioUObstruido,
                                    onRespuestaChange =
                                        viewModel::onFiltroCabinaSucioUObstruidoChange,
                                    habilitada = estado.puedeModificar,
                                    mostrarPendiente =
                                        estado.mostrarPreguntasPendientes
                                )

                                PreguntaChecklist(
                                    numero = 9,
                                    pregunta = stringResource(
                                        R.string.checklist_question_condenser
                                    ),
                                    respuesta = estado.condensadorObstruido,
                                    onRespuestaChange =
                                        viewModel::onCondensadorObstruidoChange,
                                    habilitada = estado.puedeModificar,
                                    mostrarPendiente =
                                        estado.mostrarPreguntasPendientes
                                )
                            }
                        }

                        item(key = "comentarios") {
                            OutlinedTextField(
                                value = estado.comentarios,
                                onValueChange = viewModel::onComentariosChange,
                                modifier = Modifier.fillMaxWidth(),
                                readOnly = !estado.puedeModificar,
                                label = {
                                    Text(
                                        stringResource(
                                            R.string.checklist_comments
                                        )
                                    )
                                },
                                supportingText = {
                                    Text(
                                        stringResource(
                                            R.string.checklist_comments_help
                                        )
                                    )
                                },
                                minLines = 4,
                                shape = MaterialTheme.shapes.medium
                            )
                        }

                        estado.errorGuardado?.let { mensaje ->
                            item(key = "error_guardado") {
                                Text(
                                    text = mensaje,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        if (estado.permiteEdicion) {
                            item(key = "guardar") {
                                Button(
                                    onClick = {
                                        viewModel.guardar(tecnicoId)
                                    },
                                    enabled = estado.puedeGuardar,
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(16.dp)
                                ) {
                                    if (estado.guardando) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        Spacer(Modifier.width(12.dp))
                                    }

                                    Text(
                                        stringResource(
                                            when {
                                                estado.guardando ->
                                                    R.string.checklist_saving

                                                estado.checklistId != null ->
                                                    R.string.checklist_save_changes

                                                else ->
                                                    R.string.checklist_save
                                            }
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirma la salida únicamente cuando hay cambios pendientes.
    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            title = {
                Text(stringResource(R.string.checklist_leave_title))
            },
            text = {
                Text(stringResource(R.string.checklist_leave_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmarSalida = false
                        onVolver()
                    }
                ) {
                    Text(stringResource(R.string.checklist_discard))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarSalida = false }) {
                    Text(stringResource(R.string.checklist_keep_editing))
                }
            }
        )
    }

    // Espera a que termine toda la operación antes de mostrar el éxito.
    if (estado.guardadoExitoso && !estado.guardando) {
        AlertDialog(
            onDismissRequest = viewModel::consumirGuardadoExitoso,
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(stringResource(R.string.checklist_saved_title))
            },
            text = {
                Text(stringResource(R.string.checklist_saved_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.consumirGuardadoExitoso()
                        onVolver()
                    }
                ) {
                    Text(stringResource(R.string.checklist_back_to_detail))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = viewModel::consumirGuardadoExitoso
                ) {
                    Text(stringResource(R.string.checklist_stay))
                }
            }
        )
    }
}

// Presenta el vehículo y el avance real de las respuestas.
// El progreso no representa un diagnóstico ni un cambio de estado.
@Composable
private fun EncabezadoChecklist(
    estado: ChecklistUiState
) {
    val ingreso = estado.ingreso ?: return
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = MaterialTheme.shapes.large,
        color = colors.surface,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = colors.primaryContainer,
                    contentColor = colors.onPrimaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(12.dp)
                            .size(28.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = ingreso.placa,
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.primary
                    )

                    Text(
                        text = ingreso.modelo,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text(
                        text = stringResource(
                            R.string.checklist_ingreso_number,
                            ingreso.ingresoId
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }

            Text(
                text = stringResource(R.string.checklist_instructions),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )

            LinearProgressIndicator(
                progress = { estado.progreso },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = stringResource(
                    R.string.checklist_section_progress,
                    estado.preguntasRespondidas,
                    estado.totalPreguntas
                ),
                style = MaterialTheme.typography.labelLarge,
                color = colors.primary
            )

            Text(
                text = stringResource(
                    when {
                        estado.tieneCambiosSinGuardar ->
                            R.string.checklist_unsaved_changes

                        estado.checklistId != null ->
                            R.string.checklist_saved_record

                        else ->
                            R.string.checklist_new_record
                    }
                ),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
    }
}