package com.example.myapplication.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.ui.diagnostico.SeleccionIngresoDiagnosticoScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.repository.DashboardRepository
import com.example.myapplication.data.repository.RegistroRepository
import com.example.myapplication.ui.checklist.SeleccionIngresoChecklistScreen
import com.example.myapplication.ui.dashboard.DashboardMenuOption
import com.example.myapplication.ui.dashboard.DashboardScreen
import com.example.myapplication.ui.dashboard.DashboardViewModel
import com.example.myapplication.ui.dashboard.DashboardViewModelFactory
import com.example.myapplication.data.local.repository.DiagnosticoRepository
import com.example.myapplication.ui.diagnostico.DiagnosticoScreen
import com.example.myapplication.ui.diagnostico.DiagnosticoViewModel
import com.example.myapplication.ui.diagnostico.DiagnosticoViewModelFactory
import com.example.myapplication.ui.dashboard.DetalleIngresoScreen
import com.example.myapplication.ui.dashboard.DetalleIngresoViewModel
import com.example.myapplication.ui.dashboard.DetalleIngresoViewModelFactory
import com.example.myapplication.ui.login.LoginScreen
import com.example.myapplication.ui.login.LoginViewModel
import com.example.myapplication.ui.registro.RegistroScreen
import com.example.myapplication.ui.registro.RegistroViewModel
import com.example.myapplication.ui.registro.RegistroViewModelFactory
import com.example.myapplication.data.repository.ChecklistRepository
import com.example.myapplication.ui.checklist.ChecklistScreen
import com.example.myapplication.ui.checklist.ChecklistViewModel
import com.example.myapplication.ui.checklist.ChecklistViewModelFactory

// Coordina la navegación y conecta las dependencias necesarias para cada pantalla.
@Composable
fun AutoClimaApp() {
    val contexto = LocalContext.current.applicationContext
    val baseDeDatos = remember(contexto) { AppDatabase.getInstance(contexto) }
    val tecnicoDao = remember(baseDeDatos) { baseDeDatos.tecnicoDao() }
    val dashboardRepository = remember(baseDeDatos) { DashboardRepository(baseDeDatos = baseDeDatos) }
    val registroRepository = remember(baseDeDatos) { RegistroRepository(baseDeDatos = baseDeDatos) }
    val checklistRepository = remember(baseDeDatos) { ChecklistRepository(baseDeDatos = baseDeDatos) }
    val diagnosticoRepository = remember(baseDeDatos) { DiagnosticoRepository(baseDeDatos = baseDeDatos) }

    // Construye el ViewModel de inicio de sesión.
    val loginFactory = remember(tecnicoDao) {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                    "ViewModel desconocido: ${modelClass.name}"
                }
                @Suppress("UNCHECKED_CAST")
                return LoginViewModel(tecnicoDao) as T
            }
        }
    }

    // Vive fuera de las entradas de navegación para conservar
    // la sesión mientras se cambia de pantalla.
    val loginViewModel: LoginViewModel = viewModel(factory = loginFactory)
    val uiState by loginViewModel.uiState.collectAsStateWithLifecycle()
    // Identificador real obtenido al verificar las credenciales.
    val tecnicoId = uiState.tecnicoId

    val sesionValida = uiState.isLoggedIn && tecnicoId != null && tecnicoId > 0 && uiState.id.isNotBlank()

    val dashboardFactory = remember(dashboardRepository) {
        DashboardViewModelFactory(repository = dashboardRepository)
    }
    val backStack = rememberNavBackStack(LoginRoute)
    // Ajusta la navegación cuando cambia la sesión.
    LaunchedEffect(uiState.isLoggedIn, uiState.id, tecnicoId) {
        if (!sesionValida) {
            // Una sesión abierta debe incluir un técnico válido.
            if (uiState.isLoggedIn) { loginViewModel.resetLoginState() }

            if (backStack.size != 1 || backStack.lastOrNull() != LoginRoute) {
                backStack.clear()
                backStack.add(LoginRoute)
            }
        } else {
            val raiz = backStack.firstOrNull()
            val perteneceAlUsuario = raiz is DashboardRoute && raiz.userId == uiState.id
            if (!perteneceAlUsuario) {
                backStack.clear()
                backStack.add(DashboardRoute(userId = uiState.id))
            }
        }
    }

    // Limpia la sesión y retira las pantallas del usuario anterior.
    fun cerrarSesion() {
        loginViewModel.resetLoginState()
        backStack.clear()
        backStack.add(LoginRoute)
    }

    // Regresa al dashboard sin agregar una pantalla duplicada.
    fun volverAlDashboard() {
        if (!sesionValida) return
        val raiz = backStack.firstOrNull()
        if (raiz is DashboardRoute && raiz.userId == uiState.id) {
            while (backStack.size > 1) {
                backStack.removeAt(backStack.lastIndex)
            }
        } else {
            backStack.clear()
            backStack.add(DashboardRoute(userId = uiState.id))
        }
    }

    // Sin ID: registro nuevo. Con ID: edición.
    fun abrirRegistro(ingresoId: Long? = null) {
        if (!sesionValida) return
        if (ingresoId != null && ingresoId <= 0) return
        if (backStack.lastOrNull() is RegistroRoute) return
        backStack.add(RegistroRoute(userId = uiState.id, ingresoId = ingresoId))
    }



    // Abre el expediente de un ingreso existente.
    fun abrirDetalle(ingresoId: Long) {
        if (!sesionValida || ingresoId <= 0) return
        // Al consultar el ingreso guardado, retiramos el formulario.
        if (backStack.lastOrNull() is RegistroRoute) {
            backStack.removeAt(backStack.lastIndex)
        }
        val destino = DetalleIngresoRoute(userId = uiState.id, ingresoId = ingresoId)
        // Si se editó desde el detalle, reutilizamos esa entrada.
        if (backStack.lastOrNull() == destino) return
        backStack.add(destino)
    }

    // Abre la selección de ingreso desde el menú lateral.
    fun abrirSeleccionChecklist() {
        if (!sesionValida) return

        val destino = SeleccionIngresoChecklistRoute(
            userId = uiState.id
        )

        if (backStack.lastOrNull() == destino) return

        // Desde Registro, la pantalla ya confirmó la salida
        // si existían cambios pendientes.
        // Retirar su entrada evita conservar un formulario descartado.
        if (backStack.lastOrNull() is RegistroRoute) {
            backStack.removeAt(backStack.lastIndex)
        }

        if (backStack.lastOrNull() != destino) {
            backStack.add(destino)
        }
    }

    // Abre la selección de ingreso desde el menú lateral.
    fun abrirSeleccionDiagnostico() {
        if (!sesionValida) return

        val destino = SeleccionIngresoDiagnosticoRoute(
            userId = uiState.id
        )

        if (backStack.lastOrNull() == destino) return

        // Registro ya solicitó confirmar la salida si había cambios.
        // Retiramos su entrada para no conservar el formulario descartado.
        if (backStack.lastOrNull() is RegistroRoute) {
            backStack.removeAt(backStack.lastIndex)
        }

        if (backStack.lastOrNull() != destino) {
            backStack.add(destino)
        }
    }

    // Abre el checklist conservando el detalle debajo en la pila.
    fun abrirChecklist(ingresoId: Long) {
        if (!sesionValida || ingresoId <= 0) return

        val destino = ChecklistRoute(
            userId = uiState.id,
            ingresoId = ingresoId
        )

        if (backStack.lastOrNull() == destino) return

        backStack.add(destino)
    }

    // Abre el diagnóstico conservando el detalle debajo.
    fun abrirDiagnostico(ingresoId: Long) {
        if (!sesionValida || ingresoId <= 0) return

        val destino = DiagnosticoRoute(
            userId = uiState.id,
            ingresoId = ingresoId
        )

        if (backStack.lastOrNull() == destino) return

        backStack.add(destino)
    }

    // Retira el diagnóstico y recupera el expediente correspondiente.
    fun volverDelDiagnostico(ingresoId: Long) {
        if (!sesionValida) return

        val actual = backStack.lastOrNull()

        if (actual is DiagnosticoRoute && actual.ingresoId == ingresoId) {
            backStack.removeAt(backStack.lastIndex)
        }

        abrirDetalle(ingresoId)
    }

    // Retira el checklist y recupera el detalle del mismo ingreso.
    fun volverDelChecklist(ingresoId: Long) {
        if (!sesionValida) return

        val actual = backStack.lastOrNull()

        if (actual is ChecklistRoute && actual.ingresoId == ingresoId) {
            backStack.removeAt(backStack.lastIndex)
        }

        abrirDetalle(ingresoId)
    }

    NavDisplay(backStack = backStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        onBack = { if (backStack.size > 1) {
                backStack.removeAt(backStack.lastIndex)
            }
        },
        entryProvider = { key ->
            when (key) {
                is LoginRoute -> NavEntry(key) {
                    LoginScreen(
                        viewModel = loginViewModel,
                        onLoginSuccess = {}
                    )
                }
                is DashboardRoute -> NavEntry(key) {
                    if (sesionValida && key.userId == uiState.id) {
                        val dashboardViewModel: DashboardViewModel = viewModel(factory = dashboardFactory)
                        DashboardScreen(
                            viewModel = dashboardViewModel,
                            onLogout = { cerrarSesion() },
                            onMenuSeleccionado = { opcion ->
                                when (opcion) {
                                    DashboardMenuOption.REGISTRO -> abrirRegistro()
                                    DashboardMenuOption.DASHBOARD -> volverAlDashboard()
                                    DashboardMenuOption.CHECKLIST -> abrirSeleccionChecklist()
                                    DashboardMenuOption.DIAGNOSTICO -> abrirSeleccionDiagnostico()
                                    else -> Unit
                                }
                            },
                            onEditarIngreso = { ingresoId -> abrirRegistro(ingresoId) },
                            onVerDetalle = { ingresoId -> abrirDetalle(ingresoId) }
                        )
                    }
                }

                is RegistroRoute -> NavEntry(key) {
                    if (sesionValida && tecnicoId != null && key.userId == uiState.id) {
                        // La fábrica pertenece a esta entrada y recibe
                        // el ingreso que corresponde editar.
                        val registroFactory = remember(registroRepository, dashboardRepository, key.ingresoId) {
                            RegistroViewModelFactory(repo = registroRepository, dashboardRepository = dashboardRepository, ingresoId = key.ingresoId) }
                        val registroViewModel: RegistroViewModel =
                            viewModel(factory = registroFactory)
                            RegistroScreen(viewModel = registroViewModel, tecnicoId = tecnicoId, onLogout = {
                                cerrarSesion()
                            },
                                onMenuSeleccionado = { opcion ->
                                    when (opcion) {
                                        DashboardMenuOption.DASHBOARD -> volverAlDashboard()
                                        DashboardMenuOption.REGISTRO -> Unit
                                        DashboardMenuOption.CHECKLIST -> abrirSeleccionChecklist()
                                        DashboardMenuOption.DIAGNOSTICO -> abrirSeleccionDiagnostico()
                                        else -> Unit
                                    }
                                },
                            onVerIngreso = { ingresoId -> abrirDetalle(ingresoId) }
                        )
                    }
                }

                is DetalleIngresoRoute -> NavEntry(key) {
                    if (sesionValida && tecnicoId != null && key.userId == uiState.id) {
                        val detalleFactory = remember(key.ingresoId, dashboardRepository) {
                            DetalleIngresoViewModelFactory(ingresoId = key.ingresoId, repository = dashboardRepository)
                        }
                        val detalleViewModel: DetalleIngresoViewModel = viewModel(factory = detalleFactory)
                        DetalleIngresoScreen(
                            viewModel = detalleViewModel,
                            tecnicoId = tecnicoId,
                            onVolver = { volverAlDashboard() },
                            onEditar = { ingresoId ->
                                abrirRegistro(ingresoId) },
                            onAbrirChecklist = { ingresoId ->
                                abrirChecklist(ingresoId) },
                            onAbrirDiagnostico = { ingresoId -> abrirDiagnostico(ingresoId) }
                        )
                    }
                }
                is ChecklistRoute -> NavEntry(key) {
                    if (sesionValida && tecnicoId != null &&
                        key.userId == uiState.id) {
                        val checklistFactory = remember(
                            key.ingresoId,
                            checklistRepository,
                            dashboardRepository) {
                            ChecklistViewModelFactory(
                                ingresoId = key.ingresoId,
                                repo = checklistRepository,
                                dashboardRepository = dashboardRepository
                            )
                        }

                        val checklistViewModel: ChecklistViewModel = viewModel(
                            factory = checklistFactory
                        )

                        ChecklistScreen(
                            viewModel = checklistViewModel,
                            tecnicoId = tecnicoId,
                            onVolver = {
                                volverDelChecklist(key.ingresoId)
                            }
                        )
                    }
                }
                is SeleccionIngresoChecklistRoute -> NavEntry(key) {
                    if (sesionValida && key.userId == uiState.id) {
                        // Esta instancia pertenece al selector.
                        // No modifica la búsqueda ni los filtros del dashboard.
                        val selectorViewModel: DashboardViewModel = viewModel(
                            factory = dashboardFactory
                        )

                        SeleccionIngresoChecklistScreen(
                            viewModel = selectorViewModel,
                            onVolver = {
                                if (backStack.lastOrNull() == key) {
                                    backStack.removeAt(backStack.lastIndex)
                                }
                            },
                            onSeleccionarIngreso = { ingresoId ->
                                // Conserva el detalle debajo del checklist.
                                // Al salir del checklist se mostrará ese expediente.
                                abrirDetalle(ingresoId)
                                abrirChecklist(ingresoId)
                            }
                        )
                    }
                }
                is DiagnosticoRoute -> NavEntry(key) {
                    if (
                        sesionValida &&
                        tecnicoId != null &&
                        key.userId == uiState.id
                    ) {
                        val diagnosticoFactory = remember(
                            key.ingresoId,
                            diagnosticoRepository,
                            dashboardRepository,
                            checklistRepository
                        ) {
                            DiagnosticoViewModelFactory(
                                ingresoId = key.ingresoId,
                                repository = diagnosticoRepository,
                                dashboardRepository = dashboardRepository,
                                checklistRepository = checklistRepository
                            )
                        }

                        val diagnosticoViewModel: DiagnosticoViewModel = viewModel(
                            factory = diagnosticoFactory
                        )

                        DiagnosticoScreen(
                            viewModel = diagnosticoViewModel,
                            tecnicoId = tecnicoId,
                            onVolver = {
                                volverDelDiagnostico(key.ingresoId)
                            }
                        )
                    }
                }
                is SeleccionIngresoDiagnosticoRoute -> NavEntry(key) {
                    if (sesionValida && key.userId == uiState.id) {
                        // Instancia exclusiva de esta entrada de navegación.
                        // Conserva independientes los filtros del dashboard.
                        val selectorViewModel: DashboardViewModel = viewModel(
                            factory = dashboardFactory
                        )

                        SeleccionIngresoDiagnosticoScreen(
                            viewModel = selectorViewModel,
                            onVolver = {
                                if (backStack.lastOrNull() == key) {
                                    backStack.removeAt(backStack.lastIndex)
                                }
                            },
                            onSeleccionarIngreso = { ingresoId ->
                                // Coloca el detalle debajo del diagnóstico.
                                // Al terminar, el usuario vuelve al expediente.
                                abrirDetalle(ingresoId)
                                abrirDiagnostico(ingresoId)
                            }
                        )
                    }
                }
                else -> error("Ruta no reconocida: $key")
            }
        }
    )
}