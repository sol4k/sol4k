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
        val stream: InputStream = if (responseCode < HttpURLConnection.HTTP_BAD_REQUEST) {
            connection.inputStream
        } else {
            connection.errorStream
                ?: throw IOException("RPC node returned HTTP $responseCode with an empty body")
        }

        val responseBody = stream.use {
            BufferedReader(InputStreamReader(it)).use { reader ->
                reader.readText()
            }
        }

        connection.disconnect()
        return responseBody
    }

    companion object {
        const val DEFAULT_CONNECT_TIMEOUT_MILLIS = 15_000
        const val DEFAULT_READ_TIMEOUT_MILLIS = 30_000
    }
}
