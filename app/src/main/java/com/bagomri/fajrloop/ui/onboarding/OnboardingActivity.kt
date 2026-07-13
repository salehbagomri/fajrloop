package com.bagomri.fajrloop.ui.onboarding

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.bagomri.fajrloop.ui.BaseActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bagomri.fajrloop.R
import com.bagomri.fajrloop.alarm.AlarmPreferences
import com.bagomri.fajrloop.databinding.ActivityOnboardingBinding
import com.bagomri.fajrloop.ui.auth.LoginActivity

/**
 * OnboardingActivity — شاشات الترحيب والتعريف بالتطبيق عند أول تشغيل
 */
class OnboardingActivity : BaseActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private val onboardingItems = listOf(
        OnboardingItem("🌙", "مرحباً بك في حلقة الفجر 🌙", "نظام حلقات تفاعلي يساعدك وأعضاء حلقتك على الاستيقاظ لصلاة الفجر جماعة يومياً."),
        OnboardingItem("🤝", "كوّن حلقتك الأولى 🤝", "أنشئ حلقة جديدة، ادعُ أصدقاءك، وكن مسؤولاً عن إيقاظهم ليكونوا هم أيضاً عوناً لك."),
        OnboardingItem("⏰", "منبه ذكي لا يمكن تجاهله ⏰", "لن يتوقف منبهك عن الرنين إلا بعد حل التحدي والحصول على تأكيد الاستيقاظ من صديقك المسؤول."),
        OnboardingItem("🚀", "ابدأ رحلتك الآن! 🚀", "سجل الدخول، انضم لحلقة الفجر، واستمتع بنشاط الصباح وأجره العظيم.")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        setupDots()
        updateDots(0)

        binding.btnSkip.setOnClickListener {
            completeOnboarding()
        }

        binding.btnNext.setOnClickListener {
            val current = binding.viewPager.currentItem
            if (current < onboardingItems.size - 1) {
                binding.viewPager.currentItem = current + 1
            } else {
                completeOnboarding()
            }
        }
    }

    private fun setupViewPager() {
        binding.viewPager.adapter = OnboardingAdapter(onboardingItems)
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateDots(position)
                if (position == onboardingItems.size - 1) {
                    binding.btnNext.text = "ابدأ الآن 🚀"
                    binding.btnSkip.visibility = View.GONE
                } else {
                    binding.btnNext.text = "التالي"
                    binding.btnSkip.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun setupDots() {
        binding.layoutDots.removeAllViews()
        for (i in onboardingItems.indices) {
            val dot = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(24, 24).apply {
                    setMargins(8, 0, 8, 0)
                }
                setBackgroundResource(R.drawable.bg_circle_icon)
                background.setTint(Color.parseColor("#40FFFFFF"))
            }
            binding.layoutDots.addView(dot)
        }
    }

    private fun updateDots(position: Int) {
        for (i in 0 until binding.layoutDots.childCount) {
            val dot = binding.layoutDots.getChildAt(i)
            if (i == position) {
                dot.layoutParams = LinearLayout.LayoutParams(48, 24).apply {
                    setMargins(8, 0, 8, 0)
                }
                dot.background.setTint(Color.parseColor("#FFD700"))
            } else {
                dot.layoutParams = LinearLayout.LayoutParams(24, 24).apply {
                    setMargins(8, 0, 8, 0)
                }
                dot.background.setTint(Color.parseColor("#40FFFFFF"))
            }
        }
    }

    private fun completeOnboarding() {
        val prefs = getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("onboarding_completed", true).apply()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    data class OnboardingItem(val icon: String, val title: String, val desc: String)

    class OnboardingAdapter(private val items: List<OnboardingItem>) :
        RecyclerView.Adapter<OnboardingAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textIcon: TextView = view.findViewById(R.id.text_onboarding_icon)
            val textTitle: TextView = view.findViewById(R.id.text_onboarding_title)
            val textDesc: TextView = view.findViewById(R.id.text_onboarding_desc)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_onboarding, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.textIcon.text = item.icon
            holder.textTitle.text = item.title
            holder.textDesc.text = item.desc
        }

        override fun getItemCount() = items.size
    }
}
