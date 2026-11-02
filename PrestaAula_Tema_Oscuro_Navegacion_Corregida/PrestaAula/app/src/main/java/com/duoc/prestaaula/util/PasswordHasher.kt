package com.duoc.prestaaula.util

import java.security.MessageDigest

/** Las contraseñas nunca se guardan en texto plano. */
object PasswordHasher {
    fun hash(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest("prestaaula::$password".toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
