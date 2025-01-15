package org.sol4k.instruction

import org.sol4k.AccountMeta
import org.sol4k.Constants.ASSOCIATED_TOKEN_PROGRAM_ID
import org.sol4k.Constants.SYSTEM_PROGRAM
import org.sol4k.Constants.SYSVAR_RENT_ADDRESS
import org.sol4k.Constants.TOKEN_2022_PROGRAM_ID
import org.sol4k.PublicKey

class CreateAssociatedToken2022AccountInstruction (
    payer: PublicKey,
    associatedToken: PublicKey,
    owner: PublicKey,
    mint: PublicKey,
) : Instruction {

    override val data: ByteArray = byteArrayOf(0)

    override val keys: List<AccountMeta> = listOf(
        AccountMeta.signerAndWritable(payer),
        AccountMeta.writable(associatedToken),
        AccountMeta(owner),
        AccountMeta(mint),
        AccountMeta(SYSTEM_PROGRAM),
        AccountMeta(TOKEN_2022_PROGRAM_ID),
        AccountMeta(SYSVAR_RENT_ADDRESS),
    )

    override val programId: PublicKey = ASSOCIATED_TOKEN_PROGRAM_ID
}
