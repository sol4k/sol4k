package org.sol4k.rpc

import kotlinx.serialization.Serializable
import org.sol4k.PublicKey

@Serializable
data class GetProgramAccountsResponse<T>(
    val account: T,
    @Serializable(with = PublicKeySerializer::class)
    val pubkey: PublicKey
)
