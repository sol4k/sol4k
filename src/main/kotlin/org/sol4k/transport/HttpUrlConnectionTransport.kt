package org.sol4k.transport

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class HttpUrlConnectionTransport : RpcTransport {

    override fun post(
        url: String,
        body: String,
        headers: Map<String, String>,
    ): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "POST"

        connection.setRequestProperty("Content-Type", "application/json")
        headers.forEach { (key, value) ->
            connection.setRequestProperty(key, value)
        }

        connection.doOutput = true

        connection.outputStream.use {
            it.write(body.toByteArray())
        }

        val responseBody = connection.inputStream.use {
            BufferedReader(InputStreamReader(it)).use { reader ->
                reader.readText()
            }
        }

        connection.disconnect()
        return responseBody
    }
}
