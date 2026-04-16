package com.example.smartcard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

@Composable
fun PersonalDataScreen(
    onBack: () -> Unit,
    onBottomHome: () -> Unit,
    onBottomBag: () -> Unit,
    onBottomCart: () -> Unit,
    onBottomHistory: () -> Unit
) {
    val bg = Color(0xFFF6F6F6)
    val textDark = Color(0xFF2F2F2F)
    val hint = Color(0xFF8A8A8A)
    val fieldBg = Color.White
    val borderColor = Color(0xFFE0E0E0)
    val accent = Color(0xFFCF6B2D)

    val user = FirebaseAuth.getInstance().currentUser

    var name by remember {
        mutableStateOf(
            user?.displayName?.takeIf { it.isNotBlank() }
                ?: user?.email?.substringBefore("@")
                ?: ""
        )
    }
    var email by remember { mutableStateOf(user?.email ?: "") }

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var message by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(26.dp))
                .background(Color.White)
                .padding(18.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("‹", fontSize = 22.sp, color = textDark)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Personal Data",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textDark,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text("Name", color = textDark, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = borderColor,
                    unfocusedBorderColor = borderColor,
                    focusedContainerColor = fieldBg,
                    unfocusedContainerColor = fieldBg
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text("Email-address", color = textDark, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                readOnly = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = borderColor,
                    unfocusedBorderColor = borderColor,
                    focusedContainerColor = fieldBg,
                    unfocusedContainerColor = fieldBg,
                    disabledBorderColor = borderColor,
                    disabledContainerColor = fieldBg
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text("Password", color = textDark, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Current password", color = hint) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = borderColor,
                    unfocusedBorderColor = borderColor,
                    focusedContainerColor = fieldBg,
                    unfocusedContainerColor = fieldBg
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("New password", color = hint) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = borderColor,
                    unfocusedBorderColor = borderColor,
                    focusedContainerColor = fieldBg,
                    unfocusedContainerColor = fieldBg
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Confirm new password", color = hint) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = borderColor,
                    unfocusedBorderColor = borderColor,
                    focusedContainerColor = fieldBg,
                    unfocusedContainerColor = fieldBg
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            if (error != null) {
                Text(
                    text = error!!,
                    color = Color.Red,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (message != null) {
                Text(
                    text = message!!,
                    color = Color(0xFF2E7D32),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    error = null
                    message = null

                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val userEmail = currentUser?.email

                    if (currentUser == null || userEmail.isNullOrBlank()) {
                        error = "User not found"
                        return@Button
                    }

                    if (currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
                        error = "Fill in all password fields"
                        return@Button
                    }

                    if (newPassword != confirmPassword) {
                        error = "New passwords do not match"
                        return@Button
                    }

                    if (newPassword.length < 6) {
                        error = "Password must be at least 6 characters"
                        return@Button
                    }

                    isSaving = true

                    val credential = EmailAuthProvider.getCredential(userEmail, currentPassword)

                    currentUser.reauthenticate(credential)
                        .addOnSuccessListener {
                            currentUser.updatePassword(newPassword)
                                .addOnSuccessListener {
                                    isSaving = false
                                    message = "Password updated successfully"
                                    currentPassword = ""
                                    newPassword = ""
                                    confirmPassword = ""
                                }
                                .addOnFailureListener { e ->
                                    isSaving = false
                                    error = e.message ?: "Failed to update password"
                                }
                        }
                        .addOnFailureListener { e ->
                            isSaving = false
                            error = e.message ?: "Current password is incorrect"
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Save",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            BottomNavBar(
                onHome = onBottomHome,
                onBag = onBottomBag,
                onCart = onBottomCart,
                onHistory = onBottomHistory
            )
        }
    }
}