package com.example.sem8_autenticacinconestadopersistente.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sem8_autenticacinconestadopersistente.DataStore.SessionManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Exponemos el estado de sesión y el correo directamente a la UI
    val isLoggedIn: StateFlow<Boolean?> = sessionManager.isLoggedIn.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null // null representa el estado "Splash/Check" (cargando)
    )

    val userEmail: StateFlow<String> = sessionManager.userEmail.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    fun login(email: String, pass: String, onError: (String) -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            onError("Completa todos los campos")
            return
        }

        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Si Firebase acepta, guardamos manual en DataStore
                    viewModelScope.launch {
                        sessionManager.saveSession(email)
                    }
                } else {
                    onError(task.exception?.message ?: "Error al iniciar sesión")
                }
            }
    }
        fun register (email: String, pass: String, onError: (String) -> Unit) {
            if (email.isBlank() || pass.isBlank()) {
                onError("Completa todos los campos")
                return
            }

            auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        viewModelScope.launch { sessionManager.saveSession(email) }
                    } else {
                        onError(task.exception?.message ?: "Error al registrar usuario")
                    }
                }
        }
            fun logout() {
                auth.signOut() // 1. Cierra en Firebase
                viewModelScope.launch {
                    sessionManager.clearSession() // 2. Limpia DataStore
                }
            }

      }


