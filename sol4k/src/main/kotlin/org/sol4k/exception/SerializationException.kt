package org.sol4k.exception

import java.lang.RuntimeException

data class SerializationException(
    override val message: String,
) : RuntimeException(message)
