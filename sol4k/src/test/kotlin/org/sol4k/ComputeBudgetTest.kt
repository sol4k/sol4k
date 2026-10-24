package org.sol4k

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalStdlibApi::class)
class ComputeBudgetTest {

    @Test
    fun testDecodeComputeUnitLimit() {
        var data = "022e720200"
        var unitLimit = decodeComputeUnitLimit(data.hexToByteArray())
        assertEquals(unitLimit, 160302)

        data = "02c0d40100"
        unitLimit = decodeComputeUnitLimit(data.hexToByteArray())
        assertEquals(unitLimit, 120000)
    }

    @Test
    fun testDecodeComputeUnitPrice() {
        var data = "03ab05000000000000"
        var unitPrice = decodeComputeUnitPrice(data.hexToByteArray())
        assertEquals(unitPrice, 1451)

        data = "037427000000000000"
        unitPrice = decodeComputeUnitPrice(data.hexToByteArray())
        assertEquals(unitPrice, 10100)
    }

    @Test
    fun testComputeBudget() {
        var data = listOf("037427000000000000", "02c0d40100").map { it.hexToByteArray() }
        var fee = computeBudget(data)
        assertEquals(fee.toPlainString(), "0.000001212")

        data = listOf("02c0d40100", "037427000000000000").map { it.hexToByteArray() }
        fee = computeBudget(data)
        assertEquals(fee.toPlainString(), "0.000001212")

        data = listOf("022e720200", "03ab05000000000000").map { it.hexToByteArray() }
        fee = computeBudget(data)
        assertEquals(fee.toPlainString(), "0.000000233")

        data = listOf("03ab05000000000000", "022e720200").map { it.hexToByteArray() }
        fee = computeBudget(data)
        assertEquals(fee.toPlainString(), "0.000000233")
    }
}
