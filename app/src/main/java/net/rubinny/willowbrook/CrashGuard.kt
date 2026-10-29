package net.rubinny.willowbrook

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.content.edit

/**
 * Remembers that the app stopped unexpectedly, so the next start can say so calmly instead of
 * leaving a confused player at a blank screen. Only a yes/no flag is stored: no stack trace,
 * no personal data. The crash is still passed on to Android afterwards.
 */
object CrashGuard {
    private const val PREFS = "crash_guard"
    private const val KEY = "stopped_unexpectedly"

    @SuppressLint("ApplySharedPref") // commit() on purpose: the process is about to end, apply() could be lost.
    fun install(context: Context) {
        val app = context.applicationContext
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            try {
                // commit(), not apply(): the process is about to end.
                app.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit(commit = true) { putBoolean(KEY, true) }
            } catch (_: Exception) {
            }
            previous?.uncaughtException(thread, error)
        }
    }

    /** True once after an unexpected stop; reading it clears it. */
    fun consumeStoppedUnexpectedly(context: Context): Boolean {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val value = prefs.getBoolean(KEY, false)
        if (value) prefs.edit { remove(KEY) }
        return value
    }
}
