package org.sol4k

data class SignInInput(
    val domain: String,
    val address: String,
    val statement: String? = null,
    val uri: String? = null,
    val version: String? = null,
    val chainId: String? = null,
    val nonce: String? = null,
    val issuedAt: String? = null,
    val expirationTime: String? = null,
    val notBefore: String? = null,
    val requestId: String? = null,
    val resources: List<String>? = null,
) {
    fun toMessage(): String {
        val s = StringBuilder()
        s.append("$domain wants you to sign in with your Solana account:\n")
            .append(address)
        if (!statement.isNullOrBlank()) {
            s.append("\n\n$statement")
        }

        val fields = mutableListOf<String>()
        if (!uri.isNullOrBlank()) {
            fields.add("URI: $uri")
        }
        if (!version.isNullOrBlank()) {
            fields.add("Version: $version")
        }
        if (!chainId.isNullOrBlank()) {
            fields.add("Chain ID: $chainId")
        }
        if (!nonce.isNullOrBlank()) {
            fields.add("Nonce: $nonce")
        }
        if (!issuedAt.isNullOrBlank()) {
            fields.add("Issued At: $issuedAt")
        }
        if (!expirationTime.isNullOrBlank()) {
            fields.add("Expiration Time: $expirationTime")
        }
        if (!notBefore.isNullOrBlank()) {
            fields.add("Not Before: $notBefore")
        }
        if (!requestId.isNullOrBlank()) {
            fields.add("Request ID: $requestId")
        }
        if (!resources.isNullOrEmpty()) {
            fields.add("Resources:")
            for (r in resources) {
                fields.add("- $r")
            }
        }
        if (fields.isNotEmpty()) {
            s.append("\n\n${fields.joinToString("\n")}")
        }

        return s.toString()
    }
}

data class SignInOutput(
    val account: SignInAccount,
    val signature: String,
    val signedMessage: String,
    val signatureType: String = "ed25519",
)

data class SignInAccount(
    val publicKey: String,
)

class SignInException(message: String) : IllegalArgumentException(message) {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
