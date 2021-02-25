package xyz.fi5t.biometric


data class EncryptedEntity(
    val ciphertext: ByteArray,
    val iv: ByteArray
)
