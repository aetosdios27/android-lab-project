package edu.muj.studentportal.data

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Local demo credential verifier; this is not server authentication. */
object PasswordVerifier {
    fun salt() = ByteArray(16).also { SecureRandom().nextBytes(it) }.joinToString("") { "%02x".format(it) }
    fun hash(password: String, salt: String): String {
        val spec = PBEKeySpec(password.toCharArray(), salt.toByteArray(Charsets.UTF_8), 60000, 256)
        return try { SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).encoded
            .joinToString("") { "%02x".format(it) } } finally { spec.clearPassword() }
    }
    fun matches(password: String, user: UserEntity) = MessageDigest.isEqual(
        hash(password, user.salt).toByteArray(Charsets.UTF_8), user.verifier.toByteArray(Charsets.UTF_8))
}
