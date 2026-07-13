package com.bagomri.fajrloop.ui.stats

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.bagomri.fajrloop.R
import java.util.*

class CalendarAdapter(
    private val context: Context,
    private val dayStatusMap: Map<Int, String>, // DayOfMonth -> Status ("awake", "travel", "missed", "challenge_done")
    private val currentDayOfMonth: Int
) : BaseAdapter() {

    private val inflater = LayoutInflater.from(context)
    private val totalDays: Int
    private val offset: Int

    init {
        val cal = Calendar.getInstance()
        totalDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        // حساب إزاحة البداية لتطابق يوم السبت كأول عمود
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        // SATURDAY = 7, SUNDAY = 1, MONDAY = 2, ...
        offset = (dayOfWeek - Calendar.SATURDAY + 7) % 7
    }

    override fun getCount(): Int = totalDays + offset

    override fun getItem(position: Int): Any? {
        if (position < offset) return null
        return position - offset + 1
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = inflater.inflate(R.layout.item_calendar_day, parent, false)
            holder = ViewHolder(view.findViewById(R.id.text_day_number))
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        if (position < offset) {
            // خانات فارغة قبل بداية الشهر
            holder.textDay.text = ""
            view.setBackgroundColor(Color.TRANSPARENT)
            view.visibility = View.INVISIBLE
        } else {
            view.visibility = View.VISIBLE
            val day = position - offset + 1
            holder.textDay.text = day.toString()

            // تحديد الخلفية واللون بناءً على الحالة واليوم الحالي
            val status = dayStatusMap[day]
            
            when {
                day == currentDayOfMonth -> {
                    // اليوم الحالي (بوردر ذهبي)
                    view.setBackgroundResource(R.drawable.bg_calendar_day_today)
                    holder.textDay.setTextColor(Color.parseColor("#FFD700"))
                }
                status == "awake" || status == "challenge_done" -> {
                    // مستيقظ (خلفية خضراء خفيفة وبوردر أخضر)
                    view.setBackgroundResource(R.drawable.bg_calendar_day_awake)
                    holder.textDay.setTextColor(Color.parseColor("#2ECC71"))
                }
                status == "travel" -> {
                    // مسافر (خلفية زرقاء خفيفة وبوردر أزرق)
                    view.setBackgroundResource(R.drawable.bg_calendar_day_travel)
                    holder.textDay.setTextColor(Color.parseColor("#3498DB"))
                }
                status == "missed" -> {
                    // فاته الفجر (خلفية حمراء خفيفة وبوردر أحمر)
                    view.setBackgroundResource(R.drawable.bg_calendar_day_missed)
                    holder.textDay.setTextColor(Color.parseColor("#E74C3C"))
                }
                else -> {
                    // لا يوجد سجل أو أيام المستقبل
                    view.setBackgroundResource(R.drawable.bg_calendar_day_default)
                    holder.textDay.setTextColor(Color.parseColor("#B0B0C5"))
                }
            }
        }

        return view
    }

    private class ViewHolder(val textDay: TextView)
}
