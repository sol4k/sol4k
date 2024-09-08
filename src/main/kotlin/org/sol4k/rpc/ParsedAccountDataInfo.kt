package org.sol4k.rpc

import kotlinx.serialization.Serializable

@Serializable
data class ParsedAccountDataInfo (
    val isNative: Boolean,
    val mint: String,
    val owner: String,
    val state: String,
    val tokenAmount: TokenAmount
)