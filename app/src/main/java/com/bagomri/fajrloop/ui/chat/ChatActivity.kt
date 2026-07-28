package com.bagomri.fajrloop.ui.chat

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import com.bagomri.fajrloop.alarm.AlarmPreferences
import com.bagomri.fajrloop.data.ChatMessage
import com.bagomri.fajrloop.ui.BaseActivity
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow

class ChatActivity : BaseActivity() {

    private lateinit var databaseRef: DatabaseReference
    private lateinit var messagesListener: ValueEventListener

    private val messagesStateFlow = MutableStateFlow<List<ChatMessage>>(emptyList())
    private val halqaNameStateFlow = MutableStateFlow("")

    private var halqaId: String? = null
    private var currentUid = ""
    private var currentDisplayName = "عضو"
    private var currentPhotoUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        databaseRef = FirebaseDatabase.getInstance()
            .getReference("chatMessages")
            .child(halqaId!!)

        loadHalqaName()
        setupFirebaseListener()

        setContent {
            FajrLoopTheme {
                val messages = messagesStateFlow.value
                val halqaName = halqaNameStateFlow.value

                ChatScreen(
                    title = halqaName,
                    messages = messages,
                    currentUid = currentUid,
                    onSendMessage = { text, type -> sendMessage(text, type) },
                    onBackClick = { finish() }
                )
            }
        }
    }

    private fun loadHalqaName() {
        FirebaseDatabase.getInstance().getReference("halqas").child(halqaId!!)
            .child("name")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.value as? String
                    if (!name.isNullOrEmpty()) {
                        halqaNameStateFlow.value = name
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun setupFirebaseListener() {
        messagesListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newList = mutableListOf<ChatMessage>()
                for (msgSnap in snapshot.children) {
                    val msg = msgSnap.getValue(ChatMessage::class.java)
                    if (msg != null) {
                        newList.add(msg)
                    }
                }
                newList.sortBy { it.timestamp }
                messagesStateFlow.value = newList
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ChatActivity, "فشل تحميل المحادثة: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        databaseRef.addValueEventListener(messagesListener)
    }

    private fun sendMessage(text: String, type: String) {
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
            .addOnFailureListener { e ->
                Toast.makeText(this, "فشل الإرسال: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::messagesListener.isInitialized) {
            databaseRef.removeEventListener(messagesListener)
        }
    }
}
