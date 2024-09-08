package org.sol4k.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TokenAccountsByOwnerParams {
    @SerialName("programId")
    PROGRAM_ID,

    @SerialName("mint")
    MINT
}