package com.spendsass.screens.settings

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.spendsass.R
import com.spendsass.screens.dashboard.DashboardActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            })
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        })
        finish()
    }
}