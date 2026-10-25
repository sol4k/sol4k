package org.sol4k.rpc

import kotlinx.serialization.Serializable

@Serializable
internal data class FeeForMessageResponse(
    val context: FeeContext,
    val value: Long?,
)

@Serializable
internal data class FeeContext(val slot: Long)
