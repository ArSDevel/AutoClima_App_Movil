package com.example.myapplication.ui.dashboard.components

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.ui.dashboard.DashboardMenuOption
import com.example.myapplication.ui.theme.AutoClimasGradients
import com.example.myapplication.ui.theme.AutoClimasWhite
import com.example.myapplication.ui.theme.MyApplicationTheme

// Menú compartido por dashboard y registro.
// Las pantallas principales delegan la navegación mediante callbacks.
@Composable
fun DashboardDrawer(
    opcionSeleccionada: DashboardMenuOption,
    onOpcionSeleccionada: (DashboardMenuOption) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val shapes = MaterialTheme.shapes
    val typography = MaterialTheme.typography

    ModalDrawerSheet(modifier = modifier.width(300.dp),
        drawerContainerColor = colors.surface,
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        Column(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
            // Encabezado: logo sobre un cuadro blanco, nombre del técnico y su rol.
            Row(
                modifier = Modifier.fillMaxWidth().background(AutoClimasGradients.header)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Start))
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(18.dp)).background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    com.example.myapplication.ui.components.LogoAutoClimas(
                        sobreFondoOscuro = false,
                        descripcion = stringResource(R.string.dashboard_logo_description),
                        modifier = Modifier.size(54.dp)) }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = com.example.myapplication.ui.components.nombreCortoTecnico(
                            com.example.myapplication.ui.components.LocalNombreTecnico.current
                        ) ?: stringResource(R.string.dashboard_title),
                        style = typography.titleLarge, color = Color.White, maxLines = 1)
                    Text(text = stringResource(R.string.drawer_role),
                        style = typography.bodyMedium, color = Color(0xFFDBEAFE)) } }

            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    DashboardMenuOption.DASHBOARD,
                    DashboardMenuOption.REGISTRO,
                    DashboardMenuOption.CHECKLIST,
                    DashboardMenuOption.EVIDENCIAS,
                    DashboardMenuOption.DIAGNOSTICO,
                    DashboardMenuOption.FIRMA,
                    DashboardMenuOption.HISTORIAL
                ).forEach { opcion ->
                    OpcionPrincipalDrawer(
                        opcion = opcion,
                        seleccionada = opcionSeleccionada == opcion,
                        onClick = { onOpcionSeleccionada(opcion) }) } }

            HorizontalDivider(color = colors.outlineVariant)
            // Acción independiente de las secciones.
            TextButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)),
                shape = shapes.small,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = colors.error)
            ) {
                Row(modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = stringResource(R.string.dashboard_logout), style = typography.labelLarge) } } } } }

// Acceso del menú: icono y nombre. El seleccionado lleva fondo azul claro.
@Composable
private fun OpcionPrincipalDrawer(
    opcion: DashboardMenuOption,
    seleccionada: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val color = if (seleccionada) colors.primary else colors.onSurface
    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (seleccionada) colors.primaryContainer else Color.Transparent)
            .clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = iconoDrawer(opcion), contentDescription = null,
            tint = if (seleccionada) colors.primary else colors.onSurfaceVariant,
            modifier = Modifier.size(24.dp))
        Text(
            text = stringResource(etiquetaDrawer(opcion)),
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 16.sp),
            color = color,
            modifier = Modifier.weight(1f)) } }

// Elemento informativo sin acción de navegación.
// También se identifica como no disponible para accesibilidad.
@Composable
private fun OpcionPendienteDrawer(
    opcion: DashboardMenuOption
) {
    Row(
        modifier = Modifier.fillMaxWidth().semantics(mergeDescendants = true) { disabled() },
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = iconoDrawer(opcion), contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
        Text(text = stringResource(etiquetaDrawer(opcion)), style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f)) } }

// Nombre de cada opción del menú.
@StringRes
private fun etiquetaDrawer(opcion: DashboardMenuOption): Int {
    return when (opcion) {
        DashboardMenuOption.DASHBOARD -> R.string.dashboard_menu_home
        DashboardMenuOption.REGISTRO -> R.string.dashboard_menu_registration
        DashboardMenuOption.CHECKLIST -> R.string.dashboard_menu_checklist
        DashboardMenuOption.EVIDENCIAS -> R.string.dashboard_menu_evidence
        DashboardMenuOption.DIAGNOSTICO -> R.string.dashboard_menu_diagnosis
        DashboardMenuOption.FIRMA -> R.string.dashboard_menu_signature
        DashboardMenuOption.HISTORIAL -> R.string.dashboard_menu_history } }

// Icono de cada opción del menú.
private fun iconoDrawer(
    opcion: DashboardMenuOption
): ImageVector {
    return when (opcion) {
        DashboardMenuOption.DASHBOARD -> Icons.Outlined.Home
        DashboardMenuOption.REGISTRO -> Icons.Outlined.Add
        DashboardMenuOption.CHECKLIST -> Icons.Outlined.CheckBox
        DashboardMenuOption.EVIDENCIAS -> Icons.Outlined.PhotoCamera
        DashboardMenuOption.DIAGNOSTICO -> Icons.Outlined.Description
        DashboardMenuOption.FIRMA -> Icons.Outlined.Draw
        DashboardMenuOption.HISTORIAL -> Icons.Outlined.Schedule } }

// Vista previa del menú en modo claro.
@Preview(
    name = "Menú lateral · claro",
    showBackground = true,
    widthDp = 320,
    heightDp = 900)
@Composable
private fun DashboardDrawerPreview() {
    MyApplicationTheme(darkTheme = false) {
        DashboardDrawer(
            opcionSeleccionada = DashboardMenuOption.DASHBOARD,
            onOpcionSeleccionada = {},
            onLogout = {}) } }

// Vista previa del menú en modo oscuro.
@Preview(
    name = "Menú lateral · oscuro",
    showBackground = true,
    widthDp = 320,
    heightDp = 900)
@Composable
private fun DashboardDrawerOscuroPreview() {
    MyApplicationTheme(darkTheme = true) {
        DashboardDrawer(
            opcionSeleccionada = DashboardMenuOption.REGISTRO,
            onOpcionSeleccionada = {},
            onLogout = {}) } }

// Vista previa del menú en una pantalla baja.
@Preview(
    name = "Menú lateral · pantalla baja",
    showBackground = true,
    widthDp = 320,
    heightDp = 400)
@Composable
private fun DashboardDrawerCompactoPreview() {
    MyApplicationTheme(darkTheme = true) {
        DashboardDrawer(
            opcionSeleccionada = DashboardMenuOption.DASHBOARD,
            onOpcionSeleccionada = {},
            onLogout = {}) } }