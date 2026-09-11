package com.example.myapplication.ui.dashboard.components

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
// Los módulos pendientes se muestran sin interacción.
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
        drawerContainerColor = colors.background,
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        Column(modifier = Modifier.fillMaxWidth().fillMaxHeight()
                .background(AutoClimasGradients.loginBackground)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical + WindowInsetsSides.Start))
                .verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Encabezado con la identidad visual de AutoClimas.
            Surface(
                modifier = Modifier.fillMaxWidth(), shape = shapes.large, color = colors.surface,
                contentColor = colors.onSurface
            ) {
                Column(modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.size(72.dp).clip(CircleShape)
                            .border(width = 2.dp, color = colors.primary, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(painter = painterResource(R.drawable.logo_autoclimas),
                            contentDescription = stringResource(R.string.dashboard_logo_description),
                            contentScale = ContentScale.Fit, modifier = Modifier.size(54.dp))
                    }
                    Text(text = stringResource(R.string.dashboard_title),
                        style = typography.headlineSmall)
                    Box(modifier = Modifier.width(36.dp).height(4.dp).clip(shapes.extraSmall)
                            .background(colors.secondary))
                    Text(text = stringResource(R.string.dashboard_drawer_description),
                        style = typography.bodyMedium, color = colors.onSurfaceVariant)
                }
            }

            // Accesos disponibles.
            Text(text = stringResource(R.string.dashboard_drawer_main), style = typography.labelLarge,
                color = AutoClimasWhite, modifier = Modifier.padding(horizontal = 8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(), shape = shapes.large,
                color = colors.surface, contentColor = colors.onSurface
            ) {
                Column(modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    OpcionPrincipalDrawer(
                        opcion = DashboardMenuOption.DASHBOARD,
                        seleccionada = opcionSeleccionada == DashboardMenuOption.DASHBOARD,
                        onClick = { onOpcionSeleccionada(DashboardMenuOption.DASHBOARD)
                        }
                    )
                    OpcionPrincipalDrawer(
                        opcion = DashboardMenuOption.REGISTRO,
                        seleccionada = opcionSeleccionada == DashboardMenuOption.REGISTRO,
                        onClick = { onOpcionSeleccionada(DashboardMenuOption.REGISTRO)
                        }
                    )
                }
            }

            // Módulos previstos para implementaciones posteriores.
            Text(
                text = stringResource(R.string.dashboard_drawer_coming_soon),
                style = typography.labelLarge, color = AutoClimasWhite,
                modifier = Modifier.padding(horizontal = 8.dp))
            Surface(modifier = Modifier.fillMaxWidth(),
                shape = shapes.large,
                color = colors.surface,
                contentColor = colors.onSurface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = stringResource(R.string.dashboard_drawer_pending_description),
                        style = typography.bodySmall,
                        color = colors.onSurfaceVariant)

                    HorizontalDivider()
                    DashboardMenuOption.entries
                        .filter { opcion -> opcion != DashboardMenuOption.DASHBOARD &&
                                opcion != DashboardMenuOption.REGISTRO }
                        .forEach { opcion -> OpcionPendienteDrawer(opcion) }
                }
            }

            // Acción independiente de las secciones.
            Surface(modifier = Modifier.fillMaxWidth(), shape = shapes.medium,
                color = colors.surface
            ) {
                TextButton(
                    onClick = onLogout, modifier = Modifier.fillMaxWidth(),
                    shape = shapes.medium,
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
                        Text(text = stringResource(R.string.dashboard_logout), style = typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

// Acceso disponible con indicador visual de selección.
@Composable
private fun OpcionPrincipalDrawer(
    opcion: DashboardMenuOption,
    seleccionada: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    NavigationDrawerItem(
        label = { Text(text = stringResource(etiquetaDrawer(opcion)),
                style = MaterialTheme.typography.labelLarge) },
        icon = { Icon(imageVector = iconoDrawer(opcion), contentDescription = null,
                modifier = Modifier.size(22.dp)) },
        selected = seleccionada,
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = colors.primaryContainer,
            selectedTextColor = colors.onPrimaryContainer,
            selectedIconColor = colors.onPrimaryContainer,
            unselectedContainerColor = Color.Transparent,
            unselectedTextColor = colors.onSurface,
            unselectedIconColor = colors.onSurfaceVariant
        )
    )
}

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
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
    }
}

@StringRes
private fun etiquetaDrawer(opcion: DashboardMenuOption): Int {
    return when (opcion) {
        DashboardMenuOption.DASHBOARD -> R.string.dashboard_menu_home
        DashboardMenuOption.REGISTRO -> R.string.dashboard_menu_registration
        DashboardMenuOption.CHECKLIST -> R.string.dashboard_menu_checklist
        DashboardMenuOption.EVIDENCIAS -> R.string.dashboard_menu_evidence
        DashboardMenuOption.DIAGNOSTICO -> R.string.dashboard_menu_diagnosis
        DashboardMenuOption.FIRMA -> R.string.dashboard_menu_signature
        DashboardMenuOption.HISTORIAL -> R.string.dashboard_menu_history
    }
}

private fun iconoDrawer(
    opcion: DashboardMenuOption
): ImageVector {
    return when (opcion) {
        DashboardMenuOption.DASHBOARD -> Icons.Default.Dashboard
        DashboardMenuOption.REGISTRO -> Icons.Default.PersonAdd
        DashboardMenuOption.CHECKLIST -> Icons.Default.Checklist
        DashboardMenuOption.EVIDENCIAS -> Icons.Default.PhotoCamera
        DashboardMenuOption.DIAGNOSTICO -> Icons.Default.Build
        DashboardMenuOption.FIRMA -> Icons.Default.Draw
        DashboardMenuOption.HISTORIAL -> Icons.Default.History
    }
}

@Preview(
    name = "Menú lateral · claro",
    showBackground = true,
    widthDp = 320,
    heightDp = 900
)
@Composable
private fun DashboardDrawerPreview() {
    MyApplicationTheme(darkTheme = false) {
        DashboardDrawer(
            opcionSeleccionada = DashboardMenuOption.DASHBOARD,
            onOpcionSeleccionada = {},
            onLogout = {}
        )
    }
}

@Preview(
    name = "Menú lateral · oscuro",
    showBackground = true,
    widthDp = 320,
    heightDp = 900
)
@Composable
private fun DashboardDrawerOscuroPreview() {
    MyApplicationTheme(darkTheme = true) {
        DashboardDrawer(
            opcionSeleccionada = DashboardMenuOption.REGISTRO,
            onOpcionSeleccionada = {},
            onLogout = {}
        )
    }
}

@Preview(
    name = "Menú lateral · pantalla baja",
    showBackground = true,
    widthDp = 320,
    heightDp = 400
)
@Composable
private fun DashboardDrawerCompactoPreview() {
    MyApplicationTheme(darkTheme = true) {
        DashboardDrawer(
            opcionSeleccionada = DashboardMenuOption.DASHBOARD,
            onOpcionSeleccionada = {},
            onLogout = {}
        )
    }
}