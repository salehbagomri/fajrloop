package com.bagomri.fajrloop.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast

/**
 * AppInfoUtils — فئة مساعدة لجلب معلومات الإصدار والمشاركة والدعم الفني تلقائياً
 */
object AppInfoUtils {

    /**
     * جلب اسم الإصدار تلقائياً من حزمة النظام (مثلاً: 1.0.0)
     */
    fun getAppVersionName(context: Context): String {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    /**
     * جلب رمز البناء تلقائياً من نظام الأندرويد (مثلاً: 1)
     */
    fun getAppVersionCode(context: Context): Long {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
        } catch (e: Exception) {
            1L
        }
    }

    /**
     * مشاركة رابط التطبيق مع الأصدقاء
     */
    fun shareApp(context: Context) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "تطبيق حلقة الفجر — FajrLoop 🕌")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "انضم إلينا في تطبيق حلقة الفجر لتستيقظ لصلاة الفجر في وقتها مع أصدقائك بأسلوب إيماني متكامل 🌅\n\nحمل التطبيق الآن: https://play.google.com/store/apps/details?id=${context.packageName}"
                )
            }
            context.startActivity(Intent.createChooser(shareIntent, "مشاركة تطبيق حلقة الفجر"))
        } catch (e: Exception) {
            Toast.makeText(context, "تعذر فتح نافذة المشاركة", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * فتح صفحة التطبيق في متجر جوجل بلاي للتقييم
     */
    fun openPlayStore(context: Context) {
        try {
            val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}"))
            marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(marketIntent)
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}"))
            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(webIntent)
        }
    }

    /**
     * إرسال بريد إلكتروني للدعم الفني
     */
    fun sendSupportEmail(context: Context, supportEmail: String = "support@fajrloop.com") {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$supportEmail")
                putExtra(Intent.EXTRA_SUBJECT, "استفسار / دعم تطبيق حلقة الفجر - الإصدار ${getAppVersionName(context)}")
            }
            context.startActivity(Intent.createChooser(intent, "إرسال بريد إلكتروني للدعم الفني"))
        } catch (e: Exception) {
            Toast.makeText(context, "تواصل معنا عبر البريد: $supportEmail", Toast.LENGTH_LONG).show()
        }
    }
}
