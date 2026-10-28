package net.rubinny.willowbrook.security

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Hashes the caregiver PIN with PBKDF2 + a random salt.
 *
 * Threat model note: a 4-digit PIN only stops the player from changing settings by accident.
 * It is not strong protection against someone with physical access to the device.
 * Format: "pbkdf2$<iterations>$<salt-hex>$<hash-hex>" (hex: java.util.Base64 needs API 26, minSdk is 24).
 */
object PinHasher {
    const val PIN_LENGTH = 4
    private const val ITERATIONS = 50_000 // a 4-digit PIN has only 10^4 values; this just slows scripted guessing on cheap TV CPUs
    private const val KEY_BITS = 256
    private const val SALT_BYTES = 16
    private const val ALGORITHM = "PBKDF2WithHmacSHA1" // available on every Android API level we support

    fun isValidFormat(pin: String): Boolean = pin.length == PIN_LENGTH && pin.all { it in '0'..'9' }

    fun hash(pin: String, random: SecureRandom = SecureRandom()): String {
        require(isValidFormat(pin)) { "PIN must be $PIN_LENGTH digits" }
        val salt = ByteArray(SALT_BYTES).also(random::nextBytes)
        val derived = derive(pin, salt, ITERATIONS)
        return "pbkdf2\$$ITERATIONS\$${salt.toHex()}\$${derived.toHex()}"
    }

    fun verify(pin: String, stored: String): Boolean {
        if (!isValidFormat(pin)) return false
        val parts = stored.split('$')
        if (parts.size != 4 || parts[0] != "pbkdf2") return false
        val iterations = parts[1].toIntOrNull()?.takeIf { it > 0 } ?: return false
        val salt = parts[2].hexToBytesOrNull() ?: return false
        val expected = parts[3].hexToBytesOrNull() ?: return false
        return MessageDigest.isEqual(derive(pin, salt, iterations), expected)
    }

    private fun derive(pin: String, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, iterations, KEY_BITS)
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }
}

private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

private fun String.hexToBytesOrNull(): ByteArray? {
    if (isEmpty() || length % 2 != 0) return null
    return ByteArray(length / 2) { i ->
        substring(i * 2, i * 2 + 2).toIntOrNull(16)?.toByte() ?: return null
    }
}

/** Lockout policy after repeated wrong PINs. Pure function so it is easy to test. */
object PinLockout {
    const val FREE_ATTEMPTS = 5
    const val LOCK_MILLIS = 30_000L

    fun afterFailure(previousFailures: Int, now: Long): Pair<Int, Long> {
        val failures = previousFailures + 1
        val lockedUntil = if (failures >= FREE_ATTEMPTS) now + LOCK_MILLIS else 0L
        return failures to lockedUntil
    }

    fun remainingLockMillis(lockedUntil: Long, now: Long): Long = (lockedUntil - now).coerceAtLeast(0L)
}
