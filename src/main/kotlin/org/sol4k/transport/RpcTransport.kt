package org.sol4k.transport

interface RpcTransport {

    fun post(
        url: String,
        body: String,
        headers: Map<String, String> = emptyMap(),
    ): String
}
