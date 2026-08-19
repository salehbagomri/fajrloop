package com.bagomri.fajrloop.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

/**
 * مساعد للتعامل مع إعدادات البطارية الخاصة بكل ماركة هاتف.
 * بعض الماركات (Honor/Huawei/Samsung) تضيف قيوداً إضافية فوق Android القياسي
 * تمنع المنبه من الرنين إذا لم يتم إعدادها يدوياً.
 */
object OemBatteryHelper {

    val isHuaweiOrHonor: Boolean
        get() = Build.MANUFACTURER.lowercase().let {
            it.contains("huawei") || it.contains("honor")
        }

    val isSamsung: Boolean
        get() = Build.MANUFACTURER.lowercase().contains("samsung")

    /** هل يحتاج هذا الجهاز إعداداً خاصاً بالماركة؟ */
    fun requiresOemSettings(): Boolean = isHuaweiOrHonor || isSamsung

    /** عنوان عنصر الصلاحية المناسب للماركة */
    fun getOemLabel(): String? = when {
        isHuaweiOrHonor -> "إعدادات التشغيل (Honor)"
        isSamsung        -> "نشاط التطبيق الخلفي (Samsung)"
        else             -> null
    }

    /** وصف تفصيلي لما يجب على المستخدم فعله */
    fun getOemDescription(): String? = when {
        isHuaweiOrHonor ->
            "افتح الإعدادات وفعّل: التشغيل التلقائي، الثانوي، والخلفي. لا تختر \"إدارة تلقائية\""
        isSamsung ->
            "تأكد من أن التطبيق غير مدرج في التطبيقات النائمة وفعّل النشاط الخلفي"
        else -> null
    }

    /**
     * يُعيد Intent يفتح صفحة إعدادات الماركة المباشرة.
     * إذا فشل Intent الخاص بالماركة يرجع Intent عام لتفاصيل التطبيق.
     */
    fun getOemIntent(context: Context): Intent {
        val fallback = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${context.packageName}")
        )

        return when {
            isHuaweiOrHonor -> runCatching {
                Intent().apply {
                    component = ComponentName(
                        "com.huawei.systemmanager",
                        "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
                    )
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }.getOrDefault(fallback)

            isSamsung -> runCatching {
                Intent().apply {
                    component = ComponentName(
                        "com.samsung.android.lool",
                        "com.samsung.android.sm.battery.ui.BatteryActivity"
                    )
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }.getOrDefault(fallback)

            else -> fallback
        }
    }
}
