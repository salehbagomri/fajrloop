package com.bagomri.fajrloop.ui.stats

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bagomri.fajrloop.R
import com.bagomri.fajrloop.databinding.ItemLeaderboardRowBinding
import com.bumptech.glide.Glide

data class LeaderboardItem(
    val rank: Int,
    val userId: String,
    val displayName: String,
    val photoUrl: String,
    val streak: Int,
    val rescues: Int
)

class LeaderboardAdapter(
    private val items: List<LeaderboardItem>,
    private val currentUid: String
) : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemLeaderboardRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLeaderboardRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val binding = holder.binding

        // 1. تحديد شارة الترتيب ورنگ الألوان
        when (item.rank) {
            1 -> {
                binding.textRankPosition.text = "👑 1"
                binding.textRankPosition.setTextColor(Color.parseColor("#FFD700"))
            }
            2 -> {
                binding.textRankPosition.text = "🥈 2"
                binding.textRankPosition.setTextColor(Color.parseColor("#C0C0C0"))
            }
            3 -> {
                binding.textRankPosition.text = "🥉 3"
                binding.textRankPosition.setTextColor(Color.parseColor("#CD7F32"))
            }
            else -> {
                binding.textRankPosition.text = "#${item.rank}"
                binding.textRankPosition.setTextColor(Color.parseColor("#B0B0C5"))
            }
        }

        // 2. اسم العضو ومؤشر الحساب الشخصي
        if (item.userId == currentUid) {
            binding.textMemberName.text = "${item.displayName} (أنت)"
            binding.textMemberName.setTextColor(Color.parseColor("#FFD700"))
        } else {
            binding.textMemberName.text = item.displayName
            binding.textMemberName.setTextColor(Color.WHITE)
        }

        // 3. الأرقام والإحصائيات
        binding.textMemberStreak.text = item.streak.toString()
        binding.textMemberRescues.text = item.rescues.toString()

        // 4. تحميل الصورة الرمزية بـ Glide
        if (item.photoUrl.isNotEmpty()) {
            Glide.with(binding.root.context)
                .load(item.photoUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_default_avatar)
                .into(binding.imageMemberAvatar)
        } else {
            binding.imageMemberAvatar.setImageResource(R.drawable.ic_default_avatar)
        }
    }

    override fun getItemCount(): Int = items.size
}
