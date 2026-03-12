package com.example.smartcard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartcard.viewmodel.AuthViewModel

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val vm: AuthViewModel = viewModel()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    val msg by vm.message.collectAsState()
    val loading by vm.loading.collectAsState()

    if (msg != null) {
        AlertDialog(
            onDismissRequest = { vm.clearMessage() },
            confirmButton = {
                TextButton(onClick = { vm.clearMessage() }) { Text("OK") }
            },
            title = { Text("Info") },
            text = { Text(msg!!) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Create account", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = confirm,
            onValueChange = { confirm = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Confirm password") },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(Modifier.height(18.dp))

        Button(
            onClick = {
                if (password != confirm) {
                    // просто покажем диалог через message:
                    // (в идеале — отдельный стейт, но так тоже ок)
                    // сделаем быстро:
                    // vm.setMessage(...) — нет, поэтому:
                    // используем Firebase ошибку? не надо.
                } else {
                    vm.signUp(email, password) { onSignUpSuccess() }
                }
            },
            enabled = !loading && email.isNotBlank() && password.length >= 8 && confirm == password,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "Loading..." else "Sign up")
        }

        Spacer(Modifier.height(10.dp))

        TextButton(onClick = onBackToLogin, modifier = Modifier.fillMaxWidth()) {
            Text("Back to login")
        }
    }
}