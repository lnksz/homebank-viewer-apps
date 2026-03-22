package org.kristof.hbviewer

import kotlin.test.Test
import kotlin.test.assertEquals

class HomeBankParserTest {
    @Test
    fun parsesAccountsTransactionsAndSplits() {
        val xml = """
            <homebank v="5.0">
              <properties title="Household" />
              <account key="1" pos="0" name="Checking" type="1" curr="1" flags="0" />
              <pay key="2" name="Supermarket" />
              <cat key="10" name="Groceries" />
              <tag key="4" name="food" />
              <ope date="739309" amount="-20.5" account="1" payee="2" category="10" wording="Weekly shop" info="Receipt 77" tags="4" scat="10||10" samt="-10.0||-10.5" smem="veg||fruit" />
            </homebank>
        """.trimIndent()

        val result = HomeBankParser().parse(xml)

        assertEquals("Household", result.title)
        assertEquals(1, result.accounts.size)
        assertEquals("Checking", result.accounts.single().name)
        assertEquals(1, result.transactions.size)
        assertEquals("Weekly shop", result.transactions.single().memo)
        assertEquals(2, result.transactions.single().splits.size)
        assertEquals("fruit", result.transactions.single().splits[1].memo)
    }
}
