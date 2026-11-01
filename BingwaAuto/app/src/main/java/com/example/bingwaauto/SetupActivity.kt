package com.example.bingwaauto

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SetupActivity : AppCompatActivity() {

    private lateinit var btnProceed: Button
    private lateinit var smsStatus: TextView
    private lateinit var phoneStatus: TextView
    private lateinit var backgroundStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Skip this screen once setup is done and the permissions are still granted.
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        if (prefs.getBoolean("setup_complete", false) && hasRequiredPermissions() &&
            !intent.getBooleanExtra("from_home", false)
        ) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_setup)

        btnProceed = findViewById(R.id.btnProceed)
        smsStatus = findViewById(R.id.smsStatus)
        phoneStatus = findViewById(R.id.phoneStatus)
        backgroundStatus = findViewById(R.id.backgroundStatus)

        findViewById<Button>(R.id.btnSms).setOnClickListener {
            requestPermissions(arrayOf(Manifest.permission.RECEIVE_SMS), 1001)
        }
        findViewById<Button>(R.id.btnPhone).setOnClickListener {
            requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), 1002)
        }
        findViewById<Button>(R.id.btnBackground).setOnClickListener {
            startActivity(Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        }
        findViewById<Button>(R.id.btnAppSettings).setOnClickListener {
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.fromParts("package", packageName, null)
            startActivity(intent)
        }

        btnProceed.setOnClickListener {
            if (hasRequiredPermissions()) {
                prefs.edit().putBoolean("setup_complete", true).apply()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Ui.toast(this, "Please allow the SMS and Phone permissions first")
            }
        }

        refreshStatus()
    }

    override fun onResume() {
        super.onResume()
        if (::btnProceed.isInitialized) refreshStatus()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        refreshStatus()
    }

    private fun granted(permission: String) =
        checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

    private fun hasRequiredPermissions() =
        granted(Manifest.permission.RECEIVE_SMS) && granted(Manifest.permission.CALL_PHONE)

    private fun refreshStatus() {
        setStatus(smsStatus, granted(Manifest.permission.RECEIVE_SMS), "Granted", "Missing")
        setStatus(phoneStatus, granted(Manifest.permission.CALL_PHONE), "Granted", "Missing")

        val pm = getSystemService(PowerManager::class.java)
        val unrestricted = pm != null && pm.isIgnoringBatteryOptimizations(packageName)
        backgroundStatus.text = if (unrestricted) "Allowed" else "Not set"
        backgroundStatus.setTextColor(if (unrestricted) Ui.GREEN else Ui.AMBER)

        btnProceed.isEnabled = hasRequiredPermissions()
        btnProceed.alpha = if (hasRequiredPermissions()) 1f else 0.5f
    }

    private fun setStatus(view: TextView, ok: Boolean, yes: String, no: String) {
        view.text = if (ok) yes else no
        view.setTextColor(if (ok) Ui.GREEN else Ui.RED)
    }
}
