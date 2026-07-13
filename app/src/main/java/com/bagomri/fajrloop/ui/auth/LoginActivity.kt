package com.bagomri.fajrloop.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.lifecycleScope
import com.bagomri.fajrloop.auth.AuthManager
import com.bagomri.fajrloop.databinding.ActivityLoginBinding
import com.bagomri.fajrloop.ui.main.MainActivity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

/**
 * LoginActivity — شاشة تسجيل الدخول الموحدة باستخدام Credential Manager
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    // معرّف الويب العميل الافتراضي لـ Firebase المأخوذ من google-services.json
    private val WEB_CLIENT_ID = "866668685561-iaftbovc44m135k8pg14o40p3jvirekv.apps.googleusercontent.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // التحقق من حالة تسجيل الدخول مسبقاً للتوجيه التلقائي
        if (AuthManager.isUserSignedIn()) {
            navigateToMain()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnGoogleSignin.setOnClickListener {
            startGoogleSignInFlow()
        }
    }

    private fun startGoogleSignInFlow() {
        setLoading(true)
        val credentialManager = CredentialManager.create(this)

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
                val result = credentialManager.getCredential(this@LoginActivity, request)
                val credential = result.credential

                if (credential is GoogleIdTokenCredential) {
                    val idToken = credential.idToken
                    firebaseAuthWithGoogle(idToken)
                } else {
                    setLoading(false)
                    showToast("نوع تفويض غير مدعوم")
                }
            } catch (e: Exception) {
                setLoading(false)
                showToast("فشل تسجيل الدخول: ${e.localizedMessage}")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user != null) {
                    // المرحلة 2 & 3: إنشاء/تحديث ملف تعريف المستخدم في RTDB
                    AuthManager.checkOrCreateUserProfile(user) { success ->
                        setLoading(false)
                        if (success) {
                            showToast("تم تسجيل الدخول بنجاح")
                            navigateToMain()
                        } else {
                            showToast("فشل في إنشاء ملف المستخدم السحابي")
                            AuthManager.signOut()
                        }
                    }
                } else {
                    setLoading(false)
                    showToast("فشل في العثور على بيانات المستخدم في Firebase")
                }
            }
            .addOnFailureListener { e ->
                setLoading(false)
                showToast("فشل الاتصال بـ Firebase Auth: ${e.localizedMessage}")
            }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.apply {
            if (isLoading) {
                progressLoading.visibility = View.VISIBLE
                btnGoogleSignin.isEnabled = false
                layoutLogo.alpha = 0.5f
            } else {
                progressLoading.visibility = View.GONE
                btnGoogleSignin.isEnabled = true
                layoutLogo.alpha = 1.0f
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
