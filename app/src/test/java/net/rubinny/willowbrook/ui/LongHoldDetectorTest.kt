package net.rubinny.willowbrook.ui

import android.view.KeyEvent
import net.rubinny.willowbrook.ui.components.LongHoldDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LongHoldDetectorTest {
    private var opened = 0
    private val d = LongHoldDetector()

    /** Simulates a remote key held for [millis] (repeats every 100 ms); returns whether the release was swallowed. */
    private fun hold(key: Int, millis: Long): Boolean {
        val consumed = mutableListOf<Boolean>()
        consumed += d.onKey(key, KeyEvent.ACTION_DOWN, 0, 0, 0) { opened++ }
        var t = 100L
        var r = 1
        while (t <= millis) {
            consumed += d.onKey(key, KeyEvent.ACTION_DOWN, r++, 0, t) { opened++ }
            t += 100
        }
        return d.onKey(key, KeyEvent.ACTION_UP, 0, 0, millis) { opened++ }
    }

    @Test fun `a short press is never swallowed and opens nothing`() {
        assertFalse(hold(KeyEvent.KEYCODE_MENU, 0))
        assertFalse(hold(KeyEvent.KEYCODE_DPAD_CENTER, 300))
        assertEquals(0, opened)
    }

    @Test fun `menu held 3 seconds opens once, and its release is swallowed`() {
        assertFalse(hold(KeyEvent.KEYCODE_MENU, 2_900))
        assertEquals(0, opened)
        assertTrue(hold(KeyEvent.KEYCODE_MENU, 6_000))
        assertEquals(1, opened)
    }

    @Test fun `ok must be held 5 seconds, so a slow press still just presses the button`() {
        assertFalse(hold(KeyEvent.KEYCODE_DPAD_CENTER, 3_000))
        assertEquals(0, opened)
        assertTrue(hold(KeyEvent.KEYCODE_DPAD_CENTER, 5_000))
        assertEquals(1, opened)
    }

    @Test fun `other keys are ignored`() {
        assertFalse(hold(KeyEvent.KEYCODE_DPAD_LEFT, 10_000))
        assertEquals(0, opened)
    }
}
