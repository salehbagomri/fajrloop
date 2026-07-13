package com.bagomri.fajrloop.ui.chat

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.bagomri.fajrloop.ui.BaseActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bagomri.fajrloop.R
import com.bagomri.fajrloop.alarm.AlarmPreferences
import com.bagomri.fajrloop.data.ChatMessage
import com.bagomri.fajrloop.databinding.ActivityChatBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class ChatActivity : BaseActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var databaseRef: DatabaseReference
    private lateinit var messagesListener: ValueEventListener

    private val messagesList = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter

    private var halqaId: String? = null
    private var currentUid = ""
    private var currentDisplayName = "عضو"
    private var currentPhotoUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. استرجاع بيانات الحلقة والمستخدم الحالي
        val prefs = getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        halqaId = prefs.getString("current_halqa_id", null)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null || halqaId.isNullOrEmpty()) {
            Toast.makeText(this, "⚠️ يجب الانضمام لحلقة أولاً للدردشة", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        currentUid = currentUser.uid
        currentDisplayName = currentUser.displayName ?: "عضو"
        currentPhotoUrl = currentUser.photoUrl?.toString() ?: ""

        // 2. إعداد شريط العنوان
        binding.btnBack.setOnClickListener { finish() }
        loadHalqaName()

        // 3. تهيئة القائمة والـ Adapter
        adapter = ChatAdapter(messagesList, currentUid)
        val layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        binding.recyclerMessages.layoutManager = layoutManager
        binding.recyclerMessages.adapter = adapter

        // 4. ربط قاعدة البيانات والمزامنة الفورية
        databaseRef = FirebaseDatabase.getInstance()
            .getReference("chatMessages")
            .child(halqaId!!)

        setupFirebaseListener()

        // 5. تهيئة أزرار الإدخال والإرسال السريع
        setupInputListeners()
    }

    private fun loadHalqaName() {
        FirebaseDatabase.getInstance().getReference("halqas").child(halqaId!!)
            .child("name")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.value as? String
                    if (!name.isNullOrEmpty()) {
                        binding.textChatTitle.text = name
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun setupFirebaseListener() {
        messagesListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messagesList.clear()
                for (msgSnap in snapshot.children) {
                    val msg = msgSnap.getValue(ChatMessage::class.java)
                    if (msg != null) {
                        messagesList.add(msg)
                    }
                }
                // ترتيب الرسائل زمنياً تصاعدياً
                messagesList.sortBy { it.timestamp }

                adapter.notifyDataSetChanged()

                // تمرير تلقائي لآخر رسالة
                if (messagesList.isNotEmpty()) {
                    binding.recyclerMessages.scrollToPosition(messagesList.size - 1)
                    binding.layoutEmptyChat.visibility = View.GONE
                } else {
                    binding.layoutEmptyChat.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ChatActivity, "فشل تحميل المحادثة: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        databaseRef.addValueEventListener(messagesListener)
    }

    private fun setupInputListeners() {
        // زر إرسال الرسالة العادية
        binding.btnSendMessage.setOnClickListener {
            val text = binding.editMessageInput.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text, "normal")
            }
        }

        // رقاقات الإرسال السريع
        binding.chipPrayerBetter.setOnClickListener { sendMessage(binding.chipPrayerBetter.text.toString(), "normal") }
        binding.chipHeroesStrength.setOnClickListener { sendMessage(binding.chipHeroesStrength.text.toString(), "normal") }
        binding.chipBlessedFajr.setOnClickListener { sendMessage(binding.chipBlessedFajr.text.toString(), "normal") }
        binding.chipMorningAdhkar.setOnClickListener { sendMessage(binding.chipMorningAdhkar.text.toString(), "normal") }

        // زر إرسال الرسائل التحفيزية السوبر (النجمة الذهبية)
        binding.btnSendMotivational.setOnClickListener {
            showMotivationalDialog()
        }
    }

    private fun sendMessage(text: String, type: String) {
        binding.btnSendMessage.visibility = View.INVISIBLE
        binding.progressSending.visibility = View.VISIBLE

        val msgId = databaseRef.push().key ?: return
        val chatMsg = ChatMessage(
            id = msgId,
            senderId = currentUid,
            senderName = currentDisplayName,
            senderPhotoUrl = currentPhotoUrl,
            message = text,
            type = type,
            timestamp = System.currentTimeMillis()
        )

        databaseRef.child(msgId).setValue(chatMsg)
            .addOnSuccessListener {
                binding.editMessageInput.text.clear()
                binding.btnSendMessage.visibility = View.VISIBLE
                binding.progressSending.visibility = View.GONE
                // إخفاء لوحة المفاتيح
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.editMessageInput.windowToken, 0)
            }
            .addOnFailureListener { e ->
                binding.btnSendMessage.visibility = View.VISIBLE
                binding.progressSending.visibility = View.GONE
                Toast.makeText(this, "فشل الإرسال: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showMotivationalDialog() {
        val presets = listOf(
            Triple("✨", "الصلاة خير من النوم ⏰", "#FFD700"),
            Triple("💪", "همّتكم يا أبطال الفجر!", "#FF8C00"),
            Triple("🌅", "فجر مبارك للجميع", "#2ECC71"),
            Triple("📖", "لا تنسوا أذكار الصباح", "#B57CFF"),
            Triple("🕋", "ألا إن سلعة الله غالية، ألا إن سلعة الله الجنة", "#FFD700"),
            Triple("🟢", "من صلى الفجر في جماعة فهو في ذمة الله", "#2ECC71")
        )

        // إنشاء Bottom Sheet Dialog مخصص بخلفية زجاجية داكنة
        val dialog = BottomSheetDialog(this, R.style.DarkBottomSheetTheme)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_motivational, null)
        dialog.setContentView(dialogView)

        // تلوين خلفية الـ BottomSheet نفسها
        dialog.window?.also { w ->
            w.setDimAmount(0.7f)
        }
        val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.setBackgroundColor(Color.TRANSPARENT)

        // بناء أزرار الرسائل التحفيزية ديناميكياً
        val container = dialogView.findViewById<LinearLayout>(R.id.container_presets)
        presets.forEach { (emoji, text, color) ->
            val item = LayoutInflater.from(this)
                .inflate(R.layout.item_motivational_preset, container, false)
            item.findViewById<TextView>(R.id.text_preset_emoji).text = emoji
            item.findViewById<TextView>(R.id.text_preset_message).text = text
            item.findViewById<TextView>(R.id.text_preset_message).setTextColor(Color.parseColor(color))
            item.setOnClickListener {
                sendMessage("$emoji $text", "motivational")
                dialog.dismiss()
            }
            container.addView(item)
        }

        dialogView.findViewById<TextView>(R.id.btn_dialog_cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::messagesListener.isInitialized) {
            databaseRef.removeEventListener(messagesListener)
        }
    }
}
