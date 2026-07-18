package org.sol4k.transport

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class HttpUrlConnectionTransport @JvmOverloads constructor(
    private val connectTimeoutMillis: Int = DEFAULT_CONNECT_TIMEOUT_MILLIS,
    private val readTimeoutMillis: Int = DEFAULT_READ_TIMEOUT_MILLIS,
) : RpcTransport {

    init {
        require(connectTimeoutMillis >= 0) { "connectTimeoutMillis must not be negative" }
        require(readTimeoutMillis >= 0) { "readTimeoutMillis must not be negative" }
    }

    override fun post(
        url: String,
        body: String,
        headers: Map<String, String>,
    ): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.connectTimeout = connectTimeoutMillis
        connection.readTimeout = readTimeoutMillis

        connection.setRequestProperty("Content-Type", "application/json")
        headers.forEach { (key, value) ->
            connection.setRequestProperty(key, value)
        }

        connection.doOutput = true

        connection.outputStream.use {
            it.write(body.toByteArray())
        }

        val responseCode = connection.responseCode
        if (responseCode < HttpURLConnection.HTTP_BAD_REQUEST) {
            val responseBody = readBody(connection.inputStream)
            connection.disconnect()
            return responseBody
        }

        val errorBody = connection.errorStream?.let { readBody(it) } ?: ""
        connection.disconnect()
        // pass JSON bodies through so the caller can surface the RPC error;
        // for anything else (HTML error pages, empty bodies) the HTTP status
        // is the only meaningful signal and must not be lost
        if (errorBody.trimStart().startsWith("{")) {
            return errorBody
        }
        throw IOException(
            if (errorBody.isEmpty()) {
                "RPC node returned HTTP $responseCode with an empty body"
            } else {
                "RPC node returned HTTP $responseCode: ${errorBody.take(MAX_ERROR_BODY_IN_MESSAGE)}"
            },
        )
    }

    private fun readBody(stream: InputStream): String = stream.use {
        BufferedReader(InputStreamReader(it)).use { reader ->
            reader.readText()
        }
    }

    companion object {
        const val DEFAULT_CONNECT_TIMEOUT_MILLIS = 15_000
        const val DEFAULT_READ_TIMEOUT_MILLIS = 30_000
        private const val MAX_ERROR_BODY_IN_MESSAGE = 300
    }
}
