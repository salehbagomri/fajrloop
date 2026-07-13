package com.bagomri.fajrloop.ui.adhkar

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bagomri.fajrloop.R
import com.bagomri.fajrloop.databinding.ActivityMorningAdhkarBinding

/**
 * MorningAdhkarActivity — شاشة أذكار الصباح التفاعلية بعد تأكيد الاستيقاظ
 */
class MorningAdhkarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMorningAdhkarBinding

    private val adhkarList = listOf(
        DhikrItem(
            "أعوذ بالله من الشيطان الرجيم: {اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَؤُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ}",
            1
        ),
        DhikrItem(
            "بسم الله الرحمن الرحيم: {قُلْ هُوَ اللَّهُ أَحَدٌ * اللَّهُ الصَّمَدُ * لَمْ يَلِدْ وَلَمْ يُولَدْ * وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ}",
            3
        ),
        DhikrItem(
            "بسم الله الرحمن الرحيم: {قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ * مِن شَرِّ مَا خَلَقَ * وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ * وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ * وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ}",
            3
        ),
        DhikrItem(
            "بسم الله الرحمن الرحيم: {قُلْ أَعُوذُ بِرَبِّ النَّاسِ * مَلِكِ النَّاسِ * إِلَٰهِ النَّاسِ * مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ * الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ * مِنَ الْجِنَّةِ وَالنَّاسِ}",
            3
        ),
        DhikrItem(
            "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لاَ إِلَهَ إلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، رَبِّ أَسْأَلُكَ خَيْرَ مَا فِي هَذَا الْيَوْمِ وَخَيْرَ مَا بَعْدَهُ، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِي هَذَا الْيَوْمِ وَشَرِّ مَا بَعْدَهُ، رَبِّ أَعُوذُ بِكَ مِنَ الْكَسَلِ، وَسُوءِ الْكِبَرِ، رَبِّ أَعُوذُ بِكَ مِنْ عَذَابٍ فِي النَّارِ وَعَذَابٍ فِي الْقَبْرِ.",
            1
        ),
        DhikrItem(
            "اللَّهُمَّ بِكَ أَصْبَحْنَا، وَبِكَ أَمْسَيْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ وَإِلَيْكَ النُّشُورُ.",
            1
        ),
        DhikrItem(
            "اللَّهُمَّ أَنْتَ رَبِّي لاَ إِلَهَ إلاَّ أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لاَ يَغْفِرُ الذُّنُوبَ إلاَّ أَنْتَ. (سيد الاستغفار)",
            1
        ),
        DhikrItem(
            "اللَّهُمَّ إِنِّي أَصْبَحْتُ أُشْهِدُكَ، وَأُشْهِدُ حَمَلَةَ عَرْشِكَ، وَمَلاَئِكَتَكَ، وَجَمِيعَ خَلْقِكَ، أَنَّكَ أَنْتَ اللَّهُ لاَ إِلَهَ إلاَّ أَنْتَ وَحْدَهُ لاَ شَرِيكَ لَهُ، وَأَنَّ مُحَمَّداً عَبْدُكَ وَرَسُولُكَ.",
            4
        ),
        DhikrItem(
            "رَضِيتُ بِاللَّهِ رَبَّاً، وَبِالإِسْلاَمِ دِيناً، وَبِمُحَمَّدٍ صلى الله عليه وسلم نَبِيَّاً.",
            3
        ),
        DhikrItem(
            "بِسْمِ اللَّهِ الَّذِي لاَ يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الأَرْضِ وَلاَ فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ.",
            3
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMorningAdhkarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()

        binding.btnCloseAdhkar.setOnClickListener {
            finish()
        }
    }

    private fun setupViewPager() {
        val adapter = AdhkarAdapter(adhkarList) { position ->
            val nextPosition = position + 1
            if (nextPosition < adhkarList.size) {
                binding.viewPagerAdhkar.postDelayed({
                    binding.viewPagerAdhkar.setCurrentItem(nextPosition, true)
                }, 300)
            } else {
                Toast.makeText(this, "تقبل الله طاعاتكم وغفر ذنوبكم 🌅", Toast.LENGTH_LONG).show()
                binding.btnCloseAdhkar.text = "تقبل الله 🌅 (اضغط للإغلاق)"
            }
        }

        binding.viewPagerAdhkar.adapter = adapter
        binding.viewPagerAdhkar.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.textPageIndicator.text = "${position + 1} / ${adhkarList.size}"
            }
        })
    }

    data class DhikrItem(val text: String, val targetCount: Int)

    class AdhkarAdapter(
        private val items: List<DhikrItem>,
        private val onDhikrCompleted: (Int) -> Unit
    ) : RecyclerView.Adapter<AdhkarAdapter.ViewHolder>() {

        private val counts = items.map { it.targetCount }.toMutableList()

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textDhikr: TextView = view.findViewById(R.id.text_dhikr_text)
            val textTarget: TextView = view.findViewById(R.id.text_dhikr_target)
            val textCountNumber: TextView = view.findViewById(R.id.text_counter_number)
            val btnCounterTap: FrameLayout = view.findViewById(R.id.btn_counter_tap)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_adhkar, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.textDhikr.text = item.text
            holder.textTarget.text = "التكرار المطلوب: ${item.targetCount} ${if (item.targetCount == 1) "مرة واحدة" else "مرات"}"
            holder.textCountNumber.text = counts[position].toString()

            holder.btnCounterTap.setOnClickListener {
                val currentCount = counts[position]
                if (currentCount > 0) {
                    val newCount = currentCount - 1
                    counts[position] = newCount
                    holder.textCountNumber.text = newCount.toString()

                    if (newCount == 0) {
                        holder.btnCounterTap.setBackgroundResource(R.drawable.bg_circle_icon)
                        holder.btnCounterTap.background.setTint(Color.parseColor("#202ECC71"))
                        holder.textCountNumber.setTextColor(Color.parseColor("#2ECC71"))
                        holder.textCountNumber.text = "✓"
                        onDhikrCompleted(position)
                    }
                }
            }
        }

        override fun getItemCount() = items.size
    }
}
