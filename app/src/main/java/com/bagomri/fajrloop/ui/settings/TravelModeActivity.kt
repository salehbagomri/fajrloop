package com.bagomri.fajrloop.ui.settings

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import com.bagomri.fajrloop.alarm.AlarmPreferences
import com.bagomri.fajrloop.ui.BaseActivity
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class TravelModeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        val initialEnabled = prefs.getBoolean("travel_mode_enabled", false)
        val initialType = prefs.getString("travel_mode_type", "indefinite") ?: "indefinite"
        val initialUntil = prefs.getString("travel_mode_until", "حتى الإلغاء اليدوي") ?: "حتى الإلغاء اليدوي"

        setContent {
            FajrLoopTheme {
                TravelModeScreen(
                    initialEnabled = initialEnabled,
                    initialType = initialType,
                    initialUntil = initialUntil,
                    onSaveTravelMode = { enabled, type, untilText ->
                        val editor = prefs.edit()
                        val halqaId = prefs.getString("current_halqa_id", null)
                        val uid = FirebaseAuth.getInstance().currentUser?.uid

                        if (!enabled) {
                            editor.putBoolean("travel_mode_enabled", false)
                                .remove("travel_mode_type")
                                .remove("travel_mode_until")
                                .apply()

                            if (!halqaId.isNullOrEmpty() && !uid.isNullOrEmpty()) {
                                FirebaseDatabase.getInstance()
                                    .getReference("halqas")
                                    .child(halqaId)
                                    .child("members")
                                    .child(uid)
                                    .child("status")
                                    .setValue("active")
                            }

                            Toast.makeText(this, "تم إيقاف وضع السفر بنجاح ✅", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            editor.putBoolean("travel_mode_enabled", true)
                                .putString("travel_mode_type", type)
                                .putString("travel_mode_until", untilText)
                                .apply()

                            if (!halqaId.isNullOrEmpty() && !uid.isNullOrEmpty()) {
                                FirebaseDatabase.getInstance()
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
                    },
                    onBackClick = { finish() }
                )
            }
        }
    }
}
