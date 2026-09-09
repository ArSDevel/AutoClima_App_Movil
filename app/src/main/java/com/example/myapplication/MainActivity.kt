package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.ui.dashboard.DashboardScreen
import com.example.myapplication.ui.login.LoginScreen
import com.example.myapplication.ui.login.LoginViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.serialization.Serializable
import androidx.compose.runtime.remember
import com.example.myapplication.ui.dashboard.DashboardViewModel
import com.example.myapplication.ui.dashboard.DashboardViewModelFactory
import com.example.myapplication.data.local.repository.DashboardRepository
import com.example.myapplication.data.repository.RegistroRepository
import android.content.pm.ApplicationInfo
import androidx.room.withTransaction

/**
 * Definición de las rutas de navegación utilizando Navigation 3.
 */
@Serializable
data object LoginRoute : NavKey // Ruta para la pantalla de inicio de sesión

@Serializable
data class DashboardRoute(val userId: String = "") : NavKey // Ruta para la pantalla del panel principal




// Actividad principal de la aplicación.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AutoClimaApp()
                }
            }
        }
    }
}

// Composable principal que gestiona el ViewModel y la navegación entre Login y Dashboard.
@Composable
fun AutoClimaApp() {
    // 1. Obtener el contexto actual de la aplicación
    val contexto = LocalContext.current

    // 2. Obtener la instancia de la base de datos y su respectivo DAO
    // NOTA: Ajusta el método si en tu AppDatabase usas una función Singleton personalizada (ej. AppDatabase.getInstance(contexto))
    val baseDeDatos = AppDatabase.getInstance(contexto)
    val tecnicoDao = baseDeDatos.tecnicoDao()

    // 3. Crear una fábrica (Factory) para inyectar de forma segura el DAO a tu LoginViewModel
    val loginViewModel: LoginViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LoginViewModel(tecnicoDao) as T
            }
        }
    )

    val uiState by loginViewModel.uiState.collectAsStateWithLifecycle()

    // Pila de navegación iniciando en LoginRoute
    val backStack = rememberNavBackStack(LoginRoute)

    // Reacciona cuando cambia el estado de inicio de sesión
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            // Al iniciar sesión, Login deja de estar en la pila
            if (backStack.lastOrNull() !is DashboardRoute) {
                backStack.clear()
                backStack.add(
                    DashboardRoute(userId = uiState.id)
                )
            }
        } else {
            // Al cerrar sesión
            backStack.clear()
            backStack.add(LoginRoute)
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeAt(backStack.lastIndex)
            }
        },
        entryProvider = { key ->
            when (key) {
                is LoginRoute -> NavEntry(key) {
                    LoginScreen(
                        viewModel = loginViewModel,
                        onLoginSuccess = {
                            // Manejado por LaunchedEffect cuando isLoggedIn cambia a true
                        }
                    )
                }
                is DashboardRoute -> NavEntry(key) {
                    // TEMPORAL: datos de prueba para comprobar el Dashboard.
                    LaunchedEffect(key.userId) {
                        val esDepuracion =
                            (contexto.applicationInfo.flags and
                                    ApplicationInfo.FLAG_DEBUGGABLE) != 0

                        if (esDepuracion) {
                            baseDeDatos.withTransaction {
                                val tecnico = baseDeDatos.tecnicoDao()
                                    .buscarPorUsuario(key.userId)

                                val vehiculoExistente = baseDeDatos.vehiculoDao()
                                    .buscarPorPlaca("DEMO-001")

                                if (tecnico != null && vehiculoExistente == null) {
                                    val registroRepository = RegistroRepository(
                                        clienteDao = baseDeDatos.clienteDao(),
                                        vehiculoDao = baseDeDatos.vehiculoDao(),
                                        ingresoDao = baseDeDatos.ingresoDao()
                                    )

                                    registroRepository.registrarIngreso(
                                        placa = "DEMO-001",
                                        nombreCliente = "Cliente de prueba",
                                        modelo = "Honda Civic",
                                        telefonoCliente = "8100000000",
                                        color = "Rojo",
                                        emailCliente = "prueba@example.com",
                                        numeroSerie = "SERIE-DEMO-001",
                                        fechaEntrada = System.currentTimeMillis(),
                                        tecnicoId = tecnico.tecnicoId
                                    )
                                }
                            }
                        }
                    }


                    val dashboardRepository = remember(baseDeDatos) {
                        DashboardRepository(ingresoDao = baseDeDatos.ingresoDao())
                    }
                    val dashboardFactory = remember(dashboardRepository) {
                        DashboardViewModelFactory(repository = dashboardRepository)
                    }
                    val dashboardViewModel: DashboardViewModel = viewModel(factory = dashboardFactory)
                    DashboardScreen(viewModel = dashboardViewModel, onLogout = { loginViewModel.resetLoginState() }
                    )
                }
                else -> error("Ruta no reconocida: $key")
            }
        }
    )
}
