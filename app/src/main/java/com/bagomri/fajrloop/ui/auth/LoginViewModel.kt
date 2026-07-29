package com.bagomri.fajrloop.ui.auth

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bagomri.fajrloop.auth.AuthManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val WEB_CLIENT_ID = "866668685561-iaftbovc44m135k8pg14o40p3jvirekv.apps.googleusercontent.com"
        private const val TAG = "LoginViewModel"
    }

    private val _isLoadingFlow = MutableStateFlow(false)
    val isLoadingFlow: StateFlow<Boolean> = _isLoadingFlow.asStateFlow()

    private val _loginSuccessFlow = MutableStateFlow(false)
    val loginSuccessFlow: StateFlow<Boolean> = _loginSuccessFlow.asStateFlow()

    private val _errorMessageFlow = MutableStateFlow<String?>(null)
    val errorMessageFlow: StateFlow<String?> = _errorMessageFlow.asStateFlow()

    fun resetLoginState() {
        _loginSuccessFlow.value = false
        _errorMessageFlow.value = null
        _isLoadingFlow.value = false
    }

    fun startGoogleSignInFlow(context: Context, onFallbackLegacy: (Intent) -> Unit) {
        _isLoadingFlow.value = true
        _errorMessageFlow.value = null

        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        viewModelScope.launch {
            try {
                val result = withTimeout(3_000L) {
                    credentialManager.getCredential(context, request)
                }

                val credential = result.credential
                when {
                    credential is GoogleIdTokenCredential -> {
                        firebaseAuthWithGoogle(credential.idToken)
                    }
                    credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                        val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        firebaseAuthWithGoogle(googleCredential.idToken)
                    }
                    else -> {
                        Log.w(TAG, "Unexpected credential type, falling back to legacy sign-in")
                        triggerLegacySignIn(context, onFallbackLegacy)
                    }
                }
            } catch (e: GetCredentialCancellationException) {
                _isLoadingFlow.value = false
                Log.d(TAG, "Sign-In cancelled by user")
            } catch (e: TimeoutCancellationException) {
                Log.w(TAG, "CredentialManager timed out, launching legacy GoogleSignInClient fallback")
                triggerLegacySignIn(context, onFallbackLegacy)
            } catch (e: Exception) {
                Log.w(TAG, "CredentialManager failed (${e::class.simpleName}), launching legacy GoogleSignInClient fallback", e)
                triggerLegacySignIn(context, onFallbackLegacy)
            }
        }
    }

    fun handleLegacySignInResult(idToken: String?, statusCode: Int?) {
        if (!idToken.isNullOrEmpty()) {
            firebaseAuthWithGoogle(idToken)
        } else {
            _isLoadingFlow.value = false
            if (statusCode != null) {
                _errorMessageFlow.value = "فشل تسجيل الدخول عبر قوقل (رمز $statusCode)"
            } else {
                _errorMessageFlow.value = "فشل الحصول على رمز التفويض من قوقل"
            }
        }
    }

    fun onLegacySignInCancelled() {
        _isLoadingFlow.value = false
    }

    private fun triggerLegacySignIn(context: Context, onFallbackLegacy: (Intent) -> Unit) {
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(WEB_CLIENT_ID)
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(context, gso)
            googleSignInClient.signOut().addOnCompleteListener {
                onFallbackLegacy(googleSignInClient.signInIntent)
            }
        } catch (e: Exception) {
            _isLoadingFlow.value = false
            Log.e(TAG, "Failed to start legacy Google Sign-In", e)
            _errorMessageFlow.value = "خطأ أثناء فتح حسابات قوقل: ${e.localizedMessage}"
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user != null) {
                    AuthManager.checkOrCreateUserProfile(user) { success ->
                        _isLoadingFlow.value = false
                        if (success) {
                            _loginSuccessFlow.value = true
                        } else {
                            _errorMessageFlow.value = "فشل في إنشاء ملف المستخدم السحابي"
                            AuthManager.signOut()
                        }
                    }
                } else {
                    _isLoadingFlow.value = false
                    _errorMessageFlow.value = "فشل في العثور على بيانات المستخدم"
                }
            }
            .addOnFailureListener { e ->
                _isLoadingFlow.value = false
                Log.e(TAG, "Firebase auth failed", e)
                _errorMessageFlow.value = "فشل Firebase Auth: ${e.localizedMessage}"
            }
    }
}
