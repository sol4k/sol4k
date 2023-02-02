package org.sol4k.rpc

import kotlinx.serialization.Serializable

@Serializable
data class BlockhashResponse(
    val context: BlockhashContext,
    val value: BlockhashValue,
)

@Serializable
data class BlockhashContext(val slot: Long, val apiVersion: String)

@Serializable
data class BlockhashValue(
    val blockhash: String,
    val lastValidBlockHeight: Long,
)
