package com.bagomri.fajrloop.ui.auth

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.bagomri.fajrloop.auth.AuthManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

    private val _loadingMessageFlow = MutableStateFlow<String?>(null)
    val loadingMessageFlow: StateFlow<String?> = _loadingMessageFlow.asStateFlow()

    fun resetLoginState() {
        _loginSuccessFlow.value = false
        _errorMessageFlow.value = null
        _isLoadingFlow.value = false
        _loadingMessageFlow.value = null
    }

    fun startGoogleSignInFlow(context: Context, onSignInIntent: (Intent) -> Unit) {
        _isLoadingFlow.value = true
        _errorMessageFlow.value = null
        _loadingMessageFlow.value = "جاري فتح اختيار الحساب..."

        try {
            val webClientId = getWebClientId(context)
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(context, gso)
            // نفرغ الجلسة أولاً لإظهار قائمة الحسابات دائماً
            googleSignInClient.signOut().addOnCompleteListener {
                _isLoadingFlow.value = false
                _loadingMessageFlow.value = null
                onSignInIntent(googleSignInClient.signInIntent)
            }
        } catch (e: Exception) {
            _isLoadingFlow.value = false
            _loadingMessageFlow.value = null
            Log.e(TAG, "Failed to start Google Sign-In", e)
            _errorMessageFlow.value = "خطأ أثناء فتح حسابات قوقل: ${e.localizedMessage}"
        }
    }

    fun handleSignInResult(idToken: String?, statusCode: Int?) {
        if (!idToken.isNullOrEmpty()) {
            firebaseAuthWithGoogle(idToken)
        } else {
            _isLoadingFlow.value = false
            _errorMessageFlow.value = if (statusCode != null)
                "فشل تسجيل الدخول عبر قوقل (رمز $statusCode)"
            else
                "فشل الحصول على رمز التفويض من قوقل"
        }
    }

    fun onSignInCancelled() {
        _isLoadingFlow.value = false
        _loadingMessageFlow.value = null
    }

    // دالة للتوافق مع الكود القديم في NavGraph
    fun handleLegacySignInResult(idToken: String?, statusCode: Int?) = handleSignInResult(idToken, statusCode)
    fun onLegacySignInCancelled() = onSignInCancelled()

    private fun firebaseAuthWithGoogle(idToken: String) {
        _isLoadingFlow.value = true
        _loadingMessageFlow.value = "جاري التحقق من هويتك..."
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user != null) {
                    _loadingMessageFlow.value = "جاري إعداد ملفك الشخصي..."
                    AuthManager.checkOrCreateUserProfile(user) { success ->
                        _isLoadingFlow.value = false
                        _loadingMessageFlow.value = null
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
                _loadingMessageFlow.value = null
                Log.e(TAG, "Firebase auth failed", e)
                _errorMessageFlow.value = "فشل تسجيل الدخول: ${e.localizedMessage}"
            }
    }

    private fun getWebClientId(context: Context): String {
        return try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) {
                val id = context.getString(resId)
                if (id.isNotEmpty()) id else WEB_CLIENT_ID
            } else {
                WEB_CLIENT_ID
            }
        } catch (e: Exception) {
            WEB_CLIENT_ID
        }
    }
}
