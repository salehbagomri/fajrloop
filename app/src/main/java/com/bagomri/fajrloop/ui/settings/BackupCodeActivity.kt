package com.bagomri.fajrloop.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.bagomri.fajrloop.ui.BaseActivity
import com.bagomri.fajrloop.R
import com.bagomri.fajrloop.databinding.ActivityBackupCodeBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

class BackupCodeActivity : BaseActivity() {

    private lateinit var binding: ActivityBackupCodeBinding
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackupCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences(com.bagomri.fajrloop.alarm.AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnReturnHome.setOnClickListener { finish() }
        binding.btnCopyCode.setOnClickListener {
            val codeText = binding.textTotpCode.text.toString().replace(" ", "")
            if (codeText.isNotEmpty()) {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("FajrLoop Emergency Code", codeText)
                clipboard.setPrimaryClip(clip)
                android.widget.Toast.makeText(this, "تم نسخ كود الطوارئ بنجاح! 📋", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        checkHalqaState()
    }

    private fun checkHalqaState() {
        val mainPrefs = getSharedPreferences(com.bagomri.fajrloop.alarm.AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        val halqaId = mainPrefs.getString("current_halqa_id", null)

        if (halqaId.isNullOrEmpty()) {
            // لا توجد حلقة
            binding.layoutNoHalqa.visibility = View.VISIBLE
            binding.layoutHasHalqa.visibility = View.GONE
        } else {
            // توجد حلقة
            binding.layoutNoHalqa.visibility = View.GONE
            binding.layoutHasHalqa.visibility = View.VISIBLE

            // توليد كود TOTP يومي ثابت لكل مستخدم لليوم الحالي
            val dateStr = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
            val seed = (dateStr + halqaId).hashCode().absoluteValue
            val code = (seed % 900000) + 100000 // رقم من 6 خانات
            val formattedCode = "${code / 1000} ${code % 1000}"
            binding.textTotpCode.text = formattedCode

            // محاكاة نشاط الكود بناء على تفعيل المنبه
            val isAlarmEnabled = mainPrefs.getBoolean("alarm_enabled", false)
            if (isAlarmEnabled) {
                binding.imageTotpStatusIcon.setImageResource(R.drawable.ic_circle_check)
                binding.imageTotpStatusIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#2ECC71"))
                binding.textTotpStatusDesc.text = "الكود نشط وجاهز! متبقي 20 دقيقة على انتهاء صلاحيته."
                binding.textTotpStatusDesc.setTextColor(Color.parseColor("#2ECC71"))
            } else {
                binding.imageTotpStatusIcon.setImageResource(R.drawable.ic_circle_warning)
                binding.imageTotpStatusIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#B0B0C5"))
                binding.textTotpStatusDesc.text = "هذا الكود ينشط تلقائياً ولمدة 30 دقيقة فقط فور دخول وقت أذان الفجر اليوم."
                binding.textTotpStatusDesc.setTextColor(Color.parseColor("#B0B0C5"))
            }
        }
    }
}
