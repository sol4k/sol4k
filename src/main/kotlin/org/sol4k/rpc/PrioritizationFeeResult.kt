package org.sol4k.rpc

import kotlinx.serialization.Serializable

@Serializable
internal data class PrioritizationFeeResult(
    val slot: Long,
    val prioritizationFee: Long,
)
