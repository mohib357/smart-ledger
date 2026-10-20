package com.example.util

import java.security.MessageDigest

object PinHelper {
    fun hash(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verify(pin: String, storedHash: String): Boolean = hash(pin) == storedHash
}
