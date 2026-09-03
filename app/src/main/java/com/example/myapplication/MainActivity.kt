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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.myapplication.ui.dashboard.DashboardScreen
import com.example.myapplication.ui.login.LoginScreen
import com.example.myapplication.ui.login.LoginViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.serialization.Serializable

/**
 * Definición de las rutas de navegación utilizando Navigation 3.
 */
@Serializable
data object LoginRoute : NavKey // Ruta para la pantalla de inicio de sesión

@Serializable
data class DashboardRoute(val userId: String = "") : NavKey // Ruta para la pantalla del panel principal

/**
 * Actividad principal de la aplicación.
 */
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

/**
 * Composable principal que gestiona el ViewModel y la navegación entre Login y Dashboard.
 */
@Composable
fun AutoClimaApp() {
    // Instancia del ViewModel sin necesidad de fábrica o repositorio
    val loginViewModel: LoginViewModel = viewModel()
    val uiState by loginViewModel.uiState.collectAsStateWithLifecycle()

    // Pila de navegación iniciando en LoginRoute
    val backStack = rememberNavBackStack(LoginRoute)

    // Reacciona cuando cambia el estado de inicio de sesión
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            val currentTop = backStack.lastOrNull()
            if (currentTop !is DashboardRoute) {
                backStack.add(DashboardRoute(userId = uiState.id))
            }
        } else {
            val currentTop = backStack.lastOrNull()
            if (currentTop !is LoginRoute) {
                backStack.clear()
                backStack.add(LoginRoute)
            }
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
                    DashboardScreen(
                        userId = key.userId,
                        onLogout = {
                            loginViewModel.resetLoginState()
                        }
                    )
                }
                else -> error("Ruta no reconocida: $key")
            }
        }
    )
}
