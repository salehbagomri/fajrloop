package com.bagomri.fajrloop.ui.settings

import android.os.Bundle
import com.bagomri.fajrloop.ui.BaseActivity
import com.bagomri.fajrloop.databinding.ActivityGuideBinding

class GuideActivity : BaseActivity() {

    private lateinit var binding: ActivityGuideBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
    }
}
