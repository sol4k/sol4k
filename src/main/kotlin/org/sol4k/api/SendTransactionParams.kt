package org.sol4k.api

import kotlinx.serialization.Serializable

@Serializable
data class SendTransactionParams(
    val skipPreflight: Boolean? = null,
    val preflightCommitment: Commitment? = null,
    val maxRetries: Int? = null,
    val minContextSlot: Int? = null,
)
