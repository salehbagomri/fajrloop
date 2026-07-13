package com.bagomri.fajrloop.ui.settings

import android.app.DatePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bagomri.fajrloop.databinding.ActivityTravelModeBinding
import java.text.SimpleDateFormat
import java.util.*

class TravelModeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTravelModeBinding
    private lateinit var prefs: SharedPreferences
    private var customSelectedDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTravelModeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences(com.bagomri.fajrloop.alarm.AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)

        setupListeners()
        loadSavedState()
    }

    private fun loadSavedState() {
        val isEnabled = prefs.getBoolean("travel_mode_enabled", false)
        binding.switchTravelMode.isChecked = isEnabled
        binding.cardDurationSection.visibility = if (isEnabled) View.VISIBLE else View.GONE
        updateStatusText(isEnabled)

        val type = prefs.getString("travel_mode_type", "indefinite")
        when (type) {
            "indefinite" -> binding.radioIndefinite.isChecked = true
            "1_day" -> binding.radio1Day.isChecked = true
            "3_days" -> binding.radio3Days.isChecked = true
            "7_days" -> binding.radio7Days.isChecked = true
            "custom" -> {
                binding.radioCustom.isChecked = true
                val date = prefs.getString("travel_mode_until", "")
                if (date!!.isNotEmpty() && date != "حتى الإلغاء اليدوي") {
                    customSelectedDate = date
                    binding.textCustomDateDesc.text = "حتى: $date"
                    binding.textCustomDateDesc.setTextColor(Color.parseColor("#FFD700"))
                }
            }
        }
    }

    private fun updateStatusText(isEnabled: Boolean) {
        if (isEnabled) {
            binding.textTravelSwitchDesc.text = "الوضع نشط حالياً"
            binding.textTravelSwitchDesc.setTextColor(Color.parseColor("#3498DB"))
        } else {
            binding.textTravelSwitchDesc.text = "الوضع غير نشط حالياً"
            binding.textTravelSwitchDesc.setTextColor(Color.parseColor("#B0B0C5"))
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.switchTravelMode.setOnCheckedChangeListener { _, isChecked ->
            binding.cardDurationSection.visibility = if (isChecked) View.VISIBLE else View.GONE
            updateStatusText(isChecked)
        }

        binding.radioCustom.setOnClickListener {
            showDatePicker()
        }

        binding.btnSave.setOnClickListener {
            saveSettings()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDate = String.format("%04d/%02d/%02d", selectedYear, selectedMonth + 1, selectedDay)
            customSelectedDate = formattedDate
            binding.textCustomDateDesc.text = "حتى: $formattedDate"
            binding.textCustomDateDesc.setTextColor(Color.parseColor("#FFD700"))
        }, year, month, day)

        // تعيين الحد الأدنى للغد
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        datePicker.datePicker.minDate = calendar.timeInMillis
        datePicker.show()
    }

    private fun saveSettings() {
        val isEnabled = binding.switchTravelMode.isChecked
        val editor = prefs.edit()

        val halqaId = prefs.getString("current_halqa_id", null)
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid

        if (!isEnabled) {
            editor.putBoolean("travel_mode_enabled", false)
            editor.putString("travel_mode_type", null)
            editor.putString("travel_mode_until", null)
            editor.apply()

            // تحديث سحابي
            if (!halqaId.isNullOrEmpty() && !uid.isNullOrEmpty()) {
                com.google.firebase.database.FirebaseDatabase.getInstance()
                    .getReference("halqas")
                    .child(halqaId)
                    .child("members")
                    .child(uid)
                    .child("status")
                    .setValue("active")
            }

            Toast.makeText(this, "تم إيقاف وضع السفر بنجاح ✅", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val type = when {
            binding.radioIndefinite.isChecked -> "indefinite"
            binding.radio1Day.isChecked -> "1_day"
            binding.radio3Days.isChecked -> "3_days"
            binding.radio7Days.isChecked -> "7_days"
            binding.radioCustom.isChecked -> "custom"
            else -> "indefinite"
        }

        var untilText = "حتى الإلغاء اليدوي"
        val calendar = Calendar.getInstance()

        when (type) {
            "1_day" -> {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                untilText = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(calendar.time)
            }
            "3_days" -> {
                calendar.add(Calendar.DAY_OF_MONTH, 3)
                untilText = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(calendar.time)
            }
            "7_days" -> {
                calendar.add(Calendar.DAY_OF_MONTH, 7)
                untilText = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(calendar.time)
            }
            "custom" -> {
                if (customSelectedDate.isNullOrEmpty()) {
                    Toast.makeText(this, "الرجاء اختيار تاريخ مخصص أولاً", Toast.LENGTH_SHORT).show()
                    return
                }
                untilText = customSelectedDate!!
            }
        }

        editor.putBoolean("travel_mode_enabled", true)
        editor.putString("travel_mode_type", type)
        editor.putString("travel_mode_until", untilText)
        editor.apply()

        // تحديث سحابي
        if (!halqaId.isNullOrEmpty() && !uid.isNullOrEmpty()) {
            com.google.firebase.database.FirebaseDatabase.getInstance()
                .getReference("halqas")
                .child(halqaId)
                .child("members")
                .child(uid)
                .child("status")
                .setValue("travel")
        }

        Toast.makeText(this, "تم تحديث وضع السفر بنجاح ✅", Toast.LENGTH_SHORT).show()
        finish()
    }
}
