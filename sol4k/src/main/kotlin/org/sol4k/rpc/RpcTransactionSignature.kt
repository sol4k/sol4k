package org.sol4k.rpc

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal data class RpcTransactionSignature(
    val signature: String,
    val slot: Long,
    val err: JsonElement? = null,
    val memo: String? = null,
    val blockTime: Long? = null,
    val confirmationStatus: String? = null,
)
