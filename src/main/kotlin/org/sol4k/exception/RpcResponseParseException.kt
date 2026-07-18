package org.sol4k.exception

import java.lang.RuntimeException

data class RpcResponseParseException(
    val rawResponse: String,
) : RuntimeException("Unable to parse the RPC node response") {

    constructor(rawResponse: String, cause: Throwable) : this(rawResponse) {
        initCause(cause)
    }
}
