package com.bagomri.fajrloop.ui.chat

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.bagomri.fajrloop.alarm.AlarmPreferences
import com.bagomri.fajrloop.data.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val _messagesFlow = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messagesFlow: StateFlow<List<ChatMessage>> = _messagesFlow.asStateFlow()

    private val _halqaNameFlow = MutableStateFlow("")
    val halqaNameFlow: StateFlow<String> = _halqaNameFlow.asStateFlow()

    private val _errorFlow = MutableStateFlow<String?>(null)
    val errorFlow: StateFlow<String?> = _errorFlow.asStateFlow()

    var halqaId: String? = null
        private set
    var currentUid: String = ""
        private set
    var currentDisplayName: String = "عضو"
        private set
    var currentPhotoUrl: String = ""
        private set

    private var databaseRef: DatabaseReference? = null
    private var messagesListener: ValueEventListener? = null

    init {
        val prefs = application.getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        halqaId = prefs.getString("current_halqa_id", null)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null && !halqaId.isNullOrEmpty()) {
            currentUid = currentUser.uid
            currentDisplayName = currentUser.displayName ?: "عضو"
            currentPhotoUrl = currentUser.photoUrl?.toString() ?: ""

            databaseRef = FirebaseDatabase.getInstance()
                .getReference("chatMessages")
                .child(halqaId!!)

            loadHalqaName()
            setupFirebaseListener()
        }
    }

    private fun loadHalqaName() {
        val hId = halqaId ?: return
        FirebaseDatabase.getInstance().getReference("halqas").child(hId)
            .child("name")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.value as? String
                    if (!name.isNullOrEmpty()) {
                        _halqaNameFlow.value = name
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun setupFirebaseListener() {
        val ref = databaseRef ?: return
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
                _messagesFlow.value = newList
            }

            override fun onCancelled(error: DatabaseError) {
                _errorFlow.value = "فشل تحميل المحادثة: ${error.message}"
            }
        }
        ref.addValueEventListener(messagesListener!!)
    }

    fun sendMessage(text: String, type: String) {
        val ref = databaseRef ?: return
        val msgId = ref.push().key ?: return
        val chatMsg = ChatMessage(
            id = msgId,
            senderId = currentUid,
            senderName = currentDisplayName,
            senderPhotoUrl = currentPhotoUrl,
            message = text,
            type = type,
            timestamp = System.currentTimeMillis()
        )

        ref.child(msgId).setValue(chatMsg)
            .addOnFailureListener { e ->
                _errorFlow.value = "فشل الإرسال: ${e.message}"
            }
    }

    override fun onCleared() {
        super.onCleared()
        messagesListener?.let { listener ->
            databaseRef?.removeEventListener(listener)
        }
    }
}
