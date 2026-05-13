package com.example.sem8_autenticacinconestadopersistente

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sem8_autenticacinconestadopersistente.DataStore.SessionManager
import com.example.sem8_autenticacinconestadopersistente.Navegacion.AppNavigation
import com.example.sem8_autenticacinconestadopersistente.ViewModel.AuthViewModel
import com.example.sem8_autenticacinconestadopersistente.ui.theme.SEM8_AutenticaciónConEstadoPersistenteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inicializar SessionManager con el contexto de la aplicación
        val sessionManager = SessionManager(applicationContext)

        // 2. Crear un Factory para poder pasarle el SessionManager al ViewModel
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(sessionManager) as T
            }
        }

        // 3. Obtener la instancia del ViewModel
        val authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            SEM8_AutenticaciónConEstadoPersistenteTheme {
                // Usamos Surface en lugar de Scaffold para un contenedor básico
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 4. Llamamos a nuestra navegación principal
                    AppNavigation(authViewModel = authViewModel)
                }
            }
        }
    }
}