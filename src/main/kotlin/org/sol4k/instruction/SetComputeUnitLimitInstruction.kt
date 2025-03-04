package org.sol4k.instruction

import org.sol4k.AccountMeta
import org.sol4k.Constants.COMPUTE_BUDGET_PROGRAM_ID
import org.sol4k.PublicKey

data class SetComputeUnitLimitInstruction(
    val units: Long,
) : Instruction {
    override val data: ByteArray = ByteArray(9).apply {
        this[0] = 2
        for (i in 0 until 8) {
            this[i + 1] = ((units shr (i * 8)) and 0xFF).toByte()
        }
    }

    override val keys: List<AccountMeta> = emptyList()

    override val programId: PublicKey = COMPUTE_BUDGET_PROGRAM_ID
}
