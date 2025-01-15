package org.sol4k.instruction

import org.sol4k.Constants.TOKEN_PROGRAM_ID
import org.sol4k.PublicKey

class SplTransferInstruction @JvmOverloads constructor(
    from: PublicKey,
    to: PublicKey,
    mint: PublicKey,
    owner: PublicKey,
    amount: Long,
    decimals: Int,
    signers: List<PublicKey> = emptyList(),
) : TokenTransferInstruction(from, to, mint, owner, amount, decimals, signers) {
    override val programId: PublicKey = TOKEN_PROGRAM_ID
}
