package com.example.bingwaauto

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

/** Order history. Tap a failed or held order to send it again. */
class OrdersActivity : AppCompatActivity() {

    private lateinit var settings: Settings
    private lateinit var db: OrderDb
    private lateinit var list: ListView
    private var orders: List<Order> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)

        settings = Settings(this)
        db = OrderDb.get(this)
        list = findViewById(R.id.listOrders)

        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnRefresh).setOnClickListener { refresh() }
        findViewById<Button>(R.id.btnProcess).setOnClickListener {
            if (!settings.enabled) {
                Ui.toast(this, "Turn on Auto-send on the Home screen first")
            } else {
                SendWorker.enqueue(this)
                Ui.toast(this, "Processing pending orders")
            }
        }

        list.setOnItemClickListener { _, _, position, _ -> onOrderTapped(orders[position]) }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun onOrderTapped(order: Order) {
        val canRetry = order.status == Status.FAILED || order.status == Status.REVIEW ||
            order.status == Status.SENDING
        val message = buildString {
            append("KSh ${order.amount.toInt()} from ${order.name} ${order.phone ?: "(number hidden)"}\n")
            append("M-PESA code ${order.code}\n\n${order.detail}")
            if (order.status == Status.SENDING || order.status == Status.FAILED) {
                append("\n\nCheck the customer really has no bundle before you send again, or they may get it twice.")
            }
        }
        val builder = AlertDialog.Builder(this).setTitle(order.status).setMessage(message)
        if (canRetry && order.phone != null) {
            builder.setPositiveButton("Send now") { _, _ ->
                db.setStatus(order.id, Status.PENDING, "Approved by you")
                if (settings.enabled) SendWorker.enqueue(this)
                else Ui.toast(this, "Turn on Auto-send to send")
                refresh()
            }
            builder.setNegativeButton("Close", null)
        } else {
            builder.setPositiveButton("Close", null)
        }
        builder.show()
    }

    private fun refresh() {
        orders = db.recent()
        val lines = orders.map {
            "${it.status}  KSh ${it.amount.toInt()}  ${it.phone ?: "hidden"}\n" +
                "${it.code}  ${Ui.time(it.created)}\n${it.detail}"
        }
        list.adapter = ArrayAdapter(this, R.layout.item_order, R.id.itemText, lines)
    }
}
