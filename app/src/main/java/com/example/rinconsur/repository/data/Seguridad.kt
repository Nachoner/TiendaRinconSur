package com.example.rinconsur.repository.data

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

// Herramienta técnica para guardar claves. La lógica del login vive en el ViewModel.
object Seguridad {

    fun nuevaSal(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun hash(clave: String, sal: String): String {
        val spec = PBEKeySpec(clave.toCharArray(), sal.toByteArray(), 10_000, 256)
        val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(spec)
            .encoded
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // Comparación que no filtra información por tiempo de respuesta
    fun iguales(a: String, b: String): Boolean =
        MessageDigest.isEqual(a.toByteArray(), b.toByteArray())
}