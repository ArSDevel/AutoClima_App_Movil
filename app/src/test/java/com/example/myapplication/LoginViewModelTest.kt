package com.example.myapplication

import com.example.myapplication.data.local.dao.TecnicoDao
import com.example.myapplication.data.local.entity.Tecnico
import com.example.myapplication.data.local.model.UsuarioListado
import com.example.myapplication.ui.login.LoginViewModel
import com.example.myapplication.util.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// TecnicoDao en memoria: permite probar el inicio de sesión sin Room.
class FakeTecnicoDao : TecnicoDao {
    private val tecnicos = mutableListOf<Tecnico>()

    override suspend fun buscarPorUsuario(usuario: String): Tecnico? {
        return tecnicos.find { it.usuario == usuario } }

    override suspend fun insertar(tecnico: Tecnico): Long {
        tecnicos.add(tecnico)
        return tecnico.tecnicoId }

    override suspend fun obtener(id: Long): Tecnico? {
        return tecnicos.find { it.tecnicoId == id } }

    override fun observarCantidad(): Flow<Int> = flowOf(tecnicos.size)

    override suspend fun contarPorUsuario(usuario: String): Int {
        return tecnicos.count { it.usuario.equals(usuario, ignoreCase = true) } }

    override fun observarListado(): Flow<List<UsuarioListado>> {
        return flowOf(tecnicos.map { UsuarioListado(it.tecnicoId, it.nombre, it.usuario, ingresos = 0, actividad = 0) }) }

    override suspend fun eliminarSinActividad(id: Long, actualId: Long): Int {
        if (id == actualId) return 0
        return if (tecnicos.removeAll { it.tecnicoId == id }) 1 else 0 }

    override suspend fun contarPorUsuarioExcepto(usuario: String, id: Long): Int {
        return tecnicos.count { it.usuario.equals(usuario, ignoreCase = true) && it.tecnicoId != id } }

    private fun reemplazar(id: Long, cambio: (Tecnico) -> Tecnico): Int {
        val posicion = tecnicos.indexOfFirst { it.tecnicoId == id }
        if (posicion < 0) return 0
        tecnicos[posicion] = cambio(tecnicos[posicion])
        return 1 }

    override suspend fun actualizarDatos(id: Long, nombre: String, usuario: String): Int =
        reemplazar(id) { it.copy(nombre = nombre, usuario = usuario) }

    override suspend fun actualizarPassword(id: Long, passwordHash: String): Int =
        reemplazar(id) { it.copy(passwordHash = passwordHash) }

    override suspend fun actualizarRecuperacion(id: Long, recuperacionHash: String): Int =
        reemplazar(id) { it.copy(recuperacionHash = recuperacionHash) }

    override suspend fun restablecerConCodigo(id: Long, passwordHash: String, recuperacionNuevo: String,
                                              recuperacionActual: String): Int {
        val actual = tecnicos.find { it.tecnicoId == id } ?: return 0
        if (actual.recuperacionHash != recuperacionActual) return 0
        return reemplazar(id) { it.copy(passwordHash = passwordHash, recuperacionHash = recuperacionNuevo) } }

    // Para las pruebas: lo que quedó guardado de un técnico.
    fun guardado(id: Long): Tecnico? = tecnicos.find { it.tecnicoId == id } }

// Pruebas del inicio de sesión; el dispatcher de pruebas reemplaza al principal para controlar las corrutinas del ViewModel.
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeTecnicoDao: FakeTecnicoDao
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeTecnicoDao = FakeTecnicoDao()
        viewModel = LoginViewModel(fakeTecnicoDao) }

    @After
    fun tearDown() {
        Dispatchers.resetMain() }

    @Test
    fun initialUiState_isDefaultEmpty() {
        val state = viewModel.uiState.value
        assertEquals("", state.id)
        assertEquals("", state.password)
        assertNull(state.idError)
        assertNull(state.passwordError)
        assertFalse(state.isLoggedIn) }

    @Test
    fun login_withEmptyFields_showsErrorMessages() {
        viewModel.login()

        val state = viewModel.uiState.value
        assertEquals("El usuario no puede estar vacío", state.idError)
        assertEquals("La contraseña no puede estar vacía", state.passwordError)
        assertFalse(state.isLoggedIn) }

    @Test
    fun login_withValidInput_succeeds() = runTest {
        val passHash = PasswordHasher.hash("password123")
        fakeTecnicoDao.insertar(
            Tecnico(
                tecnicoId = 1,
                nombre = "Técnico Test",
                usuario = "01",
                passwordHash = passHash))

        viewModel.onIdChanged("01")
        viewModel.onPasswordChanged("password123")

        viewModel.login()
        // El hash de la contraseña corre en un hilo real: se espera a que la sesión se abra.
        viewModel.uiState.first { it.isLoggedIn }

        val state = viewModel.uiState.value
        assertNull(state.idError)
        assertNull(state.passwordError)
        assertTrue(state.isLoggedIn)
        assertEquals("01", state.id) }

    @Test
    fun resetLoginState_resetsLoggedInToFalse() = runTest {
        val passHash = PasswordHasher.hash("password123")
        fakeTecnicoDao.insertar(
            Tecnico(
                tecnicoId = 1,
                nombre = "Técnico Test",
                usuario = "01",
                passwordHash = passHash))

        viewModel.onIdChanged("01")
        viewModel.onPasswordChanged("password123")
        viewModel.login()
        // El hash de la contraseña corre en un hilo real: se espera a que la sesión se abra.
        viewModel.uiState.first { it.isLoggedIn }

        assertTrue(viewModel.uiState.value.isLoggedIn)

        viewModel.resetLoginState()
        assertFalse(viewModel.uiState.value.isLoggedIn) } }
