package org.sol4k.api

data class TransactionSignature(
    val signature: String,
    val slot: Long,
    val isError: Boolean,
    val memo: String? = null,
    val blockTime: Long? = null,
    val confirmationStatus: String? = null,
)
