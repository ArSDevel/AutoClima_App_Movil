package com.example.myapplication.ui.respaldo

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.util.FormatoFechas
import com.example.myapplication.data.respaldo.ConteosDatos
import kotlin.system.exitProcess

// Diálogos del respaldo que comparten la pantalla de Respaldo y la bienvenida: progreso, confirmaciones, elección entre
// reemplazar y combinar, borrar todo, resultado y fin de la restauración.
// onExportarAntes ofrece guardar un respaldo de lo que hay ahora antes de una acción que no se puede deshacer; pideContrasena
// indica si hay una sesión cuya contraseña se debe pedir.
@Composable
fun DialogosRespaldo(
    viewModel: RespaldoViewModel,
    estado: RespaldoUiState,
    onExportarAntes: (() -> Unit)? = null,
    pideContrasena: Boolean = false) {
    val contexto = LocalContext.current
    val manifiesto = estado.manifiesto
    val actuales = estado.actuales

    if (estado.trabajando) {
        // No se puede cerrar: la operación sigue hasta terminar.
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            text = {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    Text(stringResource(estado.textoTrabajo ?: R.string.respaldo_checking)) } }) }

    if (!estado.trabajando && !estado.restauracionCompleta) {
        when (estado.paso) {
            PasoRespaldo.CONFIRMAR_VACIO -> if (manifiesto != null) {
                AlertDialog(
                    onDismissRequest = viewModel::cancelar,
                    title = { Text(stringResource(R.string.respaldo_confirm_title)) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(stringResource(R.string.respaldo_confirm_date, FormatoFechas.fechaYHora(manifiesto.fechaMs)))
                            Text(stringResource(R.string.respaldo_confirm_counts, manifiesto.ingresos, manifiesto.vehiculos,
                                manifiesto.clientes, manifiesto.usuarios))
                            Text(stringResource(R.string.respaldo_confirm_text)) } },
                    confirmButton = { TextButton(onClick = viewModel::confirmarRestauracionVacia) {
                        Text(stringResource(R.string.respaldo_confirm_yes)) } },
                    dismissButton = { TextButton(onClick = viewModel::cancelar) {
                        Text(stringResource(R.string.usuarios_delete_cancel)) } }) }

            PasoRespaldo.ELEGIR -> if (manifiesto != null && actuales != null) {
                AlertDialog(
                    onDismissRequest = viewModel::cancelar,
                    title = { Text(stringResource(R.string.respaldo_has_data_title)) },
                    text = {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Comparacion(manifiesto.fechaMs, ConteosDatos(manifiesto.ingresos, manifiesto.vehiculos,
                                manifiesto.clientes, manifiesto.usuarios), actuales)
                            Text(stringResource(R.string.respaldo_choose_text)) } },
                    confirmButton = {
                        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = viewModel::elegirCombinar, modifier = Modifier.fillMaxWidth()) {
                                Text(stringResource(R.string.respaldo_merge)) }
                            Text(stringResource(R.string.respaldo_merge_hint), style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            OutlinedButton(
                                onClick = viewModel::elegirReemplazar, modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                                Text(stringResource(R.string.respaldo_replace)) }
                            Text(stringResource(R.string.respaldo_replace_hint), style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            TextButton(onClick = viewModel::cancelar, modifier = Modifier.fillMaxWidth()) {
                                Text(stringResource(R.string.usuarios_delete_cancel)) } } }) }

            PasoRespaldo.CONFIRMAR_REEMPLAZO -> if (manifiesto != null && actuales != null) {
                ConfirmacionPeligrosa(
                    titulo = stringResource(R.string.respaldo_replace_title),
                    palabra = stringResource(R.string.respaldo_word_replace),
                    botonConfirmar = stringResource(R.string.respaldo_replace_confirm),
                    errorContrasena = estado.errorContrasena, pideContrasena = pideContrasena,
                    onExportarAntes = onExportarAntes,
                    onConfirmar = viewModel::confirmarReemplazo, onCancelar = viewModel::cancelar) {
                    Comparacion(manifiesto.fechaMs, ConteosDatos(manifiesto.ingresos, manifiesto.vehiculos,
                        manifiesto.clientes, manifiesto.usuarios), actuales)
                    Text(stringResource(R.string.respaldo_replace_warning), color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium) } }

            PasoRespaldo.CONFIRMAR_COMBINAR -> {
                val plan = estado.plan
                if (plan != null) {
                    val reemplazados = plan.distintos.count { estado.ganadores[it.idRespaldo] == com.example.myapplication.data.respaldo.Lado.RESPALDO }
                    AlertDialog(
                        onDismissRequest = viewModel::volverALaVistaPrevia,
                        title = { Text(stringResource(R.string.respaldo_merge_confirm_title)) },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(pluralStringResource(R.plurals.respaldo_merge_confirm_new, plan.nuevos.size, plan.nuevos.size))
                                Text(pluralStringResource(R.plurals.respaldo_merge_confirm_replaced, reemplazados, reemplazados))
                                Text(stringResource(R.string.respaldo_merge_confirm_warning), color = MaterialTheme.colorScheme.error) } },
                        confirmButton = { TextButton(onClick = viewModel::confirmarCombinacion) {
                            Text(stringResource(R.string.respaldo_merge_confirm_yes)) } },
                        dismissButton = { TextButton(onClick = viewModel::volverALaVistaPrevia) {
                            Text(stringResource(R.string.respaldo_back)) } }) } }

            PasoRespaldo.BORRAR_TODO -> ConfirmacionPeligrosa(
                titulo = stringResource(R.string.respaldo_wipe_title),
                palabra = stringResource(R.string.respaldo_word_wipe),
                botonConfirmar = stringResource(R.string.respaldo_wipe_confirm),
                errorContrasena = estado.errorContrasena, pideContrasena = pideContrasena,
                onExportarAntes = onExportarAntes,
                onConfirmar = viewModel::confirmarBorrarTodo, onCancelar = viewModel::cancelar) {
                Text(stringResource(R.string.respaldo_wipe_text))
                Text(stringResource(R.string.respaldo_wipe_keeps), style = MaterialTheme.typography.bodyMedium)
                Text(stringResource(R.string.respaldo_wipe_warning), color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium) }

            else -> Unit } }

    estado.resultado?.let { r ->
        AlertDialog(
            onDismissRequest = viewModel::resultadoVisto,
            title = { Text(stringResource(R.string.respaldo_merge_done_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(pluralStringResource(R.plurals.respaldo_merge_done_added, r.agregados, r.agregados))
                    Text(pluralStringResource(R.plurals.respaldo_merge_done_updated, r.actualizados, r.actualizados))
                    Text(pluralStringResource(R.plurals.respaldo_merge_done_same, r.sinCambios, r.sinCambios))
                    if (r.vehiculos + r.clientes + r.usuarios > 0) {
                        Text(stringResource(R.string.respaldo_merge_done_extra, r.vehiculos, r.clientes, r.usuarios)) } } },
            confirmButton = { TextButton(onClick = viewModel::resultadoVisto) { Text(stringResource(R.string.usuarios_blocked_ok)) } }) }

    if (estado.restauracionCompleta) {
        // La app queda con la base anterior en memoria: lo seguro es cerrarla y abrirla de nuevo.
        AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.respaldo_done_title)) },
            text = { Text(stringResource(R.string.respaldo_done_text)) },
            confirmButton = {
                TextButton(onClick = {
                    (contexto as? Activity)?.finishAffinity()
                    exitProcess(0) }) { Text(stringResource(R.string.respaldo_done_button)) } }) } }

// Lo que tiene el respaldo y lo que tiene el celular ahora, lado a lado.
@Composable
private fun Comparacion(fechaRespaldoMs: Long, respaldo: ConteosDatos, actual: ConteosDatos) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(R.string.respaldo_compare_backup, FormatoFechas.fechaYHora(fechaRespaldoMs)),
            style = MaterialTheme.typography.titleSmall)
        Text(stringResource(R.string.respaldo_confirm_counts, respaldo.ingresos, respaldo.vehiculos, respaldo.clientes, respaldo.usuarios))
        Text(stringResource(R.string.respaldo_compare_now), style = MaterialTheme.typography.titleSmall)
        Text(stringResource(R.string.respaldo_confirm_counts, actual.ingresos, actual.vehiculos, actual.clientes, actual.usuarios)) } }

// Confirmación de una acción que no se puede deshacer: hay que escribir una palabra y, con sesión, la contraseña.
@Composable
private fun ConfirmacionPeligrosa(
    titulo: String,
    palabra: String,
    botonConfirmar: String,
    errorContrasena: Boolean,
    pideContrasena: Boolean,
    onExportarAntes: (() -> Unit)?,
    onConfirmar: (String) -> Unit,
    onCancelar: () -> Unit,
    contenido: @Composable () -> Unit) {
    var escrito by rememberSaveable { mutableStateOf("") }
    var contrasena by rememberSaveable { mutableStateOf("") }
    val habilitado = escrito.trim().equals(palabra, ignoreCase = true) && (!pideContrasena || contrasena.isNotEmpty())
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                contenido()
                if (onExportarAntes != null) {
                    OutlinedButton(onClick = onExportarAntes, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.respaldo_export_before)) } }
                OutlinedTextField(
                    value = escrito, onValueChange = { escrito = it }, singleLine = true,
                    label = { Text(stringResource(R.string.respaldo_type_word, palabra)) },
                    modifier = Modifier.fillMaxWidth())
                if (pideContrasena) {
                    OutlinedTextField(
                        value = contrasena, onValueChange = { contrasena = it }, singleLine = true,
                        label = { Text(stringResource(R.string.respaldo_your_password)) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError = errorContrasena, modifier = Modifier.fillMaxWidth())
                    if (errorContrasena) {
                        Text(stringResource(R.string.usuarios_error_password_actual), color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall) } } } },
        confirmButton = {
            TextButton(
                onClick = { onConfirmar(contrasena) }, enabled = habilitado,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text(botonConfirmar) } },
        dismissButton = { TextButton(onClick = onCancelar) { Text(stringResource(R.string.usuarios_delete_cancel)) } }) }


