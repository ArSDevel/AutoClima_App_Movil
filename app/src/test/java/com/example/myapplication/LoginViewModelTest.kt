package com.example.myapplication

import com.example.myapplication.data.local.dao.TecnicoDao
import com.example.myapplication.data.local.entity.Tecnico
import com.example.myapplication.ui.login.LoginViewModel
import com.example.myapplication.util.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

class FakeTecnicoDao : TecnicoDao {
    private val tecnicos = mutableListOf<Tecnico>()

    override suspend fun buscarPorUsuario(usuario: String): Tecnico? {
        return tecnicos.find { it.usuario == usuario }
    }

    override suspend fun login(usuario: String, passwordHash: String): Tecnico? {
        return tecnicos.find { it.usuario == usuario && it.passwordHash == passwordHash }
    }

    override suspend fun insertar(tecnico: Tecnico): Long {
        tecnicos.add(tecnico)
        return tecnicos.size.toLong()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeTecnicoDao: FakeTecnicoDao
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeTecnicoDao = FakeTecnicoDao()
        viewModel = LoginViewModel(fakeTecnicoDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialUiState_isDefaultEmpty() {
        val state = viewModel.uiState.value
        assertEquals("", state.id)
        assertEquals("", state.password)
        assertNull(state.idError)
        assertNull(state.passwordError)
        assertFalse(state.isLoggedIn)
    }

    @Test
    fun login_withEmptyFields_showsErrorMessages() {
        viewModel.login()

        val state = viewModel.uiState.value
        assertEquals("El ID no puede estar vacío", state.idError)
        assertEquals("La contraseña no puede estar vacía", state.passwordError)
        assertFalse(state.isLoggedIn)
    }

    @Test
    fun login_withValidInput_succeeds() = runTest {
        val passHash = PasswordHasher.hash("password123")
        fakeTecnicoDao.insertar(
            Tecnico(
                tecnicoId = 1,
                nombre = "Técnico Test",
                usuario = "01",
                passwordHash = passHash
            )
        )

        viewModel.onIdChanged("01")
        viewModel.onPasswordChanged("password123")

        viewModel.login()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.idError)
        assertNull(state.passwordError)
        assertTrue(state.isLoggedIn)
        assertEquals("01", state.id)
    }

    @Test
    fun resetLoginState_resetsLoggedInToFalse() = runTest {
        val passHash = PasswordHasher.hash("password123")
        fakeTecnicoDao.insertar(
            Tecnico(
                tecnicoId = 1,
                nombre = "Técnico Test",
                usuario = "01",
                passwordHash = passHash
            )
        )

        viewModel.onIdChanged("01")
        viewModel.onPasswordChanged("password123")
        viewModel.login()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isLoggedIn)

        viewModel.resetLoginState()
        assertFalse(viewModel.uiState.value.isLoggedIn)
    }
}
