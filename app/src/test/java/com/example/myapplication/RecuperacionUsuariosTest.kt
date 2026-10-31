package com.example.myapplication

import com.example.myapplication.data.local.entity.Tecnico
import com.example.myapplication.ui.login.LoginViewModel
import com.example.myapplication.ui.usuarios.EditarUsuarioViewModel
import com.example.myapplication.ui.usuarios.NuevoUsuarioViewModel
import com.example.myapplication.ui.usuarios.RecuperarPasswordViewModel
import com.example.myapplication.util.CodigoRecuperacion
import com.example.myapplication.util.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.security.MessageDigest

class PasswordHasherTest {

    @Test
    fun verificar_aceptaLaCorrectaYRechazaLasDemas() {
        val guardado = PasswordHasher.hash("secreto1")
        assertTrue(PasswordHasher.verificar("secreto1", guardado))
        assertFalse(PasswordHasher.verificar("secreto2", guardado))
        assertFalse(PasswordHasher.verificar("", guardado)) }

    @Test
    fun cadaHashUsaUnaSalDistinta() {
        val uno = PasswordHasher.hash("secreto1")
        val dos = PasswordHasher.hash("secreto1")
        assertNotEquals(uno, dos)
        assertTrue(uno.startsWith("pbkdf2$"))
        assertFalse(uno.contains("secreto1")) }

    @Test
    fun reconoceElFormatoAnteriorSinSal() {
        val anterior = MessageDigest.getInstance("SHA-256").digest("1234".toByteArray())
            .joinToString("") { "%02x".format(it) }
        assertTrue(PasswordHasher.verificar("1234", anterior))
        assertFalse(PasswordHasher.verificar("12345", anterior))
        assertTrue(PasswordHasher.requiereActualizar(anterior))
        assertFalse(PasswordHasher.requiereActualizar(PasswordHasher.hash("1234"))) }

    @Test
    fun textoGuardadoMalFormado_nuncaCoincide() {
        assertFalse(PasswordHasher.verificar("x", "pbkdf2\$abc"))
        assertFalse(PasswordHasher.verificar("x", "pbkdf2\$0\$aa\$bb"))
        assertFalse(PasswordHasher.verificar("x", "pbkdf2\$120000\$zz\$bb"))
        assertFalse(PasswordHasher.verificar("x", "")) } }

class CodigoRecuperacionTest {

    @Test
    fun generar_tieneDoceCaracteresEnGruposDeCuatro() {
        repeat(20) {
            val codigo = CodigoRecuperacion.generar()
            assertTrue(codigo, Regex("[A-Z2-9]{4}-[A-Z2-9]{4}-[A-Z2-9]{4}").matches(codigo))
            assertTrue(CodigoRecuperacion.tieneFormatoValido(codigo)) } }

    @Test
    fun noSeRepiten() {
        assertEquals(50, (1..50).map { CodigoRecuperacion.generar() }.toSet().size) }

    @Test
    fun normalizar_aceptaMinusculasGuionesYEspacios() {
        assertEquals("K7QM2XPD9TVC", CodigoRecuperacion.normalizar(" k7qm-2xpd 9tvc ")) }

    @Test
    fun formatoInvalido_seRechaza() {
        assertFalse(CodigoRecuperacion.tieneFormatoValido("ABCD-EFGH"))
        assertFalse(CodigoRecuperacion.tieneFormatoValido("0000-0000-0000")) } }

@OptIn(ExperimentalCoroutinesApi::class)
class EdicionYRecuperacionTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var dao: DaoUsuarios

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        dao = DaoUsuarios() }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    // 1 = sesión actual (erik), 2 = otro técnico (carlos).
    private suspend fun sembrar() {
        dao.insertar(Tecnico(nombre = "Erik", usuario = "erik", passwordHash = PasswordHasher.hash("secreto1")))
        dao.insertar(Tecnico(nombre = "Carlos", usuario = "carlos", passwordHash = PasswordHasher.hash("clave123"))) }

    // ---- Editar usuario ----

    @Test
    fun editarOtro_cambiaNombreYUsuario() = runTest {
        sembrar()
        val vm = EditarUsuarioViewModel(dao, tecnicoActualId = 1, tecnicoId = 2)
        vm.uiState.first { !it.cargando }
        assertFalse(vm.uiState.value.puedeGuardar)
        vm.onNombreChange("Carlos Pérez")
        vm.onUsuarioChange("cperez")
        vm.guardar()
        val estado = vm.uiState.first { it.guardado != null }
        assertEquals("cperez", dao.tecnicos.value.first { it.tecnicoId == 2L }.usuario)
        assertEquals("Carlos Pérez", estado.guardado?.nombre)
        assertFalse(estado.guardado!!.esPropio) }

    @Test
    fun editarOtro_noPermiteUsuarioOcupado() = runTest {
        sembrar()
        val vm = EditarUsuarioViewModel(dao, 1, 2)
        vm.uiState.first { !it.cargando }
        vm.onUsuarioChange("ERIK")
        vm.guardar()
        vm.uiState.first { it.errorUsuarioRepetido != null }
        assertEquals(R.string.usuarios_error_usuario_repetido, vm.uiState.value.errorUsuario)
        assertEquals("carlos", dao.tecnicos.value.first { it.tecnicoId == 2L }.usuario) }

    @Test
    fun restablecerContrasenaDeOtro_noPideLaActual() = runTest {
        sembrar()
        val vm = EditarUsuarioViewModel(dao, 1, 2)
        vm.uiState.first { !it.cargando }
        vm.onPasswordNuevaChange("nueva-clave")
        vm.onConfirmacionChange("nueva-clave")
        assertTrue(vm.uiState.value.puedeGuardar)
        vm.guardar()
        vm.uiState.first { it.guardado != null }
        assertTrue(PasswordHasher.verificar("nueva-clave", dao.tecnicos.value.first { it.tecnicoId == 2L }.passwordHash)) }

    @Test
    fun cambiarContrasenaPropia_exigeLaActual() = runTest {
        sembrar()
        val vm = EditarUsuarioViewModel(dao, 1, 1)
        vm.uiState.first { !it.cargando }
        vm.onPasswordNuevaChange("nueva-clave")
        vm.onConfirmacionChange("nueva-clave")
        // Sin la actual no se puede guardar.
        assertFalse(vm.uiState.value.puedeGuardar)
        vm.onPasswordActualChange("incorrecta")
        vm.guardar()
        // La comprobación de la contraseña corre en un hilo real: se espera el resultado.
        vm.uiState.first { it.errorPasswordActual != null }
        assertEquals(R.string.usuarios_error_password_actual, vm.uiState.value.errorPasswordActual)
        assertTrue(PasswordHasher.verificar("secreto1", dao.tecnicos.value.first { it.tecnicoId == 1L }.passwordHash))
        vm.onPasswordActualChange("secreto1")
        vm.guardar()
        vm.uiState.first { it.guardado != null }
        assertTrue(PasswordHasher.verificar("nueva-clave", dao.tecnicos.value.first { it.tecnicoId == 1L }.passwordHash)) }

    @Test
    fun editarElPropio_noCambiaSuUsuarioDeSesion() = runTest {
        sembrar()
        val vm = EditarUsuarioViewModel(dao, 1, 1)
        vm.uiState.first { !it.cargando }
        vm.onUsuarioChange("otro")
        assertEquals("erik", vm.uiState.value.usuario)
        vm.onNombreChange("Erik Hernández")
        vm.guardar()
        val estado = vm.uiState.first { it.guardado != null }
        assertTrue(estado.guardado!!.esPropio)
        assertEquals("erik", dao.tecnicos.value.first { it.tecnicoId == 1L }.usuario)
        assertEquals("Erik Hernández", dao.tecnicos.value.first { it.tecnicoId == 1L }.nombre) }

    @Test
    fun generarCodigo_pideLaContrasenaYReemplazaElAnterior() = runTest {
        sembrar()
        val vm = EditarUsuarioViewModel(dao, 1, 1)
        vm.uiState.first { !it.cargando }
        assertFalse(vm.uiState.value.tieneRecuperacion)
        vm.pedirCodigo()
        vm.onPasswordCodigoChange("incorrecta")
        vm.confirmarCodigo()
        vm.uiState.first { it.errorPasswordCodigo != null }
        assertNull(dao.tecnicos.value.first { it.tecnicoId == 1L }.recuperacionHash)

        vm.onPasswordCodigoChange("secreto1")
        vm.confirmarCodigo()
        val estado = vm.uiState.first { it.codigoNuevo != null }
        val codigo = estado.codigoNuevo!!
        val guardado = dao.tecnicos.value.first { it.tecnicoId == 1L }.recuperacionHash
        assertNotNull(guardado)
        assertTrue(PasswordHasher.verificar(CodigoRecuperacion.normalizar(codigo), guardado!!))
        assertFalse(guardado.contains(CodigoRecuperacion.normalizar(codigo)))
        vm.cerrarCodigo()
        assertNull(vm.uiState.value.codigoNuevo) }

    @Test
    fun elCodigoSoloSeGeneraEnElPerfilPropio() = runTest {
        sembrar()
        val vm = EditarUsuarioViewModel(dao, 1, 2)
        vm.uiState.first { !it.cargando }
        vm.pedirCodigo()
        assertFalse(vm.uiState.value.pidiendoPasswordCodigo) }

    // ---- Primer uso ----

    @Test
    fun primerUso_generaElCodigoDeRecuperacion() = runTest {
        val vm = NuevoUsuarioViewModel(dao, generarCodigoRecuperacion = true)
        vm.onNombreChange("Erik Hernández"); vm.onUsuarioChange("erik")
        vm.onPasswordChange("secreto1"); vm.onConfirmacionChange("secreto1")
        vm.crear()
        val creado = vm.uiState.first { it.creado != null }.creado!!
        val hash = dao.tecnicos.value.single().recuperacionHash
        assertNotNull(creado.codigoRecuperacion)
        assertTrue(PasswordHasher.verificar(CodigoRecuperacion.normalizar(creado.codigoRecuperacion!!), hash!!)) }

    // ---- Olvidé mi contraseña ----

    private suspend fun tecnicoConCodigo(): String {
        val codigo = CodigoRecuperacion.generar()
        dao.insertar(Tecnico(nombre = "Erik", usuario = "erik", passwordHash = PasswordHasher.hash("vieja-clave"),
            recuperacionHash = PasswordHasher.hash(CodigoRecuperacion.normalizar(codigo))))
        return codigo }

    private fun RecuperarPasswordViewModel.llenar(usuario: String, codigo: String, password: String = "nueva-clave") {
        onUsuarioChange(usuario); onCodigoChange(codigo); onPasswordChange(password); onConfirmacionChange(password) }

    @Test
    fun recuperar_conElCodigoCorrecto_cambiaLaContrasenaYEntregaUnoNuevo() = runTest {
        val codigo = tecnicoConCodigo()
        val vm = RecuperarPasswordViewModel(dao)
        // Se acepta en minúsculas y sin guiones.
        vm.llenar("erik", codigo.lowercase().replace("-", ""))
        assertTrue(vm.uiState.value.puedeRestablecer)
        vm.restablecer()
        val resultado = vm.uiState.first { it.resultado != null }.resultado!!
        val guardado = dao.tecnicos.value.single()
        assertTrue(PasswordHasher.verificar("nueva-clave", guardado.passwordHash))
        assertFalse(PasswordHasher.verificar("vieja-clave", guardado.passwordHash))
        assertNotEquals(codigo, resultado.codigoNuevo)
        assertTrue(PasswordHasher.verificar(CodigoRecuperacion.normalizar(resultado.codigoNuevo), guardado.recuperacionHash!!))
        assertFalse(PasswordHasher.verificar(CodigoRecuperacion.normalizar(codigo), guardado.recuperacionHash!!)) }

    @Test
    fun recuperar_conCodigoIncorrecto_oUsuarioInexistente_dalMismoError() = runTest {
        tecnicoConCodigo()
        val incorrecto = RecuperarPasswordViewModel(dao)
        incorrecto.llenar("erik", "ABCD-EFGH-JKLM")
        incorrecto.restablecer()
        incorrecto.uiState.first { it.error != null }
        val inexistente = RecuperarPasswordViewModel(dao)
        inexistente.llenar("nadie", "ABCD-EFGH-JKLM")
        inexistente.restablecer()
        inexistente.uiState.first { it.error != null }
        assertEquals(R.string.recuperar_error_datos, incorrecto.uiState.value.error)
        assertEquals(R.string.recuperar_error_datos, inexistente.uiState.value.error)
        assertTrue(PasswordHasher.verificar("vieja-clave", dao.tecnicos.value.single().passwordHash)) }

    @Test
    fun recuperar_unUsuarioSinCodigo_noPuedeRestablecer() = runTest {
        dao.insertar(Tecnico(nombre = "Erik", usuario = "erik", passwordHash = PasswordHasher.hash("vieja-clave")))
        val vm = RecuperarPasswordViewModel(dao)
        vm.llenar("erik", "ABCD-EFGH-JKLM")
        vm.restablecer()
        vm.uiState.first { it.error != null }
        assertEquals(R.string.recuperar_error_datos, vm.uiState.value.error)
        assertNull(vm.uiState.value.resultado) }

    @Test
    fun recuperar_elCodigoSoloSirveUnaVez() = runTest {
        val codigo = tecnicoConCodigo()
        val primero = RecuperarPasswordViewModel(dao)
        primero.llenar("erik", codigo)
        primero.restablecer()
        primero.uiState.first { it.resultado != null }
        val segundo = RecuperarPasswordViewModel(dao)
        segundo.llenar("erik", codigo, password = "otra-clave")
        segundo.restablecer()
        segundo.uiState.first { it.error != null }
        assertNull(segundo.uiState.value.resultado)
        assertTrue(PasswordHasher.verificar("nueva-clave", dao.tecnicos.value.single().passwordHash)) }

    // ---- Inicio de sesión con contraseñas del formato anterior ----

    @Test
    fun login_conHashAnterior_iniciaSesionYLoActualiza() = runTest {
        val anterior = MessageDigest.getInstance("SHA-256").digest("1234".toByteArray())
            .joinToString("") { "%02x".format(it) }
        dao.insertar(Tecnico(nombre = "Erik", usuario = "erik", passwordHash = anterior))
        val login = LoginViewModel(dao)
        login.onIdChanged("erik"); login.onPasswordChanged("1234")
        login.login()
        login.uiState.first { it.isLoggedIn }
        advanceUntilIdle()
        val guardado = dao.tecnicos.value.single().passwordHash
        assertFalse(PasswordHasher.requiereActualizar(guardado))
        assertTrue(PasswordHasher.verificar("1234", guardado)) } }
