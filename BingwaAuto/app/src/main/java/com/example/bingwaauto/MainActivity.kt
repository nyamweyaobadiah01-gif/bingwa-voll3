package com.example.bingwaauto

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

/** Home screen: on/off switch, today's figures and real order counts. */
class MainActivity : AppCompatActivity() {

    private lateinit var settings: Settings
    private lateinit var db: OrderDb
    private lateinit var switchEnabled: SwitchCompat
    private lateinit var textStatus: TextView
    private lateinit var textSent: TextView
    private lateinit var textBalance: TextView
    private lateinit var tileHost: LinearLayout
    private var updatingSwitch = false

    private val tileCounts = linkedMapOf<String, TextView>()

    private val tileDefs = listOf(
        "Completed" to listOf(Status.SENT),
        "Pending" to listOf(Status.PENDING, Status.SENDING),
        "Failed" to listOf(Status.FAILED),
        "Held" to listOf(Status.REVIEW),
        "Unmatched" to listOf(Status.UNMATCHED),
        "No number" to listOf(Status.NO_PHONE)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        settings = Settings(this)
        db = OrderDb.get(this)

        switchEnabled = findViewById(R.id.switchEnabled)
        textStatus = findViewById(R.id.textStatus)
        textSent = findViewById(R.id.textSentToday)
        textBalance = findViewById(R.id.textBalance)
        tileHost = findViewById(R.id.tileHost)

        buildTiles()

        switchEnabled.setOnCheckedChangeListener { _, checked ->
            if (updatingSwitch) return@setOnCheckedChangeListener
            if (checked) {
                if (!hasPermissions()) {
                    setSwitch(false)
                    Ui.toast(this, "Allow the SMS and Phone permissions first")
                    openSetup()
                    return@setOnCheckedChangeListener
                }
                if (Catalog.parse(settings.catalogText).bundles.isEmpty()) {
                    setSwitch(false)
                    Ui.toast(this, "Add and save at least one bundle first")
                    startActivity(Intent(this, BundlesActivity::class.java))
                    return@setOnCheckedChangeListener
                }
                settings.enabled = true
                SendWorker.enqueue(this)
            } else {
                settings.enabled = false
            }
            refresh()
        }

        findViewById<Button>(R.id.btnOrders).setOnClickListener {
            startActivity(Intent(this, OrdersActivity::class.java))
        }
        findViewById<Button>(R.id.btnBundles).setOnClickListener {
            startActivity(Intent(this, BundlesActivity::class.java))
        }
        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        findViewById<Button>(R.id.btnPrivacy).setOnClickListener {
            startActivity(Intent(this, PrivacyPolicyActivity::class.java))
        }
        findViewById<Button>(R.id.btnPermissions).setOnClickListener { openSetup() }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun openSetup() {
        startActivity(Intent(this, SetupActivity::class.java).putExtra("from_home", true))
    }

    private fun hasPermissions() =
        checkSelfPermission(Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED

    private fun setSwitch(on: Boolean) {
        updatingSwitch = true
        switchEnabled.isChecked = on
        updatingSwitch = false
    }

    private fun buildTiles() {
        tileDefs.chunked(3).forEach { rowDefs ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            rowDefs.forEach { (label, _) ->
                val tile = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    setBackgroundColor(0xFF12263D.toInt())
                    val lp = LinearLayout.LayoutParams(0, Ui.dp(this@MainActivity, 92), 1f)
                    val m = Ui.dp(this@MainActivity, 5)
                    lp.setMargins(m, m, m, m)
                    layoutParams = lp
                }
                val count = TextView(this).apply {
                    text = "0"
                    textSize = 26f
                    setTextColor(0xFFF2F4F7.toInt())
                    gravity = Gravity.CENTER
                }
                val name = TextView(this).apply {
                    text = label
                    textSize = 13f
                    setTextColor(0xFFAFC1D6.toInt())
                    gravity = Gravity.CENTER
                }
                tile.addView(count)
                tile.addView(name)
                row.addView(tile)
                tileCounts[label] = count
            }
            tileHost.addView(row)
        }
    }

    private fun refresh() {
        val bundles = Catalog.parse(settings.catalogText).bundles
        val counts = db.countByStatus()

        tileDefs.forEach { (label, statuses) ->
            tileCounts[label]?.text = statuses.sumOf { counts[it] ?: 0 }.toString()
        }

        textSent.text = Ui.money(db.sentSince(Ui.startOfToday()))
        textBalance.text = settings.lastBalance?.let { Ui.money(it) } ?: "Not seen yet"

        setSwitch(settings.enabled)
        textStatus.text = when {
            !hasPermissions() -> "Permissions missing. Tap Permissions below."
            settings.enabled -> "ON. ${bundles.size} bundle(s) listed. Auto-send limit KSh ${settings.maxAutoAmount}."
            else -> "OFF. Nothing will be sent."
        }
    }
}
