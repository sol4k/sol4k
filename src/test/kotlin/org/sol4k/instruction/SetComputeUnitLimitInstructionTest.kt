package org.sol4k.instruction

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import org.sol4k.Constants.COMPUTE_BUDGET_PROGRAM_ID

class SetComputeUnitLimitInstructionTest {

    @Test
    fun shouldCreateInstructionWithCorrectDataForSmallUnitValue() {
        val instruction = SetComputeUnitLimitInstruction(1000L)

        assertEquals(2.toByte(), instruction.data[0])
        assertEquals(0xE8.toByte(), instruction.data[1])
        assertEquals(0x03.toByte(), instruction.data[2])

        for (i in 3..8) {
            assertEquals(0.toByte(), instruction.data[i])
        }

        assertEquals(COMPUTE_BUDGET_PROGRAM_ID, instruction.programId)
        assert(instruction.keys.isEmpty())
    }

    @Test
    fun shouldCreateInstructionWithCorrectDataForLargeUnitValue() {
        val instruction = SetComputeUnitLimitInstruction(1_000_000L)

        assertEquals(0x40.toByte(), instruction.data[1])
        assertEquals(0x42.toByte(), instruction.data[2])
        assertEquals(0x0F.toByte(), instruction.data[3])
    }

    @Test
    fun shouldHandleMaximumLongValue() {
        val instruction = SetComputeUnitLimitInstruction(Long.MAX_VALUE)

        assertEquals(2.toByte(), instruction.data[0])
        assertEquals(0x7F.toByte(), instruction.data[8])
    }
}
