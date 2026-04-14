package com.example.smartcard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartcard.localization.LocalAppStrings
import com.example.smartcard.viewmodel.AuthViewModel

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val vm: AuthViewModel = viewModel()
    val texts = LocalAppStrings.current

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var agreeTerms by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    val msg by vm.message.collectAsState()
    val loading by vm.loading.collectAsState()

    val peachLight = Color(0xFFF7D8C7)
    val peachDark = Color(0xFFE97B39)
    val borderColor = Color(0xFFB8B8B8)
    val labelColor = Color(0xFFE97B39)
    val textGray = Color(0xFF8E8E93)
    val titleColor = Color(0xFF2F2F2F)
    val blueLink = Color(0xFF4F7CFF)

    val dialogMessage = localError ?: msg

    if (dialogMessage != null) {
        AlertDialog(
            onDismissRequest = {
                localError = null
                vm.clearMessage()
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        localError = null
                        vm.clearMessage()
                    }
                ) {
                    Text(texts.ok)
                }
            },
            title = { Text(texts.info) },
            text = { Text(dialogMessage) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(peachLight)
                ) {
                    Box(
                        modifier = Modifier
                            .size(210.dp)
                            .offset(x = 120.dp, y = (-60).dp)
                            .clip(CircleShape)
                            .background(Color(0x33FFFFFF))
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 20.dp, top = 18.dp)
                    ) {
                        Spacer(modifier = Modifier.height(35.dp))

                        Text(
                            text = texts.signUp,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = titleColor
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = texts.chooseLanguage,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = titleColor
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 22.dp)
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ShoppingCart,
                            contentDescription = null,
                            tint = titleColor,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                CustomField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = texts.user,
                    placeholder = texts.user,
                    borderColor = borderColor,
                    labelColor = labelColor
                )

                Spacer(modifier = Modifier.height(14.dp))

                CustomField(
                    value = email,
                    onValueChange = { email = it },
                    label = texts.email,
                    placeholder = texts.emailPlaceholder,
                    borderColor = borderColor,
                    labelColor = labelColor,
                    keyboardType = KeyboardType.Email
                )

                Spacer(modifier = Modifier.height(14.dp))

                CustomField(
                    value = password,
                    onValueChange = { password = it },
                    label = texts.password,
                    placeholder = texts.passwordHint,
                    borderColor = borderColor,
                    labelColor = labelColor,
                    isPassword = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                CustomField(
                    value = confirm,
                    onValueChange = { confirm = it },
                    label = texts.password,
                    placeholder = texts.passwordHint,
                    borderColor = borderColor,
                    labelColor = labelColor,
                    isPassword = true
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    repeat(4) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            thickness = 3.dp,
                            color = Color(0xFFD7D7D7)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = agreeTerms,
                        onCheckedChange = { agreeTerms = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = peachDark,
                            uncheckedColor = Color(0xFFCFCFCF)
                        )
                    )

                    Text(
                        text = texts.dontHaveAccount,
                        color = textGray,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        when {
                            fullName.isBlank() -> {
                                localError = texts.user
                            }
                            email.isBlank() -> {
                                localError = texts.email
                            }
                            password.length < 8 -> {
                                localError = texts.passwordHint
                            }
                            confirm != password -> {
                                localError = texts.info
                            }
                            !agreeTerms -> {
                                localError = texts.chooseLanguage
                            }
                            else -> {
                                vm.signUp(email, password) {
                                    onSignUpSuccess()
                                }
                            }
                        }
                    },
                    enabled = !loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(peachLight, peachDark)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (loading) texts.loading else texts.signUp,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = texts.dontHaveAccount,
                    color = textGray,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.5.dp, peachDark, RoundedCornerShape(16.dp))
                        .clickable { onBackToLogin() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = texts.login,
                        color = peachDark,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    borderColor: Color,
    labelColor: Color,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = labelColor,
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 12.dp, bottom = 6.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            placeholder = {
                Text(
                    text = placeholder,
                    color = Color(0xFFB0B0B0)
                )
            },
            singleLine = true,
            visualTransformation = if (isPassword) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = borderColor,
                unfocusedBorderColor = borderColor,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                cursorColor = labelColor,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
    }
}