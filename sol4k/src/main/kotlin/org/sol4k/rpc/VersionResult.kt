package org.sol4k.rpc

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class VersionResult(
    @SerialName("solana-core") val solanaCore: String,
    @SerialName("feature-set") val featureSet: Long,
)
