package com.example.myapplication

import com.example.myapplication.ui.login.LoginViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        viewModel = LoginViewModel()
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
    fun login_withValidInput_succeeds() {
        viewModel.onIdChanged("01")
        viewModel.onPasswordChanged("password123")

        viewModel.login()

        val state = viewModel.uiState.value
        assertNull(state.idError)
        assertNull(state.passwordError)
        assertTrue(state.isLoggedIn)
        assertEquals("01", state.id)
    }

    @Test
    fun resetLoginState_resetsLoggedInToFalse() {
        viewModel.onIdChanged("01")
        viewModel.onPasswordChanged("password123")
        viewModel.login()

        assertTrue(viewModel.uiState.value.isLoggedIn)

        viewModel.resetLoginState()
        assertFalse(viewModel.uiState.value.isLoggedIn)
    }
}
