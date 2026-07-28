package com.bagomri.fajrloop.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.lifecycleScope
import com.bagomri.fajrloop.auth.AuthManager
import com.bagomri.fajrloop.ui.BaseActivity
import com.bagomri.fajrloop.ui.main.MainActivity
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

/**
 * LoginActivity — شاشة تسجيل الدخول باستخدام Credential Manager مع fallback لـ GoogleSignInClient (Jetpack Compose)
 */
class LoginActivity : BaseActivity() {

    private val WEB_CLIENT_ID = "866668685561-iaftbovc44m135k8pg14o40p3jvirekv.apps.googleusercontent.com"
    private val credentialManager by lazy { CredentialManager.create(this) }
    private val isLoadingFlow = MutableStateFlow(false)

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (!idToken.isNullOrEmpty()) {
                    firebaseAuthWithGoogle(idToken)
                } else {
                    setLoading(false)
                    showToast("فشل الحصول على رمز التفويض من قوقل")
                }
            } catch (e: ApiException) {
                setLoading(false)
                Log.e("LoginActivity", "Legacy Google Sign-In failed code: ${e.statusCode}", e)
                showToast("فشل تسجيل الدخول عبر قوقل (رمز ${e.statusCode})")
            }
        } else {
            setLoading(false)
            Log.d("LoginActivity", "Legacy Google Sign-In cancelled (code: ${result.resultCode})")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (AuthManager.isUserSignedIn()) {
            navigateToMain()
            return
        }

        setContent {
            FajrLoopTheme {
                val isLoading = isLoadingFlow.value
                LoginScreen(
                    onGoogleSignInClick = { startGoogleSignInFlow() },
                    isLoading = isLoading
                )
            }
        }
    }

    private fun startGoogleSignInFlow() {
        setLoading(true)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = withTimeout(3_000L) {
                    credentialManager.getCredential(this@LoginActivity, request)
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
                        Log.w("LoginActivity", "Unexpected credential type, falling back to legacy sign-in")
                        startLegacyGoogleSignIn()
                    }
                }

            } catch (e: GetCredentialCancellationException) {
                setLoading(false)
                Log.d("LoginActivity", "Sign-In cancelled by user")

            } catch (e: TimeoutCancellationException) {
                Log.w("LoginActivity", "CredentialManager timed out, launching legacy GoogleSignInClient fallback")
                startLegacyGoogleSignIn()

            } catch (e: Exception) {
                Log.w("LoginActivity", "CredentialManager failed (${e::class.simpleName}), launching legacy GoogleSignInClient fallback", e)
                startLegacyGoogleSignIn()
            }
        }
    }

    private fun startLegacyGoogleSignIn() {
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(WEB_CLIENT_ID)
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(this, gso)
            googleSignInClient.signOut().addOnCompleteListener {
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
            }
        } catch (e: Exception) {
            setLoading(false)
            Log.e("LoginActivity", "Failed to start legacy Google Sign-In", e)
            showToast("خطأ أثناء فتح حسابات قوقل: ${e.localizedMessage}")
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user != null) {
                    AuthManager.checkOrCreateUserProfile(user) { success ->
                        setLoading(false)
                        if (success) {
                            navigateToMain()
                        } else {
                            showToast("فشل في إنشاء ملف المستخدم السحابي")
                            AuthManager.signOut()
                        }
                    }
                } else {
                    setLoading(false)
                    showToast("فشل في العثور على بيانات المستخدم")
                }
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Log.e("LoginActivity", "Firebase auth failed", e)
                showToast("فشل Firebase Auth: ${e.localizedMessage}")
            }
    }

    private fun setLoading(isLoading: Boolean) {
        isLoadingFlow.value = isLoading
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
