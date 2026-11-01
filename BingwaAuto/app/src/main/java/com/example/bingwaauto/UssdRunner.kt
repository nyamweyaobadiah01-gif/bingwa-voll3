package com.example.bingwaauto

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyManager
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

data class UssdResult(val ok: Boolean, val message: String)

/** Sends one single-string USSD request and waits for the network's reply. */
object UssdRunner {

    private val FAILURE_WORDS = listOf(
        "insufficient", "not enough", "invalid", "failed", "unable", "sorry",
        "error", "not eligible", "cannot", "can't"
    )

    fun run(context: Context, code: String): UssdResult {
        if (context.checkSelfPermission(Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return UssdResult(false, "Phone (CALL_PHONE) permission is missing")
        }
        val tm = context.getSystemService(TelephonyManager::class.java)
            ?: return UssdResult(false, "No telephony service")

        val latch = CountDownLatch(1)
        var result = UssdResult(false, "No reply from the network")

        try {
            tm.sendUssdRequest(
                code,
                object : TelephonyManager.UssdResponseCallback() {
                    override fun onReceiveUssdResponse(
                        telephonyManager: TelephonyManager,
                        request: String,
                        response: CharSequence
                    ) {
                        val text = response.toString().trim()
                        val bad = FAILURE_WORDS.any { text.contains(it, ignoreCase = true) }
                        result = UssdResult(!bad, text)
                        latch.countDown()
                    }

                    override fun onReceiveUssdResponseFailed(
                        telephonyManager: TelephonyManager,
                        request: String,
                        failureCode: Int
                    ) {
                        result = UssdResult(false, "USSD request failed (code $failureCode)")
                        latch.countDown()
                    }
                },
                Handler(Looper.getMainLooper())
            )
        } catch (e: SecurityException) {
            return UssdResult(false, "Not allowed to send USSD: ${e.message}")
        } catch (e: Exception) {
            return UssdResult(false, e.message ?: "Could not send USSD")
        }

        latch.await(45, TimeUnit.SECONDS)
        return result
    }
}
