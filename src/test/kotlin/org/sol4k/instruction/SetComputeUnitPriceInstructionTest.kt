package org.sol4k.instruction

import org.junit.jupiter.api.Test
import org.sol4k.Constants.COMPUTE_BUDGET_PROGRAM_ID
import kotlin.test.assertEquals

class SetComputeUnitPriceInstructionTest {

    @Test
    fun shouldCreateInstructionWithCorrectDataForSmallMicrolamportsValue() {
        val instruction = SetComputeUnitPriceInstruction(1000L)

        assertEquals(3.toByte(), instruction.data[0])
        assertEquals(0xE8.toByte(), instruction.data[1])
        assertEquals(0x03.toByte(), instruction.data[2])

        for (i in 3..8) {
            assertEquals(0.toByte(), instruction.data[i])
        }

        assertEquals(COMPUTE_BUDGET_PROGRAM_ID, instruction.programId)
        assert(instruction.keys.isEmpty())
    }

    @Test
    fun shouldCreateInstructionWithCorrectDataForLargeMicrolamportsValue() {
        val instruction = SetComputeUnitPriceInstruction(1_000_000L)

        assertEquals(0x40.toByte(), instruction.data[1])
        assertEquals(0x42.toByte(), instruction.data[2])
        assertEquals(0x0F.toByte(), instruction.data[3])
    }

    @Test
    fun shouldHandleMaximumLongValueForMicrolamports() {
        val instruction = SetComputeUnitPriceInstruction(Long.MAX_VALUE)

        assertEquals(3.toByte(), instruction.data[0])
        assertEquals(0x7F.toByte(), instruction.data[8])
    }

    @Test
    fun shouldCreateDifferentInstructionsForDifferentValues() {
        val smallInstruction = SetComputeUnitPriceInstruction(1000L)
        val largeInstruction = SetComputeUnitPriceInstruction(1_000_000L)

        assert(!smallInstruction.data.contentEquals(largeInstruction.data))
    }
}
