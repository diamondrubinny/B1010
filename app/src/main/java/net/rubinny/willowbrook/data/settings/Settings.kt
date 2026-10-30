package net.rubinny.willowbrook.data.settings

/** How hard puzzles are. AUTO adapts silently to how many hints the player uses. */
enum class Difficulty { AUTO, GENTLE, STANDARD }

/**
 * Caregiver-controlled settings. All values are stored on the device only.
 * Ranges are enforced by [SettingsLimits] so a corrupted file can never produce an unusable UI.
 */
data class Settings(
    val textScale: Float = SettingsLimits.TEXT_SCALE_DEFAULT,
    val speechRate: Float = SettingsLimits.SPEECH_RATE_DEFAULT,
    val narrationEnabled: Boolean = true,
    val captionsEnabled: Boolean = true,
    val difficulty: Difficulty = Difficulty.AUTO,
    val hintDelaySec: Int = SettingsLimits.HINT_DELAY_DEFAULT,
    val reduceMotion: Boolean = false,
    val highContrast: Boolean = false,
    /** Volume of the gentle sound effects (bells). Field name kept for stored-data compatibility. */
    val musicVolume: Float = SettingsLimits.MUSIC_VOLUME_DEFAULT,
    /** Volume of the background music; 0 = off. */
    val backgroundMusicVolume: Float = SettingsLimits.BACKGROUND_MUSIC_DEFAULT,
    /** Volume of the soft scene sounds (fire, birds, breeze); 0 = off. */
    val ambienceVolume: Float = SettingsLimits.AMBIENCE_DEFAULT,
    val lastProfileId: String? = null,
)

object SettingsLimits {
    const val TEXT_SCALE_MIN = 1.0f
    const val TEXT_SCALE_MAX = 1.6f
    const val TEXT_SCALE_STEP = 0.2f
    const val TEXT_SCALE_DEFAULT = 1.0f

    const val SPEECH_RATE_MIN = 0.6f
    const val SPEECH_RATE_MAX = 1.2f
    const val SPEECH_RATE_STEP = 0.1f
    const val SPEECH_RATE_DEFAULT = 0.85f

    const val HINT_DELAY_MIN = 10
    const val HINT_DELAY_MAX = 60
    const val HINT_DELAY_STEP = 10
    const val HINT_DELAY_DEFAULT = 20

    const val MUSIC_VOLUME_MIN = 0f
    const val MUSIC_VOLUME_MAX = 1f
    const val MUSIC_VOLUME_STEP = 0.1f
    const val MUSIC_VOLUME_DEFAULT = 0.3f

    const val BACKGROUND_MUSIC_DEFAULT = 0.4f

    /** The ambience files are 10 dB quieter than the music; at 0.6 they sit about 6 dB under it. */
    const val AMBIENCE_DEFAULT = 0.6f
}

/** Clamps every numeric field into its allowed range; values are rounded to avoid float drift (1.2000001 -> 1.2). */
fun Settings.sanitized(): Settings = copy(
    textScale = textScale.roundTo2().coerceIn(SettingsLimits.TEXT_SCALE_MIN, SettingsLimits.TEXT_SCALE_MAX),
    speechRate = speechRate.roundTo2().coerceIn(SettingsLimits.SPEECH_RATE_MIN, SettingsLimits.SPEECH_RATE_MAX),
    hintDelaySec = hintDelaySec.coerceIn(SettingsLimits.HINT_DELAY_MIN, SettingsLimits.HINT_DELAY_MAX),
    musicVolume = musicVolume.roundTo2().coerceIn(SettingsLimits.MUSIC_VOLUME_MIN, SettingsLimits.MUSIC_VOLUME_MAX),
    backgroundMusicVolume = backgroundMusicVolume.roundTo2().coerceIn(SettingsLimits.MUSIC_VOLUME_MIN, SettingsLimits.MUSIC_VOLUME_MAX),
    ambienceVolume = ambienceVolume.roundTo2().coerceIn(SettingsLimits.MUSIC_VOLUME_MIN, SettingsLimits.MUSIC_VOLUME_MAX),
)

private fun Float.roundTo2(): Float = Math.round(this * 100f) / 100f
