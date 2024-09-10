package org.sol4k.rpc

import kotlinx.serialization.Serializable

@Serializable
data class ParsedAccountData(
    val program: String,
    val parsed: ParsedAccountDataParsed,
    val space: Int,
)
