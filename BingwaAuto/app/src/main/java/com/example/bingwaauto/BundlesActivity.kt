package com.example.bingwaauto

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

/** Edit the bundle list: amount|label|USSD code with {phone}. */
class BundlesActivity : AppCompatActivity() {

    private lateinit var settings: Settings
    private lateinit var editCatalog: EditText
    private lateinit var textCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bundles)

        settings = Settings(this)
        editCatalog = findViewById(R.id.editCatalog)
        textCount = findViewById(R.id.textCount)

        editCatalog.setText(settings.catalogText)
        updateCount()

        findViewById<Button>(R.id.btnSave).setOnClickListener { save() }
        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun updateCount() {
        val n = Catalog.parse(settings.catalogText).bundles.size
        textCount.text = "$n bundle(s) saved"
    }

    private fun save() {
        val text = editCatalog.text.toString()
        val result = Catalog.parse(text)
        if (result.errors.isNotEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Fix these lines")
                .setMessage(result.errors.joinToString("\n"))
                .setPositiveButton("OK", null)
                .show()
            return
        }
        settings.catalogText = text
        updateCount()
        Ui.toast(this, "Saved ${result.bundles.size} bundle(s)")
    }
}
