package com.example.bingwaauto

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var settings: Settings
    private lateinit var editLimit: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        settings = Settings(this)
        editLimit = findViewById(R.id.editLimit)
        editLimit.setText(settings.maxAutoAmount.toString())

        findViewById<Button>(R.id.btnSaveLimit).setOnClickListener {
            val value = editLimit.text.toString().trim().toIntOrNull()
            if (value == null || value < 1 || value > 100000) {
                Ui.toast(this, "Enter a whole number from 1 to 100000")
            } else {
                settings.maxAutoAmount = value
                Ui.toast(this, "Saved. Payments above KSh $value will be held for you")
            }
        }

        findViewById<Button>(R.id.btnClearHistory).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Clear order history?")
                .setMessage(
                    "This erases every order record on this phone. " +
                        "The app will no longer remember which M-PESA codes it has already served."
                )
                .setPositiveButton("Clear") { _, _ ->
                    OrderDb.get(this).clearAll()
                    Ui.toast(this, "Order history cleared")
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
    }
}
