package com.example.bingwaauto

data class Bundle(val amount: Int, val label: String, val ussdTemplate: String)

data class CatalogResult(val bundles: List<Bundle>, val errors: List<String>)

/**
 * The bundle list is typed in by the user, one per line:
 *   amount|label|USSD code containing {phone}
 * Nothing is hard-coded, so you can only send codes you have put there yourself.
 */
object Catalog {

    fun parse(text: String): CatalogResult {
        val bundles = mutableListOf<Bundle>()
        val errors = mutableListOf<String>()
        text.lines().forEachIndexed { index, rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty()) return@forEachIndexed
            val n = index + 1
            val parts = line.split("|").map { it.trim() }
            if (parts.size != 3) {
                errors += "Line $n: use amount|label|code"
                return@forEachIndexed
            }
            val amount = parts[0].toIntOrNull()
            val code = parts[2]
            when {
                amount == null || amount <= 0 -> errors += "Line $n: amount must be a whole number"
                !code.startsWith("*") || !code.endsWith("#") -> errors += "Line $n: code must start with * and end with #"
                !code.contains("{phone}") -> errors += "Line $n: code must contain {phone}"
                code.startsWith("*334") -> errors += "Line $n: the M-PESA menu (*334) is blocked on purpose"
                bundles.any { it.amount == amount } -> errors += "Line $n: amount $amount is listed twice"
                else -> bundles += Bundle(amount, parts[1], code)
            }
        }
        return CatalogResult(bundles, errors)
    }

    fun find(bundles: List<Bundle>, amount: Double): Bundle? {
        if (amount % 1.0 != 0.0) return null
        return bundles.firstOrNull { it.amount == amount.toInt() }
    }
}
