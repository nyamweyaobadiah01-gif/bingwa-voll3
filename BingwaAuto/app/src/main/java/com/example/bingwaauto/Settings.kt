package com.example.bingwaauto

import android.content.Context

class Settings(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("bingwa_settings", Context.MODE_PRIVATE)

    /** Master switch. Off by default so nothing is sent until you turn it on. */
    var enabled: Boolean
        get() = prefs.getBoolean("enabled", false)
        set(v) = prefs.edit().putBoolean("enabled", v).apply()

    var catalogText: String
        get() = prefs.getString("catalog", "") ?: ""
        set(v) = prefs.edit().putString("catalog", v).apply()

    /** Payments above this are held for manual approval. */
    var maxAutoAmount: Int
        get() = prefs.getInt("max_auto_amount", 500)
        set(v) = prefs.edit().putInt("max_auto_amount", v).apply()

    /** Last M-PESA balance seen in a real M-PESA SMS, used to catch fake messages. */
    var lastBalance: Double?
        get() = prefs.getString("last_balance", null)?.toDoubleOrNull()
        set(v) = prefs.edit().putString("last_balance", v?.toString()).apply()
}
