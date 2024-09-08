package org.sol4k.rpc

import kotlinx.serialization.Serializable

@Serializable
data class ParsedAccountDataParsed(
    val info: ParsedAccountDataInfo,
    val type: String,
)
