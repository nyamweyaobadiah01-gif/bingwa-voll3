package com.example.bingwaauto

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import kotlin.math.abs

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val parts = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (parts.isNullOrEmpty()) return

        val sender = parts[0].originatingAddress ?: return
        if (!sender.equals("MPESA", ignoreCase = true)) return
        val body = parts.joinToString("") { it.messageBody ?: "" }

        val settings = Settings(context)
        val balance = MpesaParser.balance(body)
        val payment = MpesaParser.parse(body)

        if (payment == null) {
            // Not a "money received" message, but still keep our balance record current.
            if (balance != null) settings.lastBalance = balance
            return
        }

        val previous = settings.lastBalance
        val balanceOk = balance == null || previous == null ||
            abs((previous + payment.amount) - balance) < 0.01
        if (balance != null) settings.lastBalance = balance

        val bundles = Catalog.parse(settings.catalogText).bundles
        val (status, detail) = when {
            payment.phone == null -> Status.NO_PHONE to "Customer number is hidden in the SMS"
            Catalog.find(bundles, payment.amount) == null ->
                Status.UNMATCHED to "No bundle listed for this amount"
            !balanceOk -> Status.REVIEW to
                "Balance did not match this payment. Check your real M-PESA balance before approving."
            payment.amount > settings.maxAutoAmount -> Status.REVIEW to
                "Over your auto-send limit of KSh ${settings.maxAutoAmount}"
            else -> Status.PENDING to "Waiting to send"
        }

        val isNew = OrderDb.get(context).insertIfNew(payment, status, detail)
        if (isNew && status == Status.PENDING && settings.enabled) {
            SendWorker.enqueue(context)
        }
    }
}
