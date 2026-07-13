package com.bagomri.fajrloop.ui.stats

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bagomri.fajrloop.databinding.ItemAchievementCardBinding

data class Achievement(
    val id: String,
    val title: String,
    val desc: String,
    val emoji: String,
    val colorCode: String,
    val acquiredDate: String? // null إذا لم يُكتسب بعد
)

class AchievementsAdapter(
    private val items: List<Achievement>
) : RecyclerView.Adapter<AchievementsAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemAchievementCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAchievementCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val binding = holder.binding

        binding.textBadgeEmoji.text = item.emoji
        binding.textBadgeTitle.text = item.title
        binding.textBadgeDesc.text = item.desc

        // 1. إعداد لون الحدود المتوهجة للدائرة برمجياً
        val strokeColor = Color.parseColor(item.colorCode)
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor("#1AFFFFFF"))
            setStroke(2.dpToPx(binding.root.context), strokeColor)
        }
        binding.layoutBadgeCircle.background = drawable

        // 2. التحقق من حالة الاكتساب
        if (item.acquiredDate != null) {
            binding.textBadgeDate.text = "حصلت عليه في: ${item.acquiredDate}"
            binding.textBadgeDate.visibility = View.VISIBLE
            binding.root.alpha = 1.0f
            binding.btnShareBadge.visibility = View.VISIBLE

            // تفعيل زر المشاركة
            binding.btnShareBadge.setOnClickListener {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "لقد حصلت على وسام «${item.title}» في تطبيق حلقة الفجر! 🌅🏆\nالمتطلب: ${item.desc}\nانضم إلينا وحافظ على صلاتك!"
                    )
                }
                binding.root.context.startActivity(Intent.createChooser(shareIntent, "مشاركة الإنجاز"))
            }
        } else {
            // وسام مقفل
            binding.textBadgeDate.visibility = View.GONE
            binding.root.alpha = 0.4f
            binding.btnShareBadge.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = items.size

    private fun Int.dpToPx(context: android.content.Context): Int {
        val density = context.resources.displayMetrics.density
        return (this * density).toInt()
    }
}
