package com.bagomri.fajrloop.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bagomri.fajrloop.ui.BaseActivity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.lifecycleScope
import com.bagomri.fajrloop.auth.AuthManager
import com.bagomri.fajrloop.databinding.ActivityLoginBinding
import com.bagomri.fajrloop.ui.main.MainActivity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

/**
 * LoginActivity — شاشة تسجيل الدخول باستخدام Credential Manager
 */
class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding

    // معرّف الويب العميل من google-services.json
    private val WEB_CLIENT_ID = "866668685561-iaftbovc44m135k8pg14o40p3jvirekv.apps.googleusercontent.com"

    // Credential Manager — يُنشأ مرة واحدة فقط
    private val credentialManager by lazy { CredentialManager.create(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // التوجيه التلقائي إذا كان المستخدم سجّل الدخول مسبقاً
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

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)   // عرض جميع الحسابات
            .setServerClientId(WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)             // لا اختيار تلقائي
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                // مهلة 20 ثانية لمنع التعليق اللانهائي
                val result = withTimeout(20_000L) {
                    credentialManager.getCredential(this@LoginActivity, request)
                }

                val credential = result.credential
                if (credential is GoogleIdTokenCredential) {
                    firebaseAuthWithGoogle(credential.idToken)
                } else {
                    setLoading(false)
                    showToast("نوع تفويض غير مدعوم")
                }

            } catch (e: TimeoutCancellationException) {
                // التعليق اللانهائي — غالباً مشكلة في Google Play Services أو الإنترنت
                setLoading(false)
                Log.e("LoginActivity", "Google Sign-In timed out", e)
                showToast("انتهت المهلة — تأكد من الإنترنت وحداثة خدمات Google Play")

            } catch (e: GetCredentialCancellationException) {
                // المستخدم ألغى عملية الاختيار
                setLoading(false)
                Log.d("LoginActivity", "Sign-In cancelled by user")

            } catch (e: NoCredentialException) {
                // لا توجد حسابات Google على الجهاز
                setLoading(false)
                Log.e("LoginActivity", "No credential available", e)
                showToast("لم يُعثر على حساب Google. أضف حساباً في إعدادات الجهاز")

            } catch (e: Exception) {
                setLoading(false)
                Log.e("LoginActivity", "Sign-In error: ${e::class.simpleName}: ${e.message}", e)
                showToast("خطأ: ${e.localizedMessage ?: e::class.simpleName}")
            }
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
        binding.apply {
            progressLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnGoogleSignin.isEnabled = !isLoading
            layoutLogo.alpha = if (isLoading) 0.5f else 1.0f
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

