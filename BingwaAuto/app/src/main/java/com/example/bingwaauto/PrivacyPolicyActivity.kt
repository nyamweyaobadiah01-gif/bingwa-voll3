package com.example.bingwaauto

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PrivacyPolicyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)

        findViewById<TextView>(R.id.textPolicy).text = PolicyText.FULL
        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
    }
}
