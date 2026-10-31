package com.example.myapplication

import com.example.myapplication.data.local.dao.TecnicoDao
import com.example.myapplication.data.local.entity.Tecnico
import com.example.myapplication.data.local.model.UsuarioListado
import com.example.myapplication.ui.usuarios.NuevoUsuarioViewModel
import com.example.myapplication.ui.usuarios.ReglasUsuario
import com.example.myapplication.ui.usuarios.UsuariosViewModel
import com.example.myapplication.util.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// TecnicoDao en memoria con actividad por técnico, para probar el alta y la baja de usuarios sin Room.
internal class DaoUsuarios : TecnicoDao {
    val tecnicos = MutableStateFlow<List<Tecnico>>(emptyList())
    // Técnicos con registros en el sistema; no se pueden eliminar.
    val conActividad = mutableMapOf<Long, Int>()
    private var siguienteId = 1L

    override suspend fun insertar(tecnico: Tecnico): Long {
        val guardado = tecnico.copy(tecnicoId = siguienteId++)
        tecnicos.value = tecnicos.value + guardado
        return guardado.tecnicoId }

    override suspend fun buscarPorUsuario(usuario: String): Tecnico? = tecnicos.value.find { it.usuario == usuario }

    override suspend fun login(usuario: String, passwordHash: String): Tecnico? =
        tecnicos.value.find { it.usuario == usuario && it.passwordHash == passwordHash }

    override suspend fun obtener(id: Long): Tecnico? = tecnicos.value.find { it.tecnicoId == id }

    override fun observarCantidad(): Flow<Int> = tecnicos.map { it.size }

    override suspend fun contarPorUsuario(usuario: String): Int =
        tecnicos.value.count { it.usuario.equals(usuario, ignoreCase = true) }

    override fun observarListado(): Flow<List<UsuarioListado>> = tecnicos.map { lista ->
        lista.map {
            val actividad = conActividad[it.tecnicoId] ?: 0
            UsuarioListado(it.tecnicoId, it.nombre, it.usuario, ingresos = actividad, actividad = actividad,
                tieneRecuperacion = it.recuperacionHash != null) } }

    override suspend fun eliminarSinActividad(id: Long, actualId: Long): Int {
        if (id == actualId || (conActividad[id] ?: 0) > 0) return 0
        val antes = tecnicos.value.size
        tecnicos.value = tecnicos.value.filterNot { it.tecnicoId == id }
        return antes - tecnicos.value.size }

    override suspend fun contarPorUsuarioExcepto(usuario: String, id: Long): Int =
        tecnicos.value.count { it.usuario.equals(usuario, ignoreCase = true) && it.tecnicoId != id }

    private fun reemplazar(id: Long, cambio: (Tecnico) -> Tecnico): Int {
        if (tecnicos.value.none { it.tecnicoId == id }) return 0
        tecnicos.value = tecnicos.value.map { if (it.tecnicoId == id) cambio(it) else it }
        return 1 }

    override suspend fun actualizarDatos(id: Long, nombre: String, usuario: String): Int =
        reemplazar(id) { it.copy(nombre = nombre, usuario = usuario) }

    override suspend fun actualizarPassword(id: Long, passwordHash: String): Int =
        reemplazar(id) { it.copy(passwordHash = passwordHash) }

    override suspend fun actualizarRecuperacion(id: Long, recuperacionHash: String): Int =
        reemplazar(id) { it.copy(recuperacionHash = recuperacionHash) }

    override suspend fun restablecerConCodigo(id: Long, passwordHash: String, recuperacionNuevo: String,
                                              recuperacionActual: String): Int {
        val actual = tecnicos.value.find { it.tecnicoId == id } ?: return 0
        if (actual.recuperacionHash != recuperacionActual) return 0
        return reemplazar(id) { it.copy(passwordHash = passwordHash, recuperacionHash = recuperacionNuevo) } } }

class ReglasUsuarioTest {

    @Test
    fun usuario_validaLongitudYEspacios() {
        assertNull(ReglasUsuario.errorUsuario("erik"))
        assertNull(ReglasUsuario.errorUsuario("  erik  "))
        assertEquals(R.string.usuarios_error_usuario_vacio, ReglasUsuario.errorUsuario("   "))
        assertEquals(R.string.usuarios_error_usuario_corto, ReglasUsuario.errorUsuario("ab"))
        assertEquals(R.string.usuarios_error_usuario_espacios, ReglasUsuario.errorUsuario("juan perez"))
        assertEquals(R.string.usuarios_error_usuario_largo, ReglasUsuario.errorUsuario("a".repeat(31))) }

    @Test
    fun nombre_necesitaAlMenosDosLetras() {
        assertNull(ReglasUsuario.errorNombre("Erik"))
        assertEquals(R.string.usuarios_error_nombre, ReglasUsuario.errorNombre("  E "))
        assertEquals("Erik Pérez", ReglasUsuario.limpiarNombre("  Erik    Pérez ")) }

    @Test
    fun password_largoYDistintaDelUsuario() {
        assertTrue(ReglasUsuario.passwordLargaSuficiente("123456"))
        assertFalse(ReglasUsuario.passwordLargaSuficiente("12345"))
        assertFalse(ReglasUsuario.passwordDistintaDelUsuario("ERIK12", "erik12"))
        assertTrue(ReglasUsuario.passwordDistintaDelUsuario("secreto1", "erik"))
        assertTrue(ReglasUsuario.passwordDistintaDelUsuario("abc", "")) }

    @Test
    fun confirmacion_debeSerIgual() {
        assertTrue(ReglasUsuario.confirmacionCoincide("secreto1", "secreto1"))
        assertFalse(ReglasUsuario.confirmacionCoincide("secreto1", "Secreto1")) } }

@OptIn(ExperimentalCoroutinesApi::class)
class NuevoUsuarioViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var dao: DaoUsuarios
    private lateinit var viewModel: NuevoUsuarioViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        dao = DaoUsuarios()
        viewModel = NuevoUsuarioViewModel(dao) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private fun llenar(nombre: String = "Erik Hernández", usuario: String = "erik",
                       password: String = "secreto1", confirmacion: String = "secreto1") {
        viewModel.onNombreChange(nombre)
        viewModel.onUsuarioChange(usuario)
        viewModel.onPasswordChange(password)
        viewModel.onConfirmacionChange(confirmacion) }

    @Test
    fun formularioVacio_noSePuedeCrear() {
        assertFalse(viewModel.uiState.value.puedeCrear)
        viewModel.crear()
        assertTrue(dao.tecnicos.value.isEmpty()) }

    @Test
    fun contrasenaCorta_oConfirmacionDistinta_bloqueanElBoton() {
        llenar(password = "12345", confirmacion = "12345")
        assertFalse(viewModel.uiState.value.puedeCrear)
        llenar(confirmacion = "otra-cosa")
        assertTrue(viewModel.uiState.value.hayConfirmacionIncorrecta)
        assertFalse(viewModel.uiState.value.puedeCrear) }

    @Test
    fun contrasenaIgualAlUsuario_noSePermite() {
        llenar(usuario = "erik123", password = "ERIK123", confirmacion = "ERIK123")
        assertFalse(viewModel.uiState.value.passwordDistinta)
        assertFalse(viewModel.uiState.value.puedeCrear) }

    @Test
    fun crear_guardaElTecnicoConHashYSinEspacios() = runTest {
        llenar(nombre = "  Erik   Hernández ", usuario = " erik ")
        viewModel.crear()
        val estado = viewModel.uiState.first { it.creado != null }

        val guardado = dao.tecnicos.value.single()
        assertEquals("Erik Hernández", guardado.nombre)
        assertEquals("erik", guardado.usuario)
        assertTrue(PasswordHasher.verificar("secreto1", guardado.passwordHash))
        assertNull(guardado.recuperacionHash)
        assertNull(estado.creado?.codigoRecuperacion)
        assertEquals("erik", estado.creado?.usuario)
        // Las contraseñas no se conservan en el estado una vez guardado.
        assertEquals("", estado.password)
        assertEquals("", estado.confirmacion) }

    @Test
    fun usuarioRepetido_seRechazaSinDistinguirMayusculas() = runTest {
        dao.insertar(Tecnico(nombre = "Otro", usuario = "Erik", passwordHash = "x"))
        llenar(usuario = "erik")
        viewModel.crear()
        advanceUntilIdle()

        val estado = viewModel.uiState.value
        assertEquals(R.string.usuarios_error_usuario_repetido, estado.errorUsuario)
        assertNull(estado.creado)
        assertEquals(1, dao.tecnicos.value.size)
        // Al corregir el usuario desaparece el aviso.
        viewModel.onUsuarioChange("erik2")
        assertNull(viewModel.uiState.value.errorUsuario) }

    @Test
    fun trasCrear_elFormularioQuedaFijo() = runTest {
        llenar()
        viewModel.crear()
        viewModel.uiState.first { it.creado != null }
        viewModel.onNombreChange("Cambiado")
        assertEquals("Erik Hernández", viewModel.uiState.value.nombre)
        assertNotNull(viewModel.uiState.value.creado) } }

@OptIn(ExperimentalCoroutinesApi::class)
class UsuariosViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var dao: DaoUsuarios

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        dao = DaoUsuarios() }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private suspend fun crearViewModel(): UsuariosViewModel {
        // 1 = sesión actual, 2 = sin registros, 3 = con registros.
        dao.insertar(Tecnico(nombre = "Erik", usuario = "erik", passwordHash = "x"))
        dao.insertar(Tecnico(nombre = "Carlos", usuario = "carlos", passwordHash = "x"))
        dao.insertar(Tecnico(nombre = "Laura", usuario = "laura", passwordHash = "x"))
        dao.conActividad[3L] = 12
        return UsuariosViewModel(dao, tecnicoActualId = 1L) }

    @Test
    fun listaTodosLosUsuarios() = runTest {
        val viewModel = crearViewModel()
        val estado = viewModel.uiState.first { !it.cargando }
        assertEquals(3, estado.usuarios.size) }

    @Test
    fun elUsuarioDeLaSesion_nuncaSeElimina() = runTest {
        val viewModel = crearViewModel()
        val estado = viewModel.uiState.first { !it.cargando }
        viewModel.pedirEliminar(estado.usuarios.first { it.tecnicoId == 1L })
        assertNull(viewModel.uiState.value.pendienteEliminar)
        assertNull(viewModel.uiState.value.bloqueado) }

    @Test
    fun conRegistros_seExplicaPorQueNoSePuedeEliminar() = runTest {
        val viewModel = crearViewModel()
        val estado = viewModel.uiState.first { !it.cargando }
        viewModel.pedirEliminar(estado.usuarios.first { it.tecnicoId == 3L })
        assertEquals("laura", viewModel.uiState.value.bloqueado?.usuario)
        assertNull(viewModel.uiState.value.pendienteEliminar)
        assertEquals(3, dao.tecnicos.value.size) }

    @Test
    fun sinRegistros_pideConfirmacionYLuegoElimina() = runTest {
        val viewModel = crearViewModel()
        val estado = viewModel.uiState.first { !it.cargando }
        viewModel.pedirEliminar(estado.usuarios.first { it.tecnicoId == 2L })
        assertEquals("carlos", viewModel.uiState.value.pendienteEliminar?.usuario)
        assertEquals(3, dao.tecnicos.value.size)

        viewModel.confirmarEliminar()
        val final = viewModel.uiState.first { it.usuarioEliminado != null }
        assertEquals("carlos", final.usuarioEliminado)
        assertNull(final.pendienteEliminar)
        assertEquals(listOf("erik", "laura"), dao.tecnicos.value.map { it.usuario }) }

    @Test
    fun cancelar_noEliminaNada() = runTest {
        val viewModel = crearViewModel()
        val estado = viewModel.uiState.first { !it.cargando }
        viewModel.pedirEliminar(estado.usuarios.first { it.tecnicoId == 2L })
        viewModel.cancelarEliminar()
        assertNull(viewModel.uiState.value.pendienteEliminar)
        assertEquals(3, dao.tecnicos.value.size) }

    @Test
    fun siEnElIntervaloGanaActividad_laBaseLoRechazaYSeAvisa() = runTest {
        val viewModel = crearViewModel()
        val estado = viewModel.uiState.first { !it.cargando }
        viewModel.pedirEliminar(estado.usuarios.first { it.tecnicoId == 2L })
        // Carlos registra un ingreso justo antes de confirmar.
        dao.conActividad[2L] = 1
        viewModel.confirmarEliminar()
        val final = viewModel.uiState.first { it.bloqueado != null }
        assertEquals("carlos", final.bloqueado?.usuario)
        assertEquals(3, dao.tecnicos.value.size) } }
