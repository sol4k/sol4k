package org.sol4k

data class AccountMeta(
    val publicKey: PublicKey,
    val writable: Boolean,
    val signer: Boolean,
)
