package com.example.bingwaauto

data class Payment(
    val code: String,
    val amount: Double,
    /** Normalised to 07XXXXXXXX / 01XXXXXXXX, or null if the SMS masks the number. */
    val phone: String?,
    val name: String
)

/** Reads M-PESA confirmation SMS text. Pure Kotlin so it can be unit tested. */
object MpesaParser {

    private val CODE = Regex("""^\s*([A-Z0-9]{10})\s+Confirmed""", RegexOption.IGNORE_CASE)

    private val RECEIVED_AMOUNT = Regex(
        """(?:received\s+Ksh\.?\s*([\d,]+(?:\.\d{1,2})?))|(?:Ksh\.?\s*([\d,]+(?:\.\d{1,2})?)\s+received)""",
        RegexOption.IGNORE_CASE
    )

    private val FROM_SEGMENT = Regex(
        """from\s+(.+?)\s+on\s+\d{1,2}/\d{1,2}/\d{2,4}""",
        RegexOption.IGNORE_CASE
    )

    private val PHONE = Regex("""(?:\+?254|0)[\d*]{9}""")

    private val BALANCE = Regex(
        """New\s+M-PESA\s+balance\s+is\s+Ksh\.?\s*([\d,]+(?:\.\d{1,2})?)""",
        RegexOption.IGNORE_CASE
    )

    /** Returns a Payment only for "money received" messages, otherwise null. */
    fun parse(body: String): Payment? {
        val text = body.replace('\n', ' ')
        val code = CODE.find(text)?.groupValues?.get(1)?.uppercase() ?: return null
        val amountMatch = RECEIVED_AMOUNT.find(text) ?: return null
        val amountText = amountMatch.groupValues[1].ifEmpty { amountMatch.groupValues[2] }
        val amount = amountText.replace(",", "").toDoubleOrNull() ?: return null

        val segment = FROM_SEGMENT.find(text)?.groupValues?.get(1) ?: return null
        val phoneRaw = PHONE.find(segment)?.value
        val name = segment
            .replace(phoneRaw ?: "", "")
            .replace("-", " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        return Payment(code, amount, normalisePhone(phoneRaw), name)
    }

    /** New M-PESA balance quoted in any M-PESA message (sent, received, paid...). */
    fun balance(body: String): Double? {
        val text = body.replace('\n', ' ')
        return BALANCE.find(text)?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull()
    }

    fun normalisePhone(raw: String?): String? {
        if (raw == null || raw.contains('*')) return null
        val digits = raw.removePrefix("+")
        val local = when {
            digits.startsWith("254") && digits.length == 12 -> "0" + digits.substring(3)
            digits.startsWith("0") && digits.length == 10 -> digits
            else -> return null
        }
        return local
    }
}
