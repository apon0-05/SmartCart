package com.example.smartcard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcard.UserSession
import com.example.smartcard.data.remote.ApiClient
import com.example.smartcard.data.remote.LoginRequest
import com.example.smartcard.data.remote.RegisterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class AuthViewModel : ViewModel() {

    private val api = ApiClient.authApi

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    fun clearMessage() { _message.value = null }

    fun register(fullName: String, email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val res = api.register(
                    RegisterRequest(
                        full_name = fullName,
                        email = email,
                        password = password
                    )
                )

                // ✅ сохраняем сессию
                UserSession.userId = res.id
                UserSession.fullName = res.full_name
                UserSession.email = res.email

                _message.value = "Registered ✅"
                onSuccess()
            } catch (e: HttpException) {
                _message.value = "Server error: ${e.code()}"
            } catch (e: Exception) {
                _message.value = "Network error: ${e.message}"
            }
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val res = api.login(LoginRequest(email, password))

                // ✅ сохраняем сессию
                UserSession.userId = res.id
                UserSession.fullName = res.full_name
                UserSession.email = res.email

                _message.value = "Login ✅"
                onSuccess()
            } catch (e: HttpException) {
                _message.value = "Wrong email/password ❌"
            } catch (e: Exception) {
                _message.value = "Network error: ${e.message}"
            }
        }
    }
}
