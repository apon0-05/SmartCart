package com.example.smartcard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartcard.localization.LocalAppStrings
import com.example.smartcard.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onSignUp: () -> Unit
) {
    val vm: AuthViewModel = viewModel()
    val texts = LocalAppStrings.current

    var email by remember { mutableStateOf("apon@gmail.com") }
    var password by remember { mutableStateOf("") }

    val orange = Color(0xFFCF6B2D)
    val orangeSoft = Color(0xFFF2C3A7)
    val textDark = Color(0xFF2F2F2F)
    val hintGray = Color(0xFF9A9A9A)
    val fieldBorder = Color(0xFF7A6D76)

    val msg by vm.message.collectAsState()
    val loading by vm.loading.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFFAFAFA))
        ) {
            LoginHeaderWave(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                backgroundTop = orangeSoft,
                backgroundBottom = Color(0xFFF5D7C7),
                titleColor = textDark,
                title = texts.login,
                subtitle = texts.cantLogin
            )

            Spacer(Modifier.height(22.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
            ) {
                LabeledOutlinedField(
                    label = texts.email,
                    value = email,
                    onValueChange = { email = it },
                    placeholder = texts.emailPlaceholder,
                    borderColor = fieldBorder,
                    hintColor = hintGray,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isPassword = false
                )

                Spacer(Modifier.height(14.dp))

                LabeledOutlinedField(
                    label = texts.password,
                    value = password,
                    onValueChange = { password = it },
                    placeholder = texts.passwordHint,
                    borderColor = fieldBorder,
                    hintColor = hintGray,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isPassword = true
                )

                Spacer(Modifier.height(18.dp))

                if (msg != null) {
                    AlertDialog(
                        onDismissRequest = { vm.clearMessage() },
                        confirmButton = {
                            TextButton(onClick = { vm.clearMessage() }) {
                                Text(texts.ok)
                            }
                        },
                        title = { Text(texts.info) },
                        text = { Text(msg!!) }
                    )
                }

                GradientPrimaryButton(
                    text = if (loading) texts.loading else texts.login,
                    enabled = !loading && email.isNotBlank() && password.length >= 8,
                    gradient = Brush.horizontalGradient(
                        listOf(Color(0xFFF1C2A6), orange)
                    ),
                    onClick = {
                        vm.login(email, password) { onLoginSuccess() }
                    }
                )

                Spacer(Modifier.height(90.dp))

                GoogleButtonLogin(
                    enabled = !loading,
                    text = texts.loginWithGoogle,
                    onClick = {
                        scope.launch {
                            signInWithGoogle(
                                context = context,
                                onSuccess = { onLoginSuccess() },
                                onError = { error ->
                                    println("Google sign-in error: $error")
                                }
                            )
                        }
                    }
                )

                Spacer(Modifier.height(22.dp))

                Text(
                    text = texts.dontHaveAccount,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = Color(0xFF8A8A8A),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(14.dp))

                OutlinedButton(
                    onClick = onSignUp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, orange),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = orange
                    )
                ) {
                    Text(
                        text = texts.signUp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(18.dp))
            }
        }
    }
}

@Composable
private fun LoginHeaderWave(
    modifier: Modifier,
    backgroundTop: Color,
    backgroundBottom: Color,
    titleColor: Color,
    title: String,
    subtitle: String
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawBehind {
                    val w = size.width
                    val h = size.height

                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(backgroundTop, backgroundBottom)
                        )
                    )

                    drawRoundRect(
                        color = Color(0xFFFAFAFA),
                        topLeft = Offset(w * 0.62f, h * 0.02f),
                        size = Size(w * 0.75f, h * 0.98f),
                        cornerRadius = CornerRadius(h * 0.65f, h * 0.65f)
                    )
                }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 22.dp, top = 22.dp, end = 18.dp)
        ) {
            Spacer(Modifier.height(54.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = titleColor,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = subtitle,
                        color = titleColor.copy(alpha = 0.85f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🛒", fontSize = 28.sp)
                }
            }
        }
    }
}

@Composable
private fun LabeledOutlinedField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    borderColor: Color,
    hintColor: Color,
    keyboardOptions: KeyboardOptions,
    isPassword: Boolean
) {
    Column {
        Text(
            text = label,
            color = Color(0xFFE07A46),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 10.dp, bottom = 6.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            singleLine = true,
            placeholder = {
                Text(
                    text = placeholder,
                    color = hintColor
                )
            },
            keyboardOptions = keyboardOptions,
            visualTransformation = if (isPassword) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = borderColor,
                unfocusedBorderColor = borderColor.copy(alpha = 0.9f),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = Color(0xFFCF6B2D)
            )
        )
    }
}

@Composable
private fun GradientPrimaryButton(
    text: String,
    enabled: Boolean,
    gradient: Brush,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(shape)
            .background(
                if (enabled) {
                    gradient
                } else {
                    Brush.horizontalGradient(
                        listOf(Color(0xFFE6E6E6), Color(0xFFD9D9D9))
                    )
                }
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) Color.White else Color(0xFF9B9B9B),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun GoogleButtonLogin(
    enabled: Boolean,
    text: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE3E3E3)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = Color(0xFF2F2F2F)
        )
    ) {
        Text(
            text = "G",
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(Color(0xFFF1F1F1))
                .wrapContentSize(Alignment.Center)
        )
        Spacer(Modifier.width(10.dp))
        Text(text = text, fontWeight = FontWeight.SemiBold)
    }
}