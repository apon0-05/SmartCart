package com.example.smartcard

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartcard.localization.LocalAppStrings
import com.example.smartcard.ui.components.SmartCartGoogleAuthButton
import com.example.smartcard.ui.components.SmartCartHeaderShape
import com.example.smartcard.ui.components.SmartCartInputField
import com.example.smartcard.ui.components.SmartCartPasswordInputField
import com.example.smartcard.ui.components.SmartCartPrimaryActionButton
import com.example.smartcard.ui.components.SmartCartSecondaryOutlineButton
import com.example.smartcard.ui.components.SmartCartSegmentedLine
import com.example.smartcard.viewmodel.AuthViewModel

private const val WEB_CLIENT_ID =
    "1090740511419-bo6team9gnufaaesjcieo15rciq4lbjk.apps.googleusercontent.com"

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onSignUp: () -> Unit,
) {
    val vm: AuthViewModel = viewModel()
    val texts = LocalAppStrings.current
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val msg by vm.message.collectAsState()
    val loading by vm.loading.collectAsState()

    val canSubmit = !loading && email.isNotBlank() && password.length >= 8

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        vm.handleGoogleResult(
            data = result.data,
            resultCode = result.resultCode,
            webClientId = WEB_CLIENT_ID,
            packageName = context.packageName,
            onSuccess = { onLoginSuccess() }
        )
    }

    if (msg != null) {
        AlertDialog(
            onDismissRequest = { vm.clearMessage() },
            confirmButton = {
                TextButton(onClick = { vm.clearMessage() }) { Text(texts.ok) }
            },
            title = { Text(texts.info) },
            text = { Text(msg!!) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(horizontal = 22.dp, vertical = 12.dp)
    ) {
        Column {
            SmartCartHeaderShape(
                title = texts.login,
                subtitle = texts.cantLogin,
                badge = {
                    Image(
                        painter = painterResource(id = R.drawable.ic_cart_phone),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            )

            Spacer(Modifier.height(18.dp))

            SmartCartInputField(
                label = texts.email,
                value = email,
                onValueChange = { email = it },
                placeholder = texts.emailPlaceholder
            )

            Spacer(Modifier.height(8.dp))

            SmartCartPasswordInputField(
                label = texts.password,
                value = password,
                onValueChange = { password = it },
                placeholder = texts.passwordHint
            )

            Spacer(Modifier.height(9.dp))
            SmartCartSegmentedLine()

            Spacer(Modifier.height(14.dp))

            SmartCartPrimaryActionButton(
                text = texts.login,
                onClick = { vm.login(email, password) { onLoginSuccess() } },
                enabled = canSubmit,
                loading = loading
            )
        }

        Spacer(Modifier.height(18.dp))

        Column {
            SmartCartGoogleAuthButton(
                text = texts.loginWithGoogle,
                onClick = {
                    vm.clearMessage()
                    val intent = vm.googleSignInIntent(context, WEB_CLIENT_ID)
                    googleLauncher.launch(intent)
                },
                enabled = !loading
            )

            Spacer(Modifier.height(18.dp))

            Text(
                text = texts.dontHaveAccount,
                color = AppColors.TextHint,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Center
            )

            SmartCartSecondaryOutlineButton(
                text = texts.signUp,
                onClick = onSignUp,
                enabled = !loading
            )

            Spacer(Modifier.height(12.dp))
        }
    }
}