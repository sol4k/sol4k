package org.sol4k

import okio.ByteString.Companion.decodeHex
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ComputeBudgetTest {

    @Test
    fun testDecodeComputeUnitLimit() {
        var data = "022e720200"
        var unitLimit = decodeComputeUnitLimit(data.decodeHex().toByteArray())
        assertEquals(unitLimit, 160302)

        data = "02c0d40100"
        unitLimit = decodeComputeUnitLimit(data.decodeHex().toByteArray())
        assertEquals(unitLimit, 120000)
    }

    @Test
    fun testDecodeComputeUnitPrice() {
        var data = "03ab05000000000000"
        var unitPrice = decodeComputeUnitPrice(data.decodeHex().toByteArray())
        assertEquals(unitPrice, 1451)

        data = "037427000000000000"
        unitPrice = decodeComputeUnitPrice(data.decodeHex().toByteArray())
        assertEquals(unitPrice, 10100)
    }

    @Test
    fun testComputeBudget() {
        var data = listOf("037427000000000000", "02c0d40100").map { it.decodeHex().toByteArray() }
        var fee = computeBudget(data)
        assertEquals(fee.toPlainString(), "0.000001212")

        data = listOf("02c0d40100", "037427000000000000").map { it.decodeHex().toByteArray() }
        fee = computeBudget(data)
        assertEquals(fee.toPlainString(), "0.000001212")

        data = listOf("022e720200", "03ab05000000000000").map { it.decodeHex().toByteArray() }
        fee = computeBudget(data)
        assertEquals(fee.toPlainString(), "0.000000233")

        data = listOf("03ab05000000000000", "022e720200").map { it.decodeHex().toByteArray() }
        fee = computeBudget(data)
        assertEquals(fee.toPlainString(), "0.000000233")
    }
}
