package com.example.smartcard.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.google.firebase.auth.UserProfileChangeRequest
import com.example.smartcard.QrFlowPhoneLog

class AuthViewModel : ViewModel() {

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
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener {
                _loading.value = false
                onSuccess()
            }
            .addOnFailureListener { e ->
                _loading.value = false
                _message.value = e.message ?: "Login failed"
            }
    }

    fun signUp(email: String, password: String, onSuccess: () -> Unit) {
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
                        onSuccess()
                    }
                    ?: run {
                        upsertUserDoc(user?.uid, user?.email, nameFromEmail)
                        onSuccess()
                    }
            }
            .addOnFailureListener { e ->
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
            .set(data)
            .addOnSuccessListener {
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

    // ---------- GOOGLE SIGN-IN ----------

    fun googleSignInIntent(context: Context, webClientId: String): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)   // ВАЖНО: именно web client id
            .requestEmail()
            .build()

        val client = GoogleSignIn.getClient(context, gso)
        return client.signInIntent
    }

    fun handleGoogleResult(data: Intent?, onSuccess: () -> Unit) {
        _loading.value = true
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(Exception::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            auth.signInWithCredential(credential)
                .addOnSuccessListener {
                    _loading.value = false
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    _loading.value = false
                    _message.value = e.message ?: "Google auth failed"
                }
        } catch (e: Exception) {
            _loading.value = false
            _message.value = e.message ?: "Google sign-in cancelled"
        }
    }

    fun logout() {
        auth.signOut()
    }
}