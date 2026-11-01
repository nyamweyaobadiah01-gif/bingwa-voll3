package com.example.bingwaauto

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

object Status {
    const val PENDING = "PENDING"        // waiting to be sent
    const val SENDING = "SENDING"        // dialled, outcome unknown (never retried automatically)
    const val SENT = "SENT"
    const val FAILED = "FAILED"
    const val UNMATCHED = "UNMATCHED"    // amount is not in your bundle list
    const val NO_PHONE = "NO_PHONE"      // SMS hid the customer's number
    const val REVIEW = "REVIEW"          // held: balance check failed or amount over your limit
}

data class Order(
    val id: Long,
    val code: String,
    val amount: Double,
    val phone: String?,
    val name: String,
    val status: String,
    val detail: String,
    val created: Long
)

class OrderDb private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, "orders.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE orders(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "code TEXT UNIQUE NOT NULL, " +
                "amount REAL NOT NULL, " +
                "phone TEXT, name TEXT, " +
                "status TEXT NOT NULL, detail TEXT, " +
                "created INTEGER NOT NULL)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    /** Returns false if this M-PESA transaction code was already recorded. */
    @Synchronized
    fun insertIfNew(p: Payment, status: String, detail: String): Boolean {
        val values = ContentValues().apply {
            put("code", p.code)
            put("amount", p.amount)
            put("phone", p.phone)
            put("name", p.name)
            put("status", status)
            put("detail", detail)
            put("created", System.currentTimeMillis())
        }
        return writableDatabase.insertWithOnConflict(
            "orders", null, values, SQLiteDatabase.CONFLICT_IGNORE
        ) != -1L
    }

    @Synchronized
    fun setStatus(id: Long, status: String, detail: String) {
        val values = ContentValues().apply {
            put("status", status)
            put("detail", detail)
        }
        writableDatabase.update("orders", values, "id = ?", arrayOf(id.toString()))
    }

    @Synchronized
    fun withStatus(status: String): List<Order> = query("status = ?", arrayOf(status), "id ASC", null)

    @Synchronized
    fun recent(limit: Int = 200): List<Order> = query(null, null, "id DESC", limit.toString())

    /** How many orders there are in each status. */
    @Synchronized
    fun countByStatus(): Map<String, Int> {
        val out = mutableMapOf<String, Int>()
        readableDatabase.rawQuery("SELECT status, COUNT(*) FROM orders GROUP BY status", null).use { c ->
            while (c.moveToNext()) out[c.getString(0)] = c.getInt(1)
        }
        return out
    }

    /** Total KSh of orders sent since the given time (start of today). */
    @Synchronized
    fun sentSince(sinceMillis: Long): Double {
        readableDatabase.rawQuery(
            "SELECT COALESCE(SUM(amount), 0) FROM orders WHERE status = ? AND created >= ?",
            arrayOf(Status.SENT, sinceMillis.toString())
        ).use { c ->
            return if (c.moveToFirst()) c.getDouble(0) else 0.0
        }
    }

    /** Erases the whole order history from this phone. */
    @Synchronized
    fun clearAll() {
        writableDatabase.delete("orders", null, null)
    }

    private fun query(where: String?, args: Array<String>?, order: String, limit: String?): List<Order> {
        val out = mutableListOf<Order>()
        readableDatabase.query(
            "orders", null, where, args, null, null, order, limit
        ).use { c ->
            while (c.moveToNext()) {
                out += Order(
                    id = c.getLong(c.getColumnIndexOrThrow("id")),
                    code = c.getString(c.getColumnIndexOrThrow("code")),
                    amount = c.getDouble(c.getColumnIndexOrThrow("amount")),
                    phone = c.getString(c.getColumnIndexOrThrow("phone")),
                    name = c.getString(c.getColumnIndexOrThrow("name")) ?: "",
                    status = c.getString(c.getColumnIndexOrThrow("status")),
                    detail = c.getString(c.getColumnIndexOrThrow("detail")) ?: "",
                    created = c.getLong(c.getColumnIndexOrThrow("created"))
                )
            }
        }
        return out
    }

    companion object {
        @Volatile private var instance: OrderDb? = null
        fun get(context: Context): OrderDb =
            instance ?: synchronized(this) {
                instance ?: OrderDb(context).also { instance = it }
            }
    }
}
