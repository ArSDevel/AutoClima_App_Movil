package com.example.myapplication.ui.dashboard.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.ui.dashboard.DashboardMenuOption
import com.example.myapplication.ui.theme.AutoClimasGradients
import com.example.myapplication.ui.theme.AutoClimasWhite
import com.example.myapplication.ui.theme.MyApplicationTheme

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

    ModalDrawerSheet(
        modifier = modifier.width(300.dp),
        drawerContainerColor = colors.background,
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth().fillMaxHeight()
                .background(AutoClimasGradients.loginBackground)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical))
                .padding(horizontal = 16.dp)
        ) {
            // Encabezado
            Column(
                modifier = Modifier.padding(start = 8.dp, top = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.dashboard_title),
                    style = typography.headlineMedium, color = AutoClimasWhite
                )
                Box(modifier = Modifier.width(36.dp).height(4.dp).clip(shapes.extraSmall).background(colors.secondary))

                Text(
                    text = stringResource(R.string.dashboard_menu_title),
                    style = typography.bodyMedium,
                    color = AutoClimasWhite
                )
            }

            Box(
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 16.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = shapes.large,
                        color = colors.surface,
                        contentColor = colors.onSurface
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            DashboardMenuOption.entries.forEach { opcion ->
                                val textoRes = when (opcion) {
                                    DashboardMenuOption.DASHBOARD -> R.string.dashboard_menu_home
                                    DashboardMenuOption.REGISTRO -> R.string.dashboard_menu_registration
                                    DashboardMenuOption.CHECKLIST -> R.string.dashboard_menu_checklist
                                    DashboardMenuOption.EVIDENCIAS -> R.string.dashboard_menu_evidence
                                    DashboardMenuOption.DIAGNOSTICO -> R.string.dashboard_menu_diagnosis
                                    DashboardMenuOption.FIRMA -> R.string.dashboard_menu_signature
                                    DashboardMenuOption.HISTORIAL -> R.string.dashboard_menu_history
                                }

                                val icono = when (opcion) {
                                    DashboardMenuOption.DASHBOARD -> Icons.Default.Dashboard
                                    DashboardMenuOption.REGISTRO -> Icons.Default.PersonAdd
                                    DashboardMenuOption.CHECKLIST -> Icons.Default.Checklist
                                    DashboardMenuOption.EVIDENCIAS -> Icons.Default.PhotoCamera
                                    DashboardMenuOption.DIAGNOSTICO -> Icons.Default.Build
                                    DashboardMenuOption.FIRMA -> Icons.Default.Draw
                                    DashboardMenuOption.HISTORIAL -> Icons.Default.History
                                }

                                NavigationDrawerItem(
                                    label = { Text(text = stringResource(textoRes), style = typography.labelLarge) },
                                    icon = {
                                        Icon(
                                            imageVector = icono,
                                            contentDescription = null,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    },
                                    selected =
                                        opcion == opcionSeleccionada,
                                    onClick = {
                                        onOpcionSeleccionada(opcion)
                                    },
                                    shape = shapes.small,
                                    colors = NavigationDrawerItemDefaults.colors(
                                            selectedContainerColor = colors.primaryContainer,
                                            selectedTextColor = colors.primary,
                                            selectedIconColor = colors.primary,
                                            unselectedContainerColor = Color.Transparent,
                                            unselectedTextColor = colors.onSurface,
                                            unselectedIconColor = colors.onSurfaceVariant
                                        )
                                )
                            }
                        }
                    }
                }
            }

            // Acción independiente de las secciones
            Surface(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = shapes.medium,
                color = colors.surface
            ) {
                TextButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    shape = shapes.medium,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.primary
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = stringResource(R.string.dashboard_logout), style = typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Preview(
    name = "Menú lateral",
    showBackground = true,
    widthDp = 320,
    heightDp = 800
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