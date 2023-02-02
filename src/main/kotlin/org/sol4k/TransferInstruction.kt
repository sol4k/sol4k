package org.sol4k

import org.sol4k.bn.Binary

class TransferInstruction(
    private val from: PublicKey,
    private val to: PublicKey,
    private val lamports: Long,
) : Instruction {
    private val systemProgram = PublicKey("11111111111111111111111111111111")

    override val data: ByteArray
        get() {
            val instruction = Binary.uint32(2L)
            val lamports = Binary.int64(this.lamports)
            return instruction + lamports
        }

    override val keys: List<AccountMeta>
        get() = listOf(
            AccountMeta(from, writable = true, signer = true),
            AccountMeta(to, writable = true, signer = false),
        )

    override val programId: PublicKey
        get() = systemProgram
}
