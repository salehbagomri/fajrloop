package com.bagomri.fajrloop.ui.chat

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.bagomri.fajrloop.R
import com.bagomri.fajrloop.data.ChatMessage
import com.bagomri.fajrloop.databinding.ItemChatBubbleBinding
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(
    private val messages: List<ChatMessage>,
    private val currentUid: String
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(val binding: ItemChatBubbleBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBubbleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val item = messages[position]
        val binding = holder.binding

        // 1. إعادة تعيين الرؤية الافتراضية لكافة التخطيطات الفرعية
        binding.textSystemMessage.visibility = View.GONE
        binding.layoutMotivational.visibility = View.GONE
        binding.layoutNormal.visibility = View.GONE

        // 2. توزيع العرض حسب نوع الرسالة
        when (item.type) {
            "system" -> {
                binding.textSystemMessage.visibility = View.VISIBLE
                binding.textSystemMessage.text = item.message
            }
            "motivational" -> {
                binding.layoutMotivational.visibility = View.VISIBLE
                binding.textMotivationalHeader.text = "رسالة تحفيزية من ${item.senderName} 🌅"
                binding.textMotivationalBody.text = "«${item.message}»"
            }
            else -> { // normal
                binding.layoutNormal.visibility = View.VISIBLE
                val isMe = item.senderId == currentUid

                // إعداد محتوى الرسالة والوقت
                binding.textChatMessageBody.text = item.message
                binding.textChatMessageTime.text = formatTime(item.timestamp)

                if (isMe) {
                    // رسالة المستخدم الحالي (يمين، بنفسجي معتم، بدون أفاتار أو اسم)
                    binding.layoutNormal.gravity = Gravity.END
                    binding.imageChatAvatar.visibility = View.GONE
                    binding.textChatSenderName.visibility = View.GONE
                    binding.layoutNormalBubble.setBackgroundResource(R.drawable.bg_chat_bubble_me)

                    // تعديل الهوامش للرسالة الخاصة
                    val params = binding.layoutNormalBubble.layoutParams as LinearLayout.LayoutParams
                    params.marginStart = 40.dpToPx(binding.root.context)
                    params.marginEnd = 0
                    binding.layoutNormalBubble.layoutParams = params
                } else {
                    // رسالة المستلمين الآخرين (يسار، سطحي معتم، بأفاتار واسم العضو)
                    binding.layoutNormal.gravity = Gravity.START
                    binding.imageChatAvatar.visibility = View.VISIBLE
                    binding.textChatSenderName.visibility = View.VISIBLE
                    binding.textChatSenderName.text = item.senderName
                    binding.layoutNormalBubble.setBackgroundResource(R.drawable.bg_chat_bubble_other)

                    // تعديل الهوامش للرسالة الخارجية
                    val params = binding.layoutNormalBubble.layoutParams as LinearLayout.LayoutParams
                    params.marginEnd = 40.dpToPx(binding.root.context)
                    params.marginStart = 0
                    binding.layoutNormalBubble.layoutParams = params

                    // تحميل الأفاتار بـ Glide
                    val avatarUrl = getPhotoUrl(item)
                    if (avatarUrl.isNotEmpty()) {
                        Glide.with(binding.root.context)
                            .load(avatarUrl)
                            .circleCrop()
                            .placeholder(R.drawable.ic_default_avatar)
                            .into(binding.imageChatAvatar)
                    } else {
                        binding.imageChatAvatar.setImageResource(R.drawable.ic_default_avatar)
                    }
                }
            }
        }
    }

    override fun getItemCount() = messages.size

    private fun formatTime(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("hh:mm a", Locale("ar"))
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            ""
        }
    }

    private fun getPhotoUrl(message: ChatMessage): String {
        return message.senderPhotoUrl
    }

    private fun Int.dpToPx(context: android.content.Context): Int {
        val density = context.resources.displayMetrics.density
        return (this * density).toInt()
    }
}
