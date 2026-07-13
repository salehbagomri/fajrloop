package com.bagomri.fajrloop.ui.permissions

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.bagomri.fajrloop.R
import com.bagomri.fajrloop.databinding.ItemPermissionRowBinding

/**
 * PermissionRowView — عنصر مخصص لعرض حالة صلاحية واحدة
 *
 * يُستخدم في PermissionSetupActivity لعرض كل صلاحية بشكل مرئي:
 * - أيقونة خضراء ✓ إذا ممنوحة
 * - أيقونة حمراء ✗ مع زر "منح" إذا غير ممنوحة
 */
class PermissionRowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ItemPermissionRowBinding

    init {
        binding = ItemPermissionRowBinding.inflate(LayoutInflater.from(context), this, true)
    }

    fun setTitle(title: String) {
        binding.textPermissionTitle.text = title
    }

    fun setDescription(desc: String) {
        binding.textPermissionDesc.text = desc
    }

    fun setStatus(granted: Boolean) {
        if (granted) {
            binding.iconStatus.setImageResource(R.drawable.ic_circle_check)
            binding.btnAction.visibility = View.GONE
        } else {
            binding.iconStatus.setImageResource(R.drawable.ic_circle_warning)
            binding.btnAction.visibility = View.VISIBLE
        }
    }

    fun setOnActionClick(action: () -> Unit) {
        binding.btnAction.setOnClickListener { action() }
    }
}

