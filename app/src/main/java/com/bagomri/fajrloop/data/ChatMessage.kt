package com.bagomri.fajrloop.data

/**
 * ChatMessage — نموذج بيانات رسالة الشات في حلقة الفجر
 */
data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderPhotoUrl: String = "", // رابط الصورة لتسريع التحميل
    val message: String = "",
    val type: String = "normal", // normal, system, motivational
    val timestamp: Long = 0L
)
