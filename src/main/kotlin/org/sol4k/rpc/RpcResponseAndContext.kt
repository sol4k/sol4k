package org.sol4k.rpc

import kotlinx.serialization.Serializable

@Serializable
data class RpcResponseAndContext<T>(
    val context: Context,
    val value: List<T>? = null
)

@Serializable
data class Context(val slot: Int)