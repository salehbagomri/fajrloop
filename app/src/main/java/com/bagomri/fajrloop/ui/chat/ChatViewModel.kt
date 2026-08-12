package com.bagomri.fajrloop.ui.chat

import android.app.Application
import android.content.Context
import android.util.Log
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
        startListening()
    }

    fun startListening(activeHalqaId: String? = null) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        currentUid = currentUser.uid
        currentDisplayName = currentUser.displayName ?: "عضو"
        currentPhotoUrl = currentUser.photoUrl?.toString() ?: ""

        val prefs = getApplication<Application>().getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        val targetHalqaId = activeHalqaId
            ?: prefs.getString(AlarmPreferences.KEY_CURRENT_HALQA_ID, null)

        if (targetHalqaId.isNullOrEmpty()) {
            Log.w("ChatViewModel", "⚠️ targetHalqaId is null or empty, stopping listening")
            stopListening()
            return
        }

        if (halqaId == targetHalqaId && databaseRef != null) {
            // Already listening to this halqa
            return
        }

        // Clean up previous listener if switching halqas
        messagesListener?.let { listener ->
            databaseRef?.removeEventListener(listener)
        }

        halqaId = targetHalqaId
        databaseRef = FirebaseDatabase.getInstance()
            .getReference("chatMessages")
            .child(targetHalqaId)

        loadHalqaName()
        setupFirebaseListener()
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
                Log.d("ChatViewModel", "✅ Loaded ${newList.size} chat messages for halqa $halqaId")
            }

            override fun onCancelled(error: DatabaseError) {
                _errorFlow.value = "فشل تحميل المحادثة: ${error.message}"
                Log.e("ChatViewModel", "❌ Failed to load chat messages", error.toException())
                if (error.code == DatabaseError.PERMISSION_DENIED) {
                    stopListening()
                }
            }
        }
        ref.addValueEventListener(messagesListener!!)
    }

    fun sendMessage(text: String, type: String) {
        if (databaseRef == null) {
            startListening()
        }
        val ref = databaseRef
        if (ref == null) {
            Log.e("ChatViewModel", "❌ Cannot send message: databaseRef is null")
            _errorFlow.value = "فشل الإرسال: غير متصل بحلقة فعالة"
            return
        }

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
            .addOnSuccessListener {
                Log.d("ChatViewModel", "✅ Sent message: $text")
            }
            .addOnFailureListener { e ->
                _errorFlow.value = "فشل الإرسال: ${e.message}"
                Log.e("ChatViewModel", "❌ Failed to send message", e)
            }
    }

    fun stopListening() {
        messagesListener?.let { listener ->
            try {
                databaseRef?.removeEventListener(listener)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error removing chat listener", e)
            }
        }
        messagesListener = null
        databaseRef = null
        halqaId = null
        _messagesFlow.value = emptyList()
        _halqaNameFlow.value = ""
        Log.d("ChatViewModel", "🛑 Stopped chat listening and cleared state")
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}
