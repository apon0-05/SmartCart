package com.example.smartcard

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartcard.localization.LocalAppStrings
import com.example.smartcard.utils.LanguageManager
import com.example.smartcard.ui.components.SmartCartGoogleAuthButton
import com.example.smartcard.ui.components.SmartCartHeaderShape
import com.example.smartcard.ui.components.SmartCartInputField
import com.example.smartcard.ui.components.SmartCartPasswordInputField
import com.example.smartcard.ui.components.SmartCartPrimaryActionButton
import com.example.smartcard.ui.components.SmartCartSecondaryOutlineButton
import com.example.smartcard.ui.components.SmartCartSegmentedLine
import com.example.smartcard.ui.components.SmartCartTermsCheckbox
import com.example.smartcard.viewmodel.AuthViewModel

private const val WEB_CLIENT_ID =
    "1090740511419-bo6team9gnufaaesjcieo15rciq4lbjk.apps.googleusercontent.com"

private enum class SignUpValidationError {
    NameRequired,
    InvalidEmail,
    PasswordTooShort,
    PasswordsDoNotMatch,
    TermsNotAccepted,
}

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onBackToLogin: () -> Unit,
) {
    val vm: AuthViewModel = viewModel()
    val texts = LocalAppStrings.current
    val context = LocalContext.current
    val termsJoiner = when (LanguageManager.getLanguage()) {
        "ru" -> " и "
        "kk" -> " және "
        else -> " and "
    }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var agreed by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<SignUpValidationError?>(null) }

    val loading by vm.loading.collectAsState()
    val msg by vm.message.collectAsState()

    val emailValid = email.contains("@") && email.contains(".")
    val passwordValid = password.length >= 8
    val confirmValid = confirmPassword == password
    val formValid = name.isNotBlank() && emailValid && passwordValid && confirmValid && agreed

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        vm.handleGoogleResult(
            data = result.data,
            resultCode = result.resultCode,
            webClientId = WEB_CLIENT_ID,
            packageName = context.packageName,
            onSuccess = { onSignUpSuccess() }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 12.dp)
    ) {
        SmartCartHeaderShape(
            title = texts.signUp,
            subtitle = texts.signUpSubtitleEnterDetails,
            badge = {
                Image(
                    painter = painterResource(id = R.drawable.ic_cart_phone),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    contentScale = ContentScale.Fit
                )
            }
        )

        Spacer(Modifier.height(12.dp))

        SmartCartGoogleAuthButton(
            text = texts.signUpWithGoogle,
            onClick = {
                vm.clearMessage()
                val intent = vm.googleSignInIntent(context, WEB_CLIENT_ID)
                googleLauncher.launch(intent)
            },
            enabled = !loading
        )

        Spacer(Modifier.height(12.dp))

        SmartCartInputField(
            label = texts.fullName,
            value = name,
            onValueChange = { name = it },
            placeholder = texts.fullNamePlaceholder,
            isError = localError == SignUpValidationError.NameRequired
        )
        Spacer(Modifier.height(8.dp))

        SmartCartInputField(
            label = texts.email,
            value = email,
            onValueChange = { email = it },
            placeholder = texts.emailPlaceholder,
            isError = localError == SignUpValidationError.InvalidEmail
        )
        Spacer(Modifier.height(8.dp))

        SmartCartPasswordInputField(
            label = texts.password,
            value = password,
            onValueChange = { password = it },
            placeholder = texts.passwordHint,
            isError = localError == SignUpValidationError.PasswordTooShort
        )
        Spacer(Modifier.height(8.dp))

        SmartCartPasswordInputField(
            label = texts.confirmPassword,
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            placeholder = texts.passwordHint,
            isError = localError == SignUpValidationError.PasswordsDoNotMatch
        )

        Spacer(Modifier.height(12.dp))
        SmartCartSegmentedLine()
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { agreed = !agreed },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            SmartCartTermsCheckbox(
                checked = agreed,
                onToggle = { agreed = !agreed }
            )
            Spacer(Modifier.size(11.dp))
            Text(
                text = buildAnnotatedString {
                    append("${texts.agreeWith} ")
                    pushStyle(SpanStyle(color = AppColors.LinkBlue, fontWeight = FontWeight.Bold))
                    append(texts.terms)
                    pop()
                    append(termsJoiner)
                    pushStyle(SpanStyle(color = AppColors.LinkBlue, fontWeight = FontWeight.Bold))
                    append(texts.privacy)
                    pop()
                },
                color = AppColors.TextHint,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                softWrap = true
            )
        }

        val errorMessage = when (localError) {
            SignUpValidationError.NameRequired -> texts.validationNameRequired
            SignUpValidationError.InvalidEmail -> texts.validationEnterValidEmail
            SignUpValidationError.PasswordTooShort -> texts.validationPasswordTooShort
            SignUpValidationError.PasswordsDoNotMatch -> texts.validationPasswordsDoNotMatch
            SignUpValidationError.TermsNotAccepted -> texts.validationAcceptTerms
            null -> msg
        }
        if (!errorMessage.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = errorMessage,
                color = AppColors.Error,
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppColors.ErrorBg)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        SmartCartPrimaryActionButton(
            text = texts.signUp,
            onClick = {
                localError = when {
                    name.isBlank() -> SignUpValidationError.NameRequired
                    !emailValid -> SignUpValidationError.InvalidEmail
                    !passwordValid -> SignUpValidationError.PasswordTooShort
                    !confirmValid -> SignUpValidationError.PasswordsDoNotMatch
                    !agreed -> SignUpValidationError.TermsNotAccepted
                    else -> null
                }
                if (localError == null) {
                    vm.signUp(email, password) { onSignUpSuccess() }
                }
            },
            enabled = formValid && !loading,
            loading = loading
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = texts.alreadyHaveAccount,
            color = AppColors.TextHint,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            softWrap = true
        )
        Spacer(Modifier.height(8.dp))

        SmartCartSecondaryOutlineButton(
            text = texts.login,
            onClick = onBackToLogin,
            enabled = !loading
        )

        Spacer(Modifier.height(12.dp))
    }
}