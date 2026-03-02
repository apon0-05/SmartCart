package com.example.smartcard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartcard.viewmodel.AuthViewModel



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = Screen.SignUp.route
            ) {
                composable(Screen.SignUp.route) {
                    SignUpScreen(
                        onGoogleClick = {},
                        onSignUp = { _, _, _ ->
                            // ✅ после успешной регистрации идём на Home
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.SignUp.route) { inclusive = true }
                            }
                        },
                        onLogin = {
                            navController.navigate(Screen.Login.route)
                        }
                    )
                }

                composable(Screen.Login.route) {
                    LoginRoute(
                        onGoHome = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        },
                        onGoSignUp = {
                            navController.navigate(Screen.SignUp.route) {
                                popUpTo(Screen.SignUp.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Home.route) {
                    HomeScreen(
                        userFullName = UserSession.fullName,
                        onProfileClick = { /*TODO*/ },
                        onScanProductClick = { navController.navigate(Screen.Camera.route) },
                        onReceiptClick = { /*TODO*/ },
                        onProductsPurchasedClick = { /*TODO*/ },
                        onBottomHome = { /* already */ },
                        onBottomBag = { /*TODO*/ },
                        onBottomCart = { /*TODO*/ },
                        onBottomHistory = { /*TODO*/ }
                    )
                }
                composable(Screen.Camera.route) {
                    CameraScreen(
                        onBack = { navController.popBackStack() }
                    )
                }


            }

        }
    }
}

@Composable
fun SignUpScreen(
    onGoogleClick: () -> Unit,
    onSignUp: (fullName: String, email: String, password: String) -> Unit,
    onLogin: () -> Unit
) {
    val vm: AuthViewModel = viewModel()

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("eskendi16092004@gmail.com") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var agree by remember { mutableStateOf(false) }
    val msg by vm.message.collectAsState()
    val orange = Color(0xFFCF6B2D)
    val orangeSoft = Color(0xFFF2C3A7)
    val linkBlue = Color(0xFF2F80ED)
    val textDark = Color(0xFF2F2F2F)
    val hintGray = Color(0xFF9A9A9A)
    val fieldBorder = Color(0xFF7A6D76)

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

            // Top header with curve + title + icon circle
            HeaderWave(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                backgroundTop = orangeSoft,
                backgroundBottom = Color(0xFFF5D7C7),
                titleColor = textDark
            )

            Spacer(Modifier.height(18.dp))

            // Content card area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
            ) {
                GoogleButton(onClick = onGoogleClick)

                Spacer(Modifier.height(16.dp))

                LabeledOutlinedField(
                    label = "Full name",
                    value = fullName,
                    onValueChange = { fullName = it },
                    placeholder = "Your name",
                    borderColor = fieldBorder,
                    hintColor = hintGray
                )

                Spacer(Modifier.height(12.dp))

                LabeledOutlinedField(
                    label = "Email",
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "name@email.com",
                    borderColor = fieldBorder,
                    hintColor = hintGray,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(Modifier.height(12.dp))

                LabeledOutlinedField(
                    label = "Password",
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "At least 8 characters",
                    borderColor = fieldBorder,
                    hintColor = hintGray,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isPassword = true
                )

                Spacer(Modifier.height(12.dp))

                LabeledOutlinedField(
                    label = "Confirm password",
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = "At least 8 characters",
                    borderColor = fieldBorder,
                    hintColor = hintGray,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isPassword = true
                )

                Spacer(Modifier.height(14.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = agree,
                        onCheckedChange = { agree = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = orange,
                            uncheckedColor = Color(0xFFB9B9B9),
                            checkmarkColor = Color.White
                        )
                    )

                    Spacer(Modifier.width(6.dp))

                    TermsText(
                        onTermsClick = { /* open Terms */ },
                        onPrivacyClick = { /* open Privacy */ },
                        linkColor = linkBlue,
                        textColor = Color(0xFF6B6B6B)
                    )
                }

                Spacer(Modifier.height(16.dp))

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

                GradientPrimaryButton(
                    text = "Sign up",
                    enabled = agree && password.length >= 8 && password == confirmPassword,
                    gradient = Brush.horizontalGradient(listOf(Color(0xFFF1C2A6), orange)),
                    onClick = {
                        vm.register(fullName, email, password){
                            onSignUp(fullName, email, password)
                        }
                    }
                )

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "Already have an account?",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = Color(0xFF8A8A8A),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, orange),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = orange
                    )
                ) {
                    Text("Log in", fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(18.dp))
            }
        }
    }
}

@Composable
fun LoginRoute(
    onGoHome: () -> Unit,
    onGoSignUp: () -> Unit
) {
    val vm: AuthViewModel = viewModel()
    val msg by vm.message.collectAsState()

    // ✅ если логин успешный -> переходим на Home
    LaunchedEffect(msg) {
        if (msg == "Login ✅") {
            vm.clearMessage()   // чтобы не зацикливалось
            onGoHome()
        }
    }

    // ✅ показываем диалог (как у тебя)
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

    LoginScreen(
        onLogin = { email, password ->
            vm.login(email, password) {
                // ❗️ здесь можно оставить пустым, потому что переход делаем через LaunchedEffect
            }
        },
        onGoogleLogin = { /*TODO*/ },
        onSignUp = { onGoSignUp() }
    )
}

@Composable
private fun HeaderWave(
    modifier: Modifier,
    backgroundTop: Color,
    backgroundBottom: Color,
    titleColor: Color
) {
    Box(modifier = modifier) {
        // Draw the peach background with a curved wave
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawBehind {
                    val w = size.width
                    val h = size.height

                    // base gradient background
                    drawRect(
                        brush = Brush.verticalGradient(listOf(backgroundTop, backgroundBottom))
                    )

                    // white curve cutout (bottom-right style)
                    // We mimic the screenshot curve using a big rounded rect shifted.
                    val cutoutPaint = Paint().apply { color = Color(0xFFFAFAFA) }
                    // Using drawRoundRect with BlendMode.Clear is more complex; simplest:
                    // draw a white-ish overlay curve to simulate the wave edge.
                    drawRoundRect(
                        color = Color(0xFFFAFAFA),
                        topLeft = Offset(w * -0.3f, h -700f),
                        size = Size(w * 0.70f, h * 0.95f),
                        cornerRadius = CornerRadius(h * 0.60f, h * 0.56f)
                    )
                }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 22.dp, top = 22.dp, end = 18.dp),
            verticalArrangement = Arrangement.Top
        ) {
            // fake status bar time
            Text(
                text = "9:41",
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )

            Spacer(Modifier.height(36.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sign Up",
                        color = titleColor,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Please enter your details",
                        color = titleColor.copy(alpha = 0.85f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Icon circle (placeholder)
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    // Simple cart-like glyph placeholder
                    Text(
                        text = "🛒",
                        fontSize = 28.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun GoogleButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
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
        // Google "G" (просто текстовый плейсхолдер, чтобы без картинок)
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
        Text("Sign up with Google", fontWeight = FontWeight.SemiBold)
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
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isPassword: Boolean = false
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
            placeholder = { Text(placeholder, color = hintColor) },
            keyboardOptions = keyboardOptions,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
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
private fun TermsText(
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    linkColor: Color,
    textColor: Color
) {
    val noRipple = remember { MutableInteractionSource() }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("I agree with ", color = textColor, fontSize = 14.sp)
        Text(
            "Terms",
            color = linkColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(
                interactionSource = noRipple,
                indication = null
            ) { onTermsClick() }
        )
        Text(" and ", color = textColor, fontSize = 14.sp)
        Text(
            "Privacy",
            color = linkColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(
                interactionSource = noRipple,
                indication = null
            ) { onPrivacyClick() }
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
            .background(if (enabled) gradient else Brush.horizontalGradient(listOf(Color(0xFFE6E6E6), Color(0xFFD9D9D9))))
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

@Preview(showBackground = true, backgroundColor = 0xFFF6F6F6)
@Composable
private fun PreviewSignUp() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = Color(0xFFF6F6F6)) {
            SignUpScreen(
                onGoogleClick = {},
                onSignUp = { _, _, _ -> },
                onLogin = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F6F6)
@Composable
private fun PreviewSignUpwithgoogle() {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ){
        Box(
            modifier = Modifier
                .size(616.dp)
                .clip(CircleShape)
                .background(Color(0xFFF2C3A7))
                .padding(bottom = 600.dp),
            contentAlignment = Alignment.Center
        ){

        }

    }
}