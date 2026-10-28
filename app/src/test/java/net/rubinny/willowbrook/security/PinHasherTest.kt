package net.rubinny.willowbrook.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PinHasherTest {

    @Test fun `correct pin verifies`() {
        val stored = PinHasher.hash("2468")
        assertTrue(PinHasher.verify("2468", stored))
    }

    @Test fun `wrong pin is rejected`() {
        val stored = PinHasher.hash("2468")
        assertFalse(PinHasher.verify("2469", stored))
        assertFalse(PinHasher.verify("0000", stored))
    }

    @Test fun `same pin hashes differently because of random salt`() {
        assertNotEquals(PinHasher.hash("1234"), PinHasher.hash("1234"))
    }

    @Test fun `stored value never contains the pin itself`() {
        val stored = PinHasher.hash("9753")
        assertFalse(stored.contains("9753"))
        assertTrue(stored.startsWith("pbkdf2$"))
    }

    @Test fun `malformed stored values are rejected without throwing`() {
        listOf("", "pbkdf2", "pbkdf2\$x\$aa\$bb", "md5\$1\$aa\$bb", "pbkdf2\$1000\$zz\$bb", "pbkdf2\$1000\$abc\$bb")
            .forEach { assertFalse("should reject '$it'", PinHasher.verify("1234", it)) }
    }

    @Test fun `pin format validation`() {
        assertTrue(PinHasher.isValidFormat("0123"))
        assertFalse(PinHasher.isValidFormat("123"))
        assertFalse(PinHasher.isValidFormat("12345"))
        assertFalse(PinHasher.isValidFormat("12a4"))
        assertFalse(PinHasher.isValidFormat("١٢٣٤")) // non-ASCII digits are not accepted
    }

    @Test(expected = IllegalArgumentException::class)
    fun `hashing an invalid pin fails fast`() {
        PinHasher.hash("12")
    }

    @Test fun `lockout starts on the fifth failure`() {
        var failures = 0
        var lockedUntil = 0L
        repeat(4) {
            val r = PinLockout.afterFailure(failures, now = 1_000L)
            failures = r.first; lockedUntil = r.second
            assertEquals(0L, lockedUntil)
        }
        val fifth = PinLockout.afterFailure(failures, now = 1_000L)
        assertEquals(5, fifth.first)
        assertEquals(1_000L + PinLockout.LOCK_MILLIS, fifth.second)
    }

    @Test fun `remaining lock time never negative`() {
        assertEquals(0L, PinLockout.remainingLockMillis(lockedUntil = 100L, now = 500L))
        assertEquals(400L, PinLockout.remainingLockMillis(lockedUntil = 900L, now = 500L))
    }
}
