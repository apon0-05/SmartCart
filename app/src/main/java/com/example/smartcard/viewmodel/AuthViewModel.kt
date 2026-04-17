package com.example.smartcard.viewmodel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.smartcard.SmartCartLogTags
import com.example.smartcard.SmartCartUiCache
import androidx.lifecycle.ViewModel
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.google.firebase.auth.UserProfileChangeRequest
import com.example.smartcard.QrFlowPhoneLog

class AuthViewModel : ViewModel() {

    private companion object {
        const val GOOGLE_AUTH_TAG = "GOOGLE_AUTH"

        private const val AUTH_FAILURE_CANCELLED = "cancelled_by_user"
        private const val AUTH_FAILURE_CONFIG = "developer_config_error"
        private const val AUTH_FAILURE_SIGN_IN = "sign_in_failed"
    }

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    fun clearMessage() {
        _message.value = null
    }

    fun isLoggedIn(): Boolean = auth.currentUser != null

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        _loading.value = true
        Log.d(SmartCartLogTags.AUTH, "login_start email=${email.trim()}")
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener {
                _loading.value = false
                Log.d(SmartCartLogTags.AUTH, "login_success uid=${auth.currentUser?.uid ?: "unknown"}")
                onSuccess()
            }
            .addOnFailureListener { e ->
                _loading.value = false
                Log.e(SmartCartLogTags.AUTH, "login_failed email=${email.trim()}", e)
                _message.value = e.message ?: "Login failed"
            }
    }

    fun signUp(email: String, password: String, onSuccess: () -> Unit) {
        Log.d(SmartCartLogTags.AUTH, "signup_start email=${email.trim()}")
        auth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { result ->
                val user = result.user
                val nameFromEmail = email.substringBefore("@")

                val req = UserProfileChangeRequest.Builder()
                    .setDisplayName(nameFromEmail)
                    .build()

                user?.updateProfile(req)
                    ?.addOnCompleteListener {
                        upsertUserDoc(user?.uid, user?.email, nameFromEmail)
                        Log.d(SmartCartLogTags.AUTH, "signup_success uid=${user?.uid ?: "unknown"}")
                        onSuccess()
                    }
                    ?: run {
                        upsertUserDoc(user?.uid, user?.email, nameFromEmail)
                        Log.d(SmartCartLogTags.AUTH, "signup_success uid=${user?.uid ?: "unknown"}")
                        onSuccess()
                    }
            }
            .addOnFailureListener { e ->
                Log.e(SmartCartLogTags.AUTH, "signup_failed email=${email.trim()}", e)
                _message.value = e.message ?: "Sign up failed"
            }
    }

    private fun upsertUserDoc(uid: String?, email: String?, name: String?) {
        if (uid.isNullOrBlank()) return

        val data = hashMapOf(
            "uid" to uid,
            "email" to (email ?: ""),
            "name" to (name ?: ""),
            "createdAt" to System.currentTimeMillis()
        )

        QrFlowPhoneLog.d(
            event = "user_firestore_upsert_start",
            "collection" to "users",
            "docId" to uid
        )

        db.collection("users")
            .document(uid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                ensureAvatarGenderDefault(uid)
                QrFlowPhoneLog.d(
                    event = "user_firestore_upsert_success",
                    "collection" to "users",
                    "docId" to uid
                )
            }
            .addOnFailureListener { e ->
                QrFlowPhoneLog.e(
                    event = "exception",
                    throwable = e,
                    "where" to "user_firestore_upsert",
                    "collection" to "users",
                    "docId" to uid
                )
            }
    }

    private fun ensureAvatarGenderDefault(uid: String) {
        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val current = doc.getString("avatarGender")?.trim()?.lowercase()
                if (current != "male" && current != "female") {
                    db.collection("users")
                        .document(uid)
                        .set(mapOf("avatarGender" to "female"), SetOptions.merge())
                }
            }
    }

    // ---------- GOOGLE SIGN-IN ----------

    fun googleSignInIntent(context: Context, webClientId: String): Intent {
        Log.d(GOOGLE_AUTH_TAG, "webClientId=$webClientId")
        Log.d(GOOGLE_AUTH_TAG, "package=${context.packageName}")

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()

        val client = GoogleSignIn.getClient(context, gso)
        return client.signInIntent
    }

    fun handleGoogleResult(
        data: Intent?,
        resultCode: Int,
        webClientId: String,
        packageName: String,
        onSuccess: () -> Unit
    ) {
        Log.d(GOOGLE_AUTH_TAG, "webClientId=$webClientId")
        Log.d(GOOGLE_AUTH_TAG, "package=$packageName")

        Log.d(
            SmartCartLogTags.AUTH,
            "google_login_result_received resultCode=$resultCode package=$packageName webClientId=$webClientId"
        )

        if (resultCode != Activity.RESULT_OK) {
            val failureCategory = if (resultCode == Activity.RESULT_CANCELED) {
                AUTH_FAILURE_CANCELLED
            } else {
                AUTH_FAILURE_SIGN_IN
            }
            Log.w(
                SmartCartLogTags.AUTH,
                "google_login_result_not_ok resultCode=$resultCode isCancelled=${resultCode == Activity.RESULT_CANCELED} category=$failureCategory"
            )
            _message.value = if (resultCode == Activity.RESULT_CANCELED) {
                "Google sign-in cancelled"
            } else {
                "Google sign-in failed (resultCode=$resultCode)"
            }
            return
        }

        if (data == null) {
            Log.w(
                SmartCartLogTags.AUTH,
                "google_login_failed_intent_data_null category=$AUTH_FAILURE_SIGN_IN"
            )
            _message.value = "Google sign-in failed: empty sign-in result"
            return
        }

        _loading.value = true
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)

        try {
            val account = task.getResult(Exception::class.java)
            val idToken = account.idToken
            Log.d(
                SmartCartLogTags.AUTH,
                "google_login_account_received idTokenNull=${idToken == null} idTokenBlank=${idToken?.isBlank() == true}"
            )
            if (idToken.isNullOrBlank()) {
                _loading.value = false
                Log.e(
                    SmartCartLogTags.AUTH,
                    "google_login_failed_missing_token category=$AUTH_FAILURE_CONFIG"
                )
                _message.value = "Google sign-in failed: missing ID token"
                return
            }

            val credential = GoogleAuthProvider.getCredential(idToken, null)

            auth.signInWithCredential(credential)
                .addOnSuccessListener { result ->
                    val user = result.user
                    val displayName =
                        user?.displayName?.takeIf { it.isNotBlank() }
                            ?: account.displayName?.takeIf { it.isNotBlank() }
                            ?: user?.email?.substringBefore("@")
                            ?: "User"

                    upsertUserDoc(user?.uid, user?.email, displayName)
                    _loading.value = false
                    Log.d(SmartCartLogTags.AUTH, "google_login_success uid=${user?.uid ?: "unknown"}")
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    _loading.value = false
                    Log.e(
                        SmartCartLogTags.AUTH,
                        "google_auth_with_firebase_failed category=$AUTH_FAILURE_SIGN_IN",
                        e
                    )
                    _message.value = e.message ?: "Google auth failed"
                }
        } catch (e: ApiException) {
            _loading.value = false
            val statusCode = e.statusCode
            val statusText = CommonStatusCodes.getStatusCodeString(statusCode)
            val failureCategory = when (statusCode) {
                CommonStatusCodes.CANCELED -> AUTH_FAILURE_CANCELLED
                10,
                CommonStatusCodes.DEVELOPER_ERROR,
                CommonStatusCodes.INVALID_ACCOUNT,
                CommonStatusCodes.SIGN_IN_REQUIRED -> AUTH_FAILURE_CONFIG
                else -> AUTH_FAILURE_SIGN_IN
            }
            Log.e(
                SmartCartLogTags.AUTH,
                "google_login_api_exception statusCode=$statusCode statusText=$statusText package=$packageName webClientId=$webClientId category=$failureCategory",
                e
            )

            _message.value = when (failureCategory) {
                AUTH_FAILURE_CANCELLED -> "Google sign-in cancelled"
                AUTH_FAILURE_CONFIG -> "Google Sign-In configuration error ($statusCode: $statusText). Check Firebase SHA-1/SHA-256 and google-services.json."
                else -> "Google sign-in failed ($statusCode: $statusText)"
            }
        } catch (e: Exception) {
            _loading.value = false
            Log.e(
                SmartCartLogTags.AUTH,
                "google_login_exception category=$AUTH_FAILURE_SIGN_IN",
                e
            )
            _message.value = e.localizedMessage ?: "Google sign-in failed"
        }
    }

    fun logout() {
        val userId = auth.currentUser?.uid
        if (!userId.isNullOrBlank()) {
            SmartCartUiCache.clearUser(userId)
        }
        Log.d(SmartCartLogTags.AUTH, "logout uid=${userId ?: "unknown"}")
        auth.signOut()
    }
}