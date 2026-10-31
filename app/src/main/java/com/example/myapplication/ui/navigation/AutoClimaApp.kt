package com.example.myapplication.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Modifier
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
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.repository.DashboardRepository
import com.example.myapplication.data.local.repository.DiagnosticoRepository
import com.example.myapplication.data.local.repository.EvidenciaRepository
import com.example.myapplication.data.local.repository.FirmaRepository
import com.example.myapplication.data.reporte.EnvioPorWhatsApp
import com.example.myapplication.data.reporte.GeneradorReportePdf
import com.example.myapplication.data.reporte.SeguimientoActividad
import com.example.myapplication.ui.components.fabricaViewModel
import com.example.myapplication.ui.diagnostico.EnvioReporteViewModel
import com.example.myapplication.ui.entrega.EntregaScreen
import com.example.myapplication.ui.entrega.EntregaViewModel
import com.example.myapplication.ui.firma.FirmaScreen
import com.example.myapplication.ui.firma.FirmaViewModel
import com.example.myapplication.ui.firma.SeleccionIngresoFirmaScreen
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
import com.example.myapplication.ui.theme.AutoClimasGradients
import com.example.myapplication.ui.login.LoginViewModel
import com.example.myapplication.data.respaldo.ServicioRespaldo
import com.example.myapplication.ui.respaldo.RespaldoScreen
import com.example.myapplication.ui.respaldo.RespaldoViewModel
import com.example.myapplication.ui.usuarios.AvisoUsuarios
import com.example.myapplication.ui.usuarios.EditarUsuarioScreen
import com.example.myapplication.ui.usuarios.EditarUsuarioViewModel
import com.example.myapplication.ui.usuarios.NuevoUsuarioScreen
import com.example.myapplication.ui.usuarios.RecuperarPasswordScreen
import com.example.myapplication.ui.usuarios.RecuperarPasswordViewModel
import com.example.myapplication.ui.usuarios.NuevoUsuarioViewModel
import com.example.myapplication.ui.usuarios.PrimerUsoFlow
import com.example.myapplication.ui.usuarios.UsuariosScreen
import com.example.myapplication.ui.usuarios.UsuariosViewModel
import com.example.myapplication.ui.registro.RegistroScreen
import com.example.myapplication.ui.registro.RegistroViewModel
import com.example.myapplication.ui.registro.RegistroViewModelFactory
import com.example.myapplication.ui.camara.PantallaEscanerPlaca

// Coordina la navegación y las dependencias de cada pantalla.

@Composable
fun AutoClimaApp() {
    val contexto = LocalContext.current.applicationContext

    // Base de datos y repositorios compartidos: se crean una vez y los usan todas las pantallas.
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

    val firmaRepository = remember(baseDeDatos, contexto) {
        FirmaRepository(
            baseDeDatos = baseDeDatos,
            directorioArchivos = contexto.filesDir) }

    // Para enviar con una API de mensajería, se cambia EnvioPorWhatsApp por otra implementación de EnvioReporte.
    val generadorReporte = remember(contexto) { GeneradorReportePdf(contexto) }
    val seguimientoActividad = remember(contexto) {
        SeguimientoActividad().also { (contexto as android.app.Application).registerActivityLifecycleCallbacks(it) } }
    val envioReporte = remember(contexto, seguimientoActividad) {
        EnvioPorWhatsApp(contexto) { seguimientoActividad.actual() } }

    // LoginViewModel recibe el DAO, por eso necesita una fábrica propia.
    val loginFactory = remember(tecnicoDao) {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T {
                require(
                    modelClass.isAssignableFrom(
                        LoginViewModel::class.java)) {
                    "ViewModel desconocido: ${modelClass.name}" }

                @Suppress("UNCHECKED_CAST")
                return LoginViewModel(tecnicoDao) as T } } }

    // La sesión vive fuera de las entradas de navegación.
    val loginViewModel: LoginViewModel = viewModel(
        factory = loginFactory)

    val uiState by loginViewModel.uiState.collectAsStateWithLifecycle()

    val tecnicoId = uiState.tecnicoId

    // La sesión es válida solo con técnico autenticado, identificador positivo y usuario no vacío.
    val sesionValida = uiState.isLoggedIn &&
            tecnicoId != null &&
            tecnicoId > 0 &&
            uiState.id.isNotBlank()

    // Primer uso: se decide una sola vez al arrancar. Sin usuarios se muestra la bienvenida y la creación del primero;
    // se conserva aunque el usuario ya exista para poder mostrar la confirmación antes del inicio de sesión.
    val cantidadUsuarios by remember(tecnicoDao) { tecnicoDao.observarCantidad() }
        .collectAsStateWithLifecycle(initialValue = null)
    var primerUso by rememberSaveable { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(cantidadUsuarios) {
        if (primerUso == null && cantidadUsuarios != null) {
            primerUso = cantidadUsuarios == 0 } }
    val primerUsoViewModel: NuevoUsuarioViewModel = viewModel(
        key = "primer_uso",
        factory = remember(tecnicoDao) {
            fabricaViewModel { NuevoUsuarioViewModel(tecnicoDao, generarCodigoRecuperacion = true) } })

    // Respaldo: exporta y restaura; el mismo servicio sirve a la pantalla de Respaldo y a la bienvenida.
    val servicioRespaldo = remember(contexto) { ServicioRespaldo(contexto) }
    val respaldoPrimerUsoViewModel: RespaldoViewModel = viewModel(
        key = "respaldo_primer_uso",
        factory = remember(servicioRespaldo) {
            fabricaViewModel { RespaldoViewModel(servicioRespaldo, contexto.contentResolver) } })

    // Lo último que pasó con los usuarios (creado o editado), para avisarlo en la lista. Guarda el tipo y el usuario.
    var avisoUsuarios by rememberSaveable { mutableStateOf<Pair<Boolean, String>?>(null) }

    val dashboardFactory = remember(dashboardRepository) {
        DashboardViewModelFactory(
            repository = dashboardRepository) }

    val backStack = rememberNavBackStack(LoginRoute)

    // Mantiene la pila de navegación coherente con la sesión: sin sesión válida solo queda el inicio de sesión; con
    // sesión, la raíz es el panel del usuario.
    LaunchedEffect(
        uiState.isLoggedIn,
        uiState.id,
        tecnicoId) {
        if (!sesionValida) {
            if (uiState.isLoggedIn) {
                loginViewModel.resetLoginState() }

            if (
                backStack.size != 1 ||
                backStack.lastOrNull() != LoginRoute) {
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

    // Una vez por semana, con la sesión abierta, se guarda una copia automática dentro del teléfono.
    LaunchedEffect(sesionValida) {
        if (sesionValida) {
            try { withContext(Dispatchers.IO) { servicioRespaldo.crearCopiaAutomaticaSiToca() } }
            catch (cancelacion: kotlinx.coroutines.CancellationException) { throw cancelacion }
            catch (_: Exception) { } } }

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
            raiz.userId == uiState.id) {
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

    // Abre una pantalla de selección del menú; si el registro estaba abierto se retira, porque ya pidió confirmar la
    // salida con cambios sin guardar.
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

    // Abre la firma del cliente.
    fun abrirFirma(ingresoId: Long) {
        if (!sesionValida || ingresoId <= 0) return

        val destino = FirmaRoute(
            userId = uiState.id,
            ingresoId = ingresoId)

        if (backStack.lastOrNull() != destino) {
            backStack.add(destino) } }

    // Abre la entrega. Si se viene de la firma, la reemplaza para que atrás regrese al detalle.
    fun abrirEntrega(ingresoId: Long) {
        if (!sesionValida || ingresoId <= 0) return

        if (backStack.lastOrNull() is FirmaRoute) {
            backStack.removeAt(backStack.lastIndex) }

        val destino = EntregaRoute(
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

    // Abre el formulario para agregar un usuario.
    fun abrirNuevoUsuario() {
        if (!sesionValida) return
        if (backStack.lastOrNull() is NuevoUsuarioRoute) return
        backStack.add(NuevoUsuarioRoute(userId = uiState.id)) }

    // Abre la edición de un usuario; el de la sesión también se edita desde aquí.
    fun abrirEditarUsuario(tecnicoDestinoId: Long) {
        if (!sesionValida || tecnicoDestinoId <= 0) return
        if (backStack.lastOrNull() is EditarUsuarioRoute) return
        backStack.add(EditarUsuarioRoute(userId = uiState.id, tecnicoId = tecnicoDestinoId)) }

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

            DashboardMenuOption.FIRMA -> {
                abrirSelector(
                    SeleccionIngresoFirmaRoute(
                        userId = uiState.id)) }

            DashboardMenuOption.HISTORIAL -> abrirSelector(HistorialRoute(uiState.id))

            DashboardMenuOption.USUARIOS -> abrirSelector(UsuariosRoute(uiState.id))

            DashboardMenuOption.RESPALDO -> abrirSelector(RespaldoRoute(uiState.id)) } }

    when (primerUso) {
        // Todavía no se sabe si hay usuarios: solo el fondo, para no mostrar el inicio de sesión un instante.
        null -> Box(
            modifier = Modifier.fillMaxSize().background(AutoClimasGradients.loginBackground))

        true -> PrimerUsoFlow(
            viewModel = primerUsoViewModel,
            respaldoViewModel = respaldoPrimerUsoViewModel,
            onTerminar = { usuario ->
                loginViewModel.onIdChanged(usuario)
                primerUso = false })

        false ->
    // Hace llegar el nombre del técnico al menú lateral sin pasarlo por cada pantalla.
    androidx.compose.runtime.CompositionLocalProvider(
        com.example.myapplication.ui.components.LocalNombreTecnico provides uiState.nombreTecnico) {
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
                    is UsuariosRoute -> key.userId
                    is NuevoUsuarioRoute -> key.userId
                    is EditarUsuarioRoute -> key.userId
                    is RespaldoRoute -> key.userId
                    is DashboardRoute -> key.userId
                    is RegistroRoute -> key.userId
                    is DetalleIngresoRoute -> key.userId
                    is ChecklistRoute -> key.userId
                    is DiagnosticoRoute -> key.userId
                    is EvidenciasIngresoRoute -> key.userId
                    is SeleccionIngresoChecklistRoute -> key.userId
                    is SeleccionIngresoDiagnosticoRoute -> key.userId
                    is SeleccionIngresoEvidenciasRoute -> key.userId
                    is SeleccionIngresoFirmaRoute -> key.userId
                    is FirmaRoute -> key.userId
                    is EntregaRoute -> key.userId
                    else -> null }

                if (
                    key != LoginRoute && key != RecuperarPasswordRoute &&
                    (!sesionValida || usuarioRuta != uiState.id)) {
                    return@NavEntry }

                when (key) {
                    LoginRoute -> {
                        LoginScreen(
                            viewModel = loginViewModel,
                            onLoginSuccess = {},
                            onOlvidePassword = {
                                if (backStack.lastOrNull() != RecuperarPasswordRoute) {
                                    backStack.add(RecuperarPasswordRoute) } }) }

                    RecuperarPasswordRoute -> {
                        val recuperarViewModel: RecuperarPasswordViewModel = viewModel(
                            factory = remember { fabricaViewModel { RecuperarPasswordViewModel(tecnicoDao) } })

                        RecuperarPasswordScreen(
                            viewModel = recuperarViewModel,
                            onVolver = { volverUnaPantalla() },
                            onTerminar = { usuario ->
                                // Regresa al inicio de sesión con el usuario ya escrito.
                                volverUnaPantalla()
                                if (usuario.isNotEmpty()) loginViewModel.onIdChanged(usuario) }) }

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
                            key.ingresoId) {
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

                        var mostrarEscanerPlaca by rememberSaveable {
                            mutableStateOf(false) }


                        var mostrarAvisoGaleria by rememberSaveable {
                            mutableStateOf(false) }

                        BackHandler(
                            enabled = mostrarCamara || mostrarEscanerPlaca
                        ) {
                            if (mostrarEscanerPlaca) {
                                mostrarEscanerPlaca = false
                            } else {
                                mostrarCamara = false
                            }
                        }

                        if (mostrarEscanerPlaca) {


                            PantallaEscanerPlaca(
                                onPlacaConfirmada = { placa ->
                                    registroViewModel.onPlacaChange(placa)
                                    mostrarEscanerPlaca = false
                                },
                                onVolver = {
                                    mostrarEscanerPlaca = false
                                }
                            )

                        } else if (mostrarCamara) {

                            PantallaCamara(
                                onNavigateToGallery = {
                                    val id = key.ingresoId

                                    if (id != null) {
                                        mostrarCamara = false
                                        abrirEvidencias(id)
                                    } else {
                                        mostrarAvisoGaleria = true
                                    }
                                },
                                onPhotoTaken = { archivo ->
                                    // Devuelve la captura al mismo formulario.
                                    registroViewModel.onFotoTomada(
                                        archivo.absolutePath
                                    )
                                    mostrarCamara = false
                                },
                                onVolver = {
                                    mostrarCamara = false
                                }
                            )

                        } else {

                            RegistroScreen(
                                viewModel = registroViewModel,
                                tecnicoId = actor,
                                onTomarFotoClick = {
                                    mostrarCamara = true
                                },
                                onLogout = {
                                    cerrarSesion()
                                },
                                onMenuSeleccionado = { opcion ->
                                    if (
                                        opcion != DashboardMenuOption.REGISTRO) {
                                        seleccionarMenu(opcion) } },
                                // Si se llegó desde el detalle del mismo ingreso, "Ver detalle" y "Atrás"
                                // llevarían al mismo lugar: se ofrece solo un botón.
                                onVerIngreso = if (vieneDelDetalle) null else { id -> abrirDetalle(id) },
                                onVolver = { volverUnaPantalla() },
                                onEscanearPlacaClick = { mostrarEscanerPlaca = true }) }

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
                                            mostrarAvisoGaleria = false }) {
                                        Text(
                                            stringResource(
                                                R.string.evidence_close)) } }) } }

                    is DetalleIngresoRoute -> {
                        val actor = tecnicoId ?: return@NavEntry

                        val factory = remember(
                            key.ingresoId,
                            dashboardRepository) {
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
                            onAbrirEvidencias = { abrirEvidencias(it) },
                            onAbrirFirma = { abrirFirma(it) },
                            onAbrirEntrega = { abrirEntrega(it) }) }

                    is ChecklistRoute -> {
                        val actor = tecnicoId ?: return@NavEntry

                        val factory = remember(
                            key.ingresoId,
                            checklistRepository,
                            dashboardRepository) {
                            ChecklistViewModelFactory(
                                ingresoId = key.ingresoId,
                                repo = checklistRepository,
                                dashboardRepository = dashboardRepository) }

                        val checklistViewModel: ChecklistViewModel =
                            viewModel(factory = factory)

                        val fotosChecklistFactory = remember(key.ingresoId, evidenciaRepository) {
                            CamaraIngresoViewModelFactory(key.ingresoId, evidenciaRepository) }
                        // El checklist usa el ViewModel de fotos del ingreso para guardarlas como evidencias.
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
                            checklistRepository) {
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

                        // Se envía desde una carpeta de caché; GeneradorReportePdf deja allí el PDF y se vacía en cada envío.
                        val envioViewModel: EnvioReporteViewModel = viewModel(
                            factory = remember(key.ingresoId) {
                                fabricaViewModel {
                                    EnvioReporteViewModel(
                                        ingresoId = key.ingresoId,
                                        envio = envioReporte,
                                        generador = generadorReporte,
                                        dashboardRepository = dashboardRepository,
                                        firmaRepository = firmaRepository,
                                        carpetaTemporal = java.io.File(contexto.cacheDir, "reportes")) } })

                        DiagnosticoScreen(
                            viewModel = diagnosticoViewModel,
                            evidenciasViewModel = fotosDiagnostico,
                            envioViewModel = envioViewModel,
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

                    is SeleccionIngresoFirmaRoute -> {
                        val selectorViewModel: DashboardViewModel =
                            viewModel(factory = dashboardFactory)

                        SeleccionIngresoFirmaScreen(
                            viewModel = selectorViewModel,
                            onVolver = { volverUnaPantalla() },
                            onFirmar = { abrirFirma(it) },
                            onEntregar = { abrirEntrega(it) }) }

                    is FirmaRoute -> {
                        val actor = tecnicoId ?: return@NavEntry

                        val firmaViewModel: FirmaViewModel = viewModel(
                            factory = remember(key.ingresoId) {
                                fabricaViewModel {
                                    FirmaViewModel(
                                        ingresoId = key.ingresoId,
                                        firmaRepository = firmaRepository,
                                        dashboardRepository = dashboardRepository,
                                        diagnosticoRepository = diagnosticoRepository) } })

                        // La clave separa este ViewModel del de otras pantallas porque comparten tipo; abre en el
                        // acta de entrega.
                        val envioFirma: EnvioReporteViewModel = viewModel(
                            key = "envio_firma_${key.ingresoId}",
                            factory = remember(key.ingresoId) {
                                fabricaViewModel {
                                    EnvioReporteViewModel(
                                        ingresoId = key.ingresoId,
                                        envio = envioReporte,
                                        generador = generadorReporte,
                                        dashboardRepository = dashboardRepository,
                                        firmaRepository = firmaRepository,
                                        carpetaTemporal = java.io.File(contexto.cacheDir, "reportes"),
                                        tipoInicial = com.example.myapplication.data.reporte.TipoReporte.ACTA) } })

                        // La clave evita reutilizar el ViewModel de fotos de otra pantalla del mismo ingreso.
                        val fotosFirma: CamaraIngresoViewModel = viewModel(
                            key = "fotos_firma_${key.ingresoId}",
                            factory = remember(key.ingresoId, evidenciaRepository) {
                                CamaraIngresoViewModelFactory(key.ingresoId, evidenciaRepository) })

                        FirmaScreen(
                            viewModel = firmaViewModel,
                            envioViewModel = envioFirma,
                            evidenciasViewModel = fotosFirma,
                            tecnicoId = actor,
                            onVolver = { volverUnaPantalla() },
                            onAbrirEntrega = { abrirEntrega(key.ingresoId) }) }

                    is EntregaRoute -> {
                        val actor = tecnicoId ?: return@NavEntry

                        val entregaViewModel: EntregaViewModel = viewModel(
                            factory = remember(key.ingresoId) {
                                fabricaViewModel {
                                    EntregaViewModel(
                                        ingresoId = key.ingresoId,
                                        repository = dashboardRepository) } })

                        EntregaScreen(
                            viewModel = entregaViewModel,
                            tecnicoId = actor,
                            onVolver = { volverUnaPantalla() },
                            onIrAIngresos = { volverAlDashboard() }) }

                    is UsuariosRoute -> {
                        val actor = tecnicoId ?: return@NavEntry

                        val usuariosViewModel: UsuariosViewModel = viewModel(
                            factory = remember(actor) {
                                fabricaViewModel { UsuariosViewModel(tecnicoDao, actor) } })

                        UsuariosScreen(
                            viewModel = usuariosViewModel,
                            aviso = avisoUsuarios?.let { (creado, usuario) ->
                                if (creado) AvisoUsuarios.Creado(usuario) else AvisoUsuarios.Editado(usuario) },
                            onAvisoMostrado = { avisoUsuarios = null },
                            onNuevoUsuario = { abrirNuevoUsuario() },
                            onEditarUsuario = { abrirEditarUsuario(it) },
                            onVolver = { volverUnaPantalla() },
                            onMenuSeleccionado = { seleccionarMenu(it) },
                            onLogout = { cerrarSesion() }) }

                    is NuevoUsuarioRoute -> {
                        val nuevoUsuarioViewModel: NuevoUsuarioViewModel = viewModel(
                            factory = remember { fabricaViewModel { NuevoUsuarioViewModel(tecnicoDao) } })

                        NuevoUsuarioScreen(
                            viewModel = nuevoUsuarioViewModel,
                            onVolver = { volverUnaPantalla() },
                            onCreado = { usuario ->
                                avisoUsuarios = true to usuario
                                volverUnaPantalla() }) }

                    is RespaldoRoute -> {
                        val actor = tecnicoId ?: return@NavEntry

                        val respaldoViewModel: RespaldoViewModel = viewModel(
                            factory = remember(servicioRespaldo, actor) {
                                fabricaViewModel { RespaldoViewModel(servicioRespaldo, contexto.contentResolver, actor) } })

                        RespaldoScreen(
                            viewModel = respaldoViewModel,
                            onVolver = { volverUnaPantalla() },
                            onMenuSeleccionado = { seleccionarMenu(it) },
                            onLogout = { cerrarSesion() }) }

                    is EditarUsuarioRoute -> {
                        val actor = tecnicoId ?: return@NavEntry

                        val editarViewModel: EditarUsuarioViewModel = viewModel(
                            factory = remember(actor, key.tecnicoId) {
                                fabricaViewModel { EditarUsuarioViewModel(tecnicoDao, actor, key.tecnicoId) } })

                        EditarUsuarioScreen(
                            viewModel = editarViewModel,
                            onVolver = { volverUnaPantalla() },
                            onGuardado = { editado ->
                                // El menú muestra el nombre de la sesión: se actualiza si se editó el propio.
                                if (editado.esPropio) loginViewModel.actualizarNombre(editado.nombre)
                                avisoUsuarios = false to editado.nombre
                                volverUnaPantalla() }) }

                    is EvidenciasIngresoRoute -> {
                        val actor = tecnicoId ?: return@NavEntry

                        val factory = remember(
                            key.ingresoId,
                            evidenciaRepository) {
                            CamaraIngresoViewModelFactory(
                                ingresoId = key.ingresoId,
                                repository = evidenciaRepository) }

                        val camaraViewModel: CamaraIngresoViewModel =
                            viewModel(factory = factory)

                        EvidenciasIngresoScreen(
                            viewModel = camaraViewModel,
                            tecnicoId = actor,
                            onVolver = { volverUnaPantalla() }) }

                    else -> error("Ruta no reconocida: $key") } } }) } } }
