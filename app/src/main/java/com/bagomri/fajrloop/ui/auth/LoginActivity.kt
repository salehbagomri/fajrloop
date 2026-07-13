package com.bagomri.fajrloop.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bagomri.fajrloop.auth.AuthManager
import com.bagomri.fajrloop.databinding.ActivityLoginBinding
import com.bagomri.fajrloop.ui.main.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

/**
 * LoginActivity — شاشة تسجيل الدخول الموحدة
 *
 * يعتمد بالكامل على Google Sign-In فقط.
 * عند نجاح تسجيل الدخول، يتم إنشاء ملف للمستخدم في قاعدة البيانات السحابية (Realtime Database).
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient

    // معرّف الويب العميل الافتراضي لـ Firebase المأخوذ من google-services.json
    private val WEB_CLIENT_ID = "866668685561-iaftbovc44m135k8pg14o40p3jvirekv.apps.googleusercontent.com"

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let { idToken ->
                    firebaseAuthWithGoogle(idToken)
                } ?: run {
                    setLoading(false)
                    showToast("فشل في الحصول على رمز Google ID Token")
                }
            } catch (e: ApiException) {
                setLoading(false)
                showToast("فشل تسجيل الدخول بواسطة جوجل: ${e.localizedMessage}")
            }
        } else {
            setLoading(false)
            showToast("تم إلغاء تسجيل الدخول")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // التحقق من حالة تسجيل الدخول مسبقاً للتوجيه التلقائي
        if (AuthManager.isUserSignedIn()) {
            navigateToMain()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupGoogleSignIn()

        binding.btnGoogleSignin.setOnClickListener {
            startGoogleSignInFlow()
        }
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .requestProfile()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun startGoogleSignInFlow() {
        setLoading(true)
        // تسجيل الخروج المسبق لتسجيل مستخدم جديد أو اختيار حساب
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
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
