package com.example.myapplication.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.myapplication.ui.historial.HistorialScreen
import com.example.myapplication.ui.historial.HistorialViewModel
import com.example.myapplication.ui.historial.HistorialViewModelFactory
import com.example.myapplication.data.local.repository.HistorialRepository
import com.example.myapplication.R
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.repository.DashboardRepository
import com.example.myapplication.data.local.repository.DiagnosticoRepository
import com.example.myapplication.data.local.repository.EvidenciaRepository
import com.example.myapplication.data.repository.ChecklistRepository
import com.example.myapplication.data.repository.RegistroRepository
import com.example.myapplication.ui.camara.CamaraIngresoViewModel
import com.example.myapplication.ui.camara.CamaraIngresoViewModelFactory
import com.example.myapplication.ui.camara.EvidenciasIngresoScreen
import com.example.myapplication.ui.camara.PantallaCamara
import com.example.myapplication.ui.camara.SeleccionIngresoEvidenciasScreen
import com.example.myapplication.ui.checklist.ChecklistScreen
import com.example.myapplication.ui.checklist.ChecklistViewModel
import com.example.myapplication.ui.checklist.ChecklistViewModelFactory
import com.example.myapplication.ui.checklist.SeleccionIngresoChecklistScreen
import com.example.myapplication.ui.dashboard.DashboardMenuOption
import com.example.myapplication.ui.dashboard.DashboardScreen
import com.example.myapplication.ui.dashboard.DashboardViewModel
import com.example.myapplication.ui.dashboard.DashboardViewModelFactory
import com.example.myapplication.ui.dashboard.DetalleIngresoScreen
import com.example.myapplication.ui.dashboard.DetalleIngresoViewModel
import com.example.myapplication.ui.dashboard.DetalleIngresoViewModelFactory
import com.example.myapplication.ui.diagnostico.DiagnosticoScreen
import com.example.myapplication.ui.diagnostico.DiagnosticoViewModel
import com.example.myapplication.ui.diagnostico.DiagnosticoViewModelFactory
import com.example.myapplication.ui.diagnostico.SeleccionIngresoDiagnosticoScreen
import com.example.myapplication.ui.login.LoginScreen
import com.example.myapplication.ui.login.LoginViewModel
import com.example.myapplication.ui.registro.RegistroScreen
import com.example.myapplication.ui.registro.RegistroViewModel
import com.example.myapplication.ui.registro.RegistroViewModelFactory

// Coordina la navegación y las dependencias de cada pantalla.
@Composable
fun AutoClimaApp() {
    val contexto = LocalContext.current.applicationContext

    val baseDeDatos = remember(contexto) {
        AppDatabase.getInstance(contexto) }

    val tecnicoDao = remember(baseDeDatos) {
        baseDeDatos.tecnicoDao() }

    val dashboardRepository = remember(baseDeDatos) {
        DashboardRepository(baseDeDatos = baseDeDatos) }

    val registroRepository = remember(baseDeDatos) {
        RegistroRepository(baseDeDatos = baseDeDatos) }

    val checklistRepository = remember(baseDeDatos) {
        ChecklistRepository(baseDeDatos = baseDeDatos) }

    val diagnosticoRepository = remember(baseDeDatos) {
        DiagnosticoRepository(baseDeDatos = baseDeDatos) }

    val evidenciaRepository = remember(baseDeDatos, contexto) {
        EvidenciaRepository(
            baseDeDatos = baseDeDatos,
            directorioArchivos = contexto.filesDir) }

    val loginFactory = remember(tecnicoDao) {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T {
                require(
                    modelClass.isAssignableFrom(
                        LoginViewModel::class.java)
                ) {
                    "ViewModel desconocido: ${modelClass.name}" }

                @Suppress("UNCHECKED_CAST")
                return LoginViewModel(tecnicoDao) as T } } }

    // La sesión vive fuera de las entradas de navegación.
    val loginViewModel: LoginViewModel = viewModel(
        factory = loginFactory)

    val uiState by loginViewModel.uiState.collectAsStateWithLifecycle()

    val tecnicoId = uiState.tecnicoId

    val sesionValida = uiState.isLoggedIn &&
            tecnicoId != null &&
            tecnicoId > 0 &&
            uiState.id.isNotBlank()

    val dashboardFactory = remember(dashboardRepository) {
        DashboardViewModelFactory(
            repository = dashboardRepository) }

    val backStack = rememberNavBackStack(LoginRoute)

    LaunchedEffect(
        uiState.isLoggedIn,
        uiState.id,
        tecnicoId
    ) {
        if (!sesionValida) {
            if (uiState.isLoggedIn) {
                loginViewModel.resetLoginState() }

            if (
                backStack.size != 1 ||
                backStack.lastOrNull() != LoginRoute
            ) {
                backStack.clear()
                backStack.add(LoginRoute) }
        } else {
            val raiz = backStack.firstOrNull()

            val perteneceAlUsuario = raiz is DashboardRoute &&
                    raiz.userId == uiState.id

            if (!perteneceAlUsuario) {
                backStack.clear()
                backStack.add(
                    DashboardRoute(userId = uiState.id)) } } }

    // Cierra la sesión y deja solo la pantalla de inicio de sesión.
    fun cerrarSesion() {
        loginViewModel.resetLoginState()
        backStack.clear()
        backStack.add(LoginRoute) }

    // Regresa al listado de ingresos y cierra todo lo que estaba encima.
    fun volverAlDashboard() {
        if (!sesionValida) return

        val raiz = backStack.firstOrNull()

        if (
            raiz is DashboardRoute &&
            raiz.userId == uiState.id
        ) {
            while (backStack.size > 1) {
                backStack.removeAt(backStack.lastIndex) }
        } else {
            backStack.clear()
            backStack.add(
                DashboardRoute(userId = uiState.id)) } }

    // Regresa a la pantalla anterior.
    fun volverUnaPantalla() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex) } }

    // Abre el registro nuevo o la edición de un ingreso.
    fun abrirRegistro(ingresoId: Long? = null) {
        if (!sesionValida) return
        if (ingresoId != null && ingresoId <= 0) return
        if (backStack.lastOrNull() is RegistroRoute) return

        backStack.add(
            RegistroRoute(
                userId = uiState.id,
                ingresoId = ingresoId)) }

    // Abre el detalle de un ingreso.
    fun abrirDetalle(ingresoId: Long) {
        if (!sesionValida || ingresoId <= 0) return

        if (backStack.lastOrNull() is RegistroRoute) {
            backStack.removeAt(backStack.lastIndex) }

        val destino = DetalleIngresoRoute(
            userId = uiState.id,
            ingresoId = ingresoId)

        if (backStack.lastOrNull() != destino) {
            backStack.add(destino) } }

    // Registro confirma previamente la salida si tiene cambios.
    fun abrirSelector(destino: NavKey) {
        if (!sesionValida) return
        if (backStack.lastOrNull() == destino) return

        if (backStack.lastOrNull() is RegistroRoute) {
            backStack.removeAt(backStack.lastIndex) }

        if (backStack.lastOrNull() != destino) {
            backStack.add(destino) } }

    // Abre el checklist de un ingreso.
    fun abrirChecklist(ingresoId: Long) {
        if (!sesionValida || ingresoId <= 0) return

        val destino = ChecklistRoute(
            userId = uiState.id,
            ingresoId = ingresoId)

        if (backStack.lastOrNull() != destino) {
            backStack.add(destino) } }

    // Abre el diagnóstico de un ingreso.
    fun abrirDiagnostico(ingresoId: Long) {
        if (!sesionValida || ingresoId <= 0) return

        val destino = DiagnosticoRoute(
            userId = uiState.id,
            ingresoId = ingresoId)

        if (backStack.lastOrNull() != destino) {
            backStack.add(destino) } }

    // Abre las fotos de un ingreso.
    fun abrirEvidencias(ingresoId: Long) {
        if (!sesionValida || ingresoId <= 0) return

        val destino = EvidenciasIngresoRoute(
            userId = uiState.id,
            ingresoId = ingresoId)

        if (backStack.lastOrNull() != destino) {
            backStack.add(destino) } }

    // Abre la pantalla que corresponde a la opción del menú.
    fun seleccionarMenu(opcion: DashboardMenuOption) {
        when (opcion) {
            DashboardMenuOption.DASHBOARD -> volverAlDashboard()

            DashboardMenuOption.REGISTRO -> abrirRegistro()

            DashboardMenuOption.CHECKLIST -> {
                abrirSelector(
                    SeleccionIngresoChecklistRoute(
                        userId = uiState.id)) }

            DashboardMenuOption.DIAGNOSTICO -> {
                abrirSelector(
                    SeleccionIngresoDiagnosticoRoute(
                        userId = uiState.id)) }

            DashboardMenuOption.EVIDENCIAS -> {
                abrirSelector(
                    SeleccionIngresoEvidenciasRoute(
                        userId = uiState.id)) }

            DashboardMenuOption.HISTORIAL -> abrirSelector(HistorialRoute(uiState.id))

            else -> Unit } }

    androidx.compose.runtime.CompositionLocalProvider(
        com.example.myapplication.ui.components.LocalNombreTecnico provides uiState.nombreTecnico
    ) {
    NavDisplay(
        backStack = backStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()),
        onBack = { volverUnaPantalla() },
        entryProvider = { key ->
            NavEntry(key) {
                // Verifica que la entrada pertenezca a la sesión actual.
                val usuarioRuta = when (key) {
                    is HistorialRoute -> key.userId
                    is DashboardRoute -> key.userId
                    is RegistroRoute -> key.userId
                    is DetalleIngresoRoute -> key.userId
                    is ChecklistRoute -> key.userId
                    is DiagnosticoRoute -> key.userId
                    is EvidenciasIngresoRoute -> key.userId
                    is SeleccionIngresoChecklistRoute -> key.userId
                    is SeleccionIngresoDiagnosticoRoute -> key.userId
                    is SeleccionIngresoEvidenciasRoute -> key.userId
                    else -> null }

                if (
                    key != LoginRoute &&
                    (!sesionValida || usuarioRuta != uiState.id)
                ) {
                    return@NavEntry }

                when (key) {
                    LoginRoute -> {
                        LoginScreen(
                            viewModel = loginViewModel,
                            onLoginSuccess = {}) }

                    is DashboardRoute -> {
                        val dashboardViewModel: DashboardViewModel =
                            viewModel(factory = dashboardFactory)

                        DashboardScreen(
                            viewModel = dashboardViewModel,
                            onLogout = { cerrarSesion() },
                            onMenuSeleccionado = { seleccionarMenu(it) },
                            onEditarIngreso = { abrirRegistro(it) },
                            onVerDetalle = { abrirDetalle(it) },
                            onVerFotos = { abrirEvidencias(it) },
                            onAbrirChecklist = { abrirChecklist(it) },
                            onAbrirDiagnostico = { abrirDiagnostico(it) }) }

                    is HistorialRoute -> {
                        val factory = remember(baseDeDatos) {
                            HistorialViewModelFactory(HistorialRepository(baseDeDatos.ingresoDao())) }
                        val historialViewModel: HistorialViewModel = viewModel(factory = factory)
                        HistorialScreen(
                            viewModel = historialViewModel,
                            onVolver = { volverUnaPantalla() },
                            onAbrirIngreso = { abrirDetalle(it) },
                            onMenuSeleccionado = { seleccionarMenu(it) },
                            onLogout = { cerrarSesion() }) }

                    is RegistroRoute -> {
                        val actor = tecnicoId ?: return@NavEntry

                        val registroFactory = remember(
                            registroRepository,
                            dashboardRepository,
                            key.ingresoId
                        ) {
                            RegistroViewModelFactory(
                                repo = registroRepository,
                                dashboardRepository = dashboardRepository,
                                ingresoId = key.ingresoId) }

                        val registroViewModel: RegistroViewModel =
                            viewModel(factory = registroFactory)

                        val vieneDelDetalle = key.ingresoId != null &&
                                (backStack.getOrNull(backStack.lastIndex - 1) as? DetalleIngresoRoute)?.ingresoId == key.ingresoId

                        var mostrarCamara by rememberSaveable {
                            mutableStateOf(false) }

                        var mostrarAvisoGaleria by rememberSaveable {
                            mutableStateOf(false) }

                        BackHandler(enabled = mostrarCamara) {
                            mostrarCamara = false }

                        if (mostrarCamara) {
                            PantallaCamara(
                                onNavigateToGallery = {
                                    val id = key.ingresoId

                                    if (id != null) {
                                        mostrarCamara = false
                                        abrirEvidencias(id)
                                    } else {
                                        mostrarAvisoGaleria = true } },
                                onPhotoTaken = { archivo ->
                                    // Devuelve la captura al mismo formulario.
                                    registroViewModel.onFotoTomada(
                                        archivo.absolutePath)
                                    mostrarCamara = false },
                                onVolver = {
                                    mostrarCamara = false })
                        } else {
                            RegistroScreen(
                                viewModel = registroViewModel,
                                tecnicoId = actor,
                                onTomarFotoClick = {
                                    mostrarCamara = true },
                                onLogout = { cerrarSesion() },
                                onMenuSeleccionado = { opcion ->
                                    if (
                                        opcion != DashboardMenuOption.REGISTRO
                                    ) {
                                        seleccionarMenu(opcion) } },
                                // Si se llegó desde el detalle del mismo ingreso, "Ver detalle" y "Atrás"
                                // llevarían al mismo lugar: se ofrece solo un botón.
                                onVerIngreso = if (vieneDelDetalle) null else { id -> abrirDetalle(id) },
                                onVolver = { volverUnaPantalla() }) }

                        if (mostrarAvisoGaleria) {
                            AlertDialog(
                                onDismissRequest = {
                                    mostrarAvisoGaleria = false },
                                title = {
                                    Text(
                                        stringResource(
                                            R.string.evidence_registration_gallery_title)) },
                                text = {
                                    Text(
                                        stringResource(
                                            R.string.evidence_registration_gallery_message)) },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            mostrarAvisoGaleria = false }
                                    ) {
                                        Text(
                                            stringResource(
                                                R.string.evidence_close)) } }) } }

                    is DetalleIngresoRoute -> {
                        val actor = tecnicoId ?: return@NavEntry

                        val factory = remember(
                            key.ingresoId,
                            dashboardRepository
                        ) {
                            DetalleIngresoViewModelFactory(
                                ingresoId = key.ingresoId,
                                repository = dashboardRepository) }

                        val detalleViewModel: DetalleIngresoViewModel =
                            viewModel(factory = factory)

                        DetalleIngresoScreen(
                            viewModel = detalleViewModel,
                            tecnicoId = actor,
                            onVolver = { volverUnaPantalla() },
                            onEditar = { abrirRegistro(it) },
                            onAbrirChecklist = { abrirChecklist(it) },
                            onAbrirDiagnostico = { abrirDiagnostico(it) },
                            onAbrirEvidencias = { abrirEvidencias(it) }) }

                    is ChecklistRoute -> {
                        val actor = tecnicoId ?: return@NavEntry

                        val factory = remember(
                            key.ingresoId,
                            checklistRepository,
                            dashboardRepository
                        ) {
                            ChecklistViewModelFactory(
                                ingresoId = key.ingresoId,
                                repo = checklistRepository,
                                dashboardRepository = dashboardRepository) }

                        val checklistViewModel: ChecklistViewModel =
                            viewModel(factory = factory)

                        val fotosChecklistFactory = remember(key.ingresoId, evidenciaRepository) {
                            CamaraIngresoViewModelFactory(key.ingresoId, evidenciaRepository) }
                        val fotosChecklist: CamaraIngresoViewModel = viewModel(factory = fotosChecklistFactory)

                        var camaraChecklist by rememberSaveable { mutableStateOf(false) }
                        BackHandler(enabled = camaraChecklist) { camaraChecklist = false }

                        if (camaraChecklist) {
                            PantallaCamara(
                                onNavigateToGallery = { camaraChecklist = false },
                                onPhotoTaken = { archivo ->
                                    // La foto se guarda de inmediato en las evidencias del ingreso.
                                    fotosChecklist.recibirFoto(archivo)
                                    fotosChecklist.guardar(actor)
                                    camaraChecklist = false },
                                onVolver = { camaraChecklist = false })
                        } else {
                            ChecklistScreen(
                                viewModel = checklistViewModel,
                                tecnicoId = actor,
                                onVolver = { volverUnaPantalla() },
                                evidenciasViewModel = fotosChecklist,
                                onAgregarFoto = { camaraChecklist = true }) } }

                    is DiagnosticoRoute -> {
                        val actor = tecnicoId ?: return@NavEntry

                        val factory = remember(
                            key.ingresoId,
                            diagnosticoRepository,
                            dashboardRepository,
                            checklistRepository
                        ) {
                            DiagnosticoViewModelFactory(
                                ingresoId = key.ingresoId,
                                repository = diagnosticoRepository,
                                dashboardRepository = dashboardRepository,
                                checklistRepository = checklistRepository) }

                        val diagnosticoViewModel: DiagnosticoViewModel =
                            viewModel(factory = factory)

                        val fotosFactory = remember(key.ingresoId, evidenciaRepository) {
                            CamaraIngresoViewModelFactory(key.ingresoId, evidenciaRepository) }
                        val fotosDiagnostico: CamaraIngresoViewModel = viewModel(factory = fotosFactory)

                        DiagnosticoScreen(
                            viewModel = diagnosticoViewModel,
                            evidenciasViewModel = fotosDiagnostico,
                            tecnicoId = actor,
                            onVolver = { volverUnaPantalla() },
                            onAbrirChecklist = { abrirChecklist(key.ingresoId) },
                            onAbrirEvidencias = { abrirEvidencias(key.ingresoId) }) }

                    is SeleccionIngresoChecklistRoute -> {
                        val selectorViewModel: DashboardViewModel =
                            viewModel(factory = dashboardFactory)

                        SeleccionIngresoChecklistScreen(
                            viewModel = selectorViewModel,
                            onVolver = { volverUnaPantalla() },
                            onSeleccionarIngreso = { abrirChecklist(it) }) }

                    is SeleccionIngresoDiagnosticoRoute -> {
                        val selectorViewModel: DashboardViewModel =
                            viewModel(factory = dashboardFactory)

                        SeleccionIngresoDiagnosticoScreen(
                            viewModel = selectorViewModel,
                            onVolver = { volverUnaPantalla() },
                            onSeleccionarIngreso = { abrirDiagnostico(it) }) }

                    is SeleccionIngresoEvidenciasRoute -> {
                        val selectorViewModel: DashboardViewModel =
                            viewModel(factory = dashboardFactory)

                        SeleccionIngresoEvidenciasScreen(
                            viewModel = selectorViewModel,
                            onVolver = { volverUnaPantalla() },
                            onSeleccionarIngreso = {
                                abrirEvidencias(it) }) }

                    is EvidenciasIngresoRoute -> {
                        val actor = tecnicoId ?: return@NavEntry

                        val factory = remember(
                            key.ingresoId,
                            evidenciaRepository
                        ) {
                            CamaraIngresoViewModelFactory(
                                ingresoId = key.ingresoId,
                                repository = evidenciaRepository) }

                        val camaraViewModel: CamaraIngresoViewModel =
                            viewModel(factory = factory)

                        EvidenciasIngresoScreen(
                            viewModel = camaraViewModel,
                            tecnicoId = actor,
                            onVolver = { volverUnaPantalla() }) }

                    else -> error("Ruta no reconocida: $key") } } }) } }
