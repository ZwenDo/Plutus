package fr.uge.plutus.backend.serialization

import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec


private val salt = "r9+?w_\"c}HAUx)q}^o#dPk2[f=~\\:<`9"
    .toByteArray()
    .slice(0..15)
    .toByteArray()

private val ivParam = IvParameterSpec(salt)

private val cipherInstance
    get() = Cipher.getInstance("AES/CBC/PKCS5Padding")

private val secKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")

fun createSecKeyFromPassword(password: String): SecretKeySpec {
    val pbeSpec = PBEKeySpec(password.toCharArray(), salt, 800_000, 256)
    val pbeSecretKey = secKeyFactory.generateSecret(pbeSpec)
    return SecretKeySpec(pbeSecretKey.encoded, "AES")
}

private fun encryptCipher(password: String) = cipherInstance.apply {
    init(Cipher.ENCRYPT_MODE, createSecKeyFromPassword(password), ivParam)
}

private fun decryptCipher(password: String) = cipherInstance.apply {
    init(Cipher.DECRYPT_MODE, createSecKeyFromPassword(password), ivParam)
}

fun encrypt(password: String, content: ByteArray): ByteArray =
    encryptCipher(password).doFinal(content)

fun decrypt(password: String, byteArray: ByteArray): ByteArray =
    decryptCipher(password).doFinal(byteArray)
