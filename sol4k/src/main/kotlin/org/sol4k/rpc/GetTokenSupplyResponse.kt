package org.sol4k.rpc

import kotlinx.serialization.Serializable

@Serializable
internal data class GetTokenSupplyResponse(
    val value: TokenAmount,
)

@Serializable
data class TokenAmount(
    val amount: String,
    val decimals: Int,
    val uiAmountString: String,
)
