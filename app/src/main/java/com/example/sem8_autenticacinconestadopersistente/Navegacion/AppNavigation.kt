package com.example.sem8_autenticacinconestadopersistente.Navegacion
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sem8_autenticacinconestadopersistente.ViewModel.AuthViewModel

@Composable
fun AppNavigation(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    // 1. Splash/Check State: Mientras DataStore lee el estado (es null al inicio)
    if (isLoggedIn == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // 2. Decisión de ruta inicial
    val startDestination = if (isLoggedIn == true) "home" else "login"

    NavHost(navController = navController, startDestination = startDestination) {

        composable("login") {
            LoginScreen(
                onLoginClick = { email, password, onError ->
                    authViewModel.login(email, password, onError)
                },
                // Si el login es exitoso, el StateFlow (isLoggedIn) cambiará a true.
                // Usamos un LaunchedEffect dentro de LoginScreen para navegar.
                isLoggedIn = isLoggedIn ?: false,
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true } // Evita volver al login con el botón 'Atrás'
                    }
                },
                // NUEVO: Acción para ir a la pantalla de registro
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        // NUEVA RUTA: Pantalla de Registro
        composable("register") {
            RegisterScreen(
                onRegisterClick = { email, password, onError ->
                    authViewModel.register(email, password, onError)
                },
                isLoggedIn = isLoggedIn ?: false,
                onNavigateToHome = {
                    navController.navigate("home") {
                        // Limpiamos todo el backstack para que no pueda volver al registro ni al login
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack() // Vuelve al login
                }
            )
        }

        composable("home") {
            val userEmail by authViewModel.userEmail.collectAsState()
            HomeScreen(
                email = userEmail,
                onLogoutClick = {
                    authViewModel.logout()
                },
                isLoggedIn = isLoggedIn ?: false,
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true } // Limpia el stack
                    }
                }
            )
        }
    }
}

@Composable
fun LoginScreen(
    onLoginClick: (String, String, (String) -> Unit) -> Unit,
    isLoggedIn: Boolean,
    onNavigateToHome: () -> Unit,
    onNavigateToRegister: () -> Unit // 👉 ¡ESTE ES EL PARÁMETRO QUE TE FALTA!
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    // Escucha reactiva: Si isLoggedIn cambia a true, navega a Home
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) onNavigateToHome()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(value = email, onValueChange = { email = it }, label = { Text("Correo") })
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(onClick = { onLoginClick(email, password) { errorMessage = it } }) {
            Text("Iniciar Sesión")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // NUEVO: Botón para navegar al registro
        TextButton(onClick = onNavigateToRegister) {
            Text("¿No tienes cuenta? Regístrate aquí")
        }
    }
}
@Composable
fun RegisterScreen(
    onRegisterClick: (String, String, (String) -> Unit) -> Unit,
    isLoggedIn: Boolean,
    onNavigateToHome: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    // Al igual que en Login, si el ViewModel marca isLoggedIn como true (registro exitoso), saltamos al Home
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) onNavigateToHome()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Crear Nueva Cuenta", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        TextField(value = email, onValueChange = { email = it }, label = { Text("Correo electrónico") })
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña (mín. 6 caracteres)") },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(onClick = { onRegisterClick(email, password) { errorMessage = it } }) {
            Text("Registrarse")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onNavigateBack) {
            Text("Volver al Login")
        }
    }
}

@Composable
fun HomeScreen(
    email: String,
    onLogoutClick: () -> Unit,
    isLoggedIn: Boolean,
    onNavigateToLogin: () -> Unit
) {
    // Escucha reactiva: Si isLoggedIn cambia a false, navega a Login
    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) onNavigateToLogin()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Hola $email", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onLogoutClick) {
            Text("Cerrar Sesión")
        }
    }
}