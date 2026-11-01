package com.example.bingwaauto

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters

/** Sends pending orders one at a time. */
class SendWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val settings = Settings(applicationContext)
        if (!settings.enabled) return Result.success()

        val db = OrderDb.get(applicationContext)
        val bundles = Catalog.parse(settings.catalogText).bundles

        for (order in db.withStatus(Status.PENDING)) {
            if (!settings.enabled) break

            val phone = order.phone
            if (phone == null) {
                db.setStatus(order.id, Status.NO_PHONE, "Customer number is hidden in the SMS")
                continue
            }
            val bundle = Catalog.find(bundles, order.amount)
            if (bundle == null) {
                db.setStatus(order.id, Status.UNMATCHED, "No bundle listed for this amount")
                continue
            }

            // Mark first. If the app dies mid-request we will NOT dial this order again by itself.
            db.setStatus(order.id, Status.SENDING, "Sending ${bundle.label}")
            val ussd = bundle.ussdTemplate.replace("{phone}", phone)
            val result = UssdRunner.run(applicationContext, ussd)

            if (result.ok) {
                db.setStatus(order.id, Status.SENT, "${bundle.label}: ${result.message}")
            } else {
                db.setStatus(order.id, Status.FAILED, "${bundle.label}: ${result.message}")
            }
            Thread.sleep(3000) // short gap between requests
        }
        return Result.success()
    }

    companion object {
        fun enqueue(context: Context) {
            WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
                "send_pending",
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                OneTimeWorkRequestBuilder<SendWorker>().build()
            )
        }
    }
}
