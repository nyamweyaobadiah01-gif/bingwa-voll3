package com.example.bingwaauto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class MpesaParserTest {

    @Test
    fun receivedFromPerson() {
        val p = MpesaParser.parse(
            "UK12ABC345 Confirmed.You have received Ksh20.00 from JOHN DOE 0712345678 " +
                "on 1/11/26 at 12:00 PM  New M-PESA balance is Ksh1,520.00."
        )
        assertNotNull(p)
        assertEquals("UK12ABC345", p!!.code)
        assertEquals(20.0, p.amount, 0.001)
        assertEquals("0712345678", p.phone)
        assertEquals("JOHN DOE", p.name)
    }

    @Test
    fun receivedPhoneFirstFormat() {
        val p = MpesaParser.parse(
            "UK12ABC346 Confirmed. Ksh50.00 received from 254712345678 - JANE W on 1/11/26 at 1:00 PM."
        )
        assertNotNull(p)
        assertEquals("0712345678", p!!.phone)
        assertEquals(50.0, p.amount, 0.001)
    }

    @Test
    fun maskedNumberGivesNullPhone() {
        val p = MpesaParser.parse(
            "UK12ABC347 Confirmed.You have received Ksh20.00 from JOHN DOE 0712***678 on 1/11/26 at 2:00 PM"
        )
        assertNotNull(p)
        assertNull(p!!.phone)
    }

    @Test
    fun sentMoneyIsIgnored() {
        val p = MpesaParser.parse(
            "UK12ABC348 Confirmed. Ksh100.00 sent to SHOP 0722000000 on 1/11/26 at 3:00 PM. " +
                "New M-PESA balance is Ksh900.00."
        )
        assertNull(p)
    }

    @Test
    fun balanceIsReadFromAnyMessage() {
        assertEquals(
            900.0,
            MpesaParser.balance("... New M-PESA balance is Ksh900.00. Transaction cost, Ksh0.00")!!,
            0.001
        )
    }

    @Test
    fun catalogRejectsMpesaMenuAndMissingPlaceholder() {
        val r = Catalog.parse("20|ok|*100*{phone}#\n30|bad|*334#\n40|nophone|*100#")
        assertEquals(1, r.bundles.size)
        assertEquals(2, r.errors.size)
    }
}
