package org.sol4k.instruction

import org.sol4k.Constants.TOKEN_2022_PROGRAM_ID
import org.sol4k.PublicKey

class Token2022TransferInstruction @JvmOverloads constructor(
    from: PublicKey,
    to: PublicKey,
    mint: PublicKey,
    owner: PublicKey,
    amount: Long,
    decimals: Int,
    signers: List<PublicKey> = emptyList(),
) : TokenTransferInstruction(from, to, mint, owner, amount, decimals, signers) {
    override val programId: PublicKey = TOKEN_2022_PROGRAM_ID
}
