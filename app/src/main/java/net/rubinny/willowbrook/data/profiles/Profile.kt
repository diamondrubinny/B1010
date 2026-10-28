package net.rubinny.willowbrook.data.profiles

import kotlinx.serialization.Serializable

/**
 * A local player profile. Data minimisation: a nickname and an avatar colour only —
 * no real name, age, photo or diagnosis is ever collected.
 */
@Serializable
data class Profile(
    val id: String,
    val nickname: String,
    val avatar: Avatar,
    val createdAt: Long,
)

/** Avatars are calm colours with the player's initial, named so screen readers can describe them. */
@Serializable
enum class Avatar(val label: String, val argb: Long) {
    ROSE("Rose", 0xFFE8A0A8),
    SKY("Sky", 0xFF8FC1E3),
    SAGE("Sage", 0xFFA8C8A0),
    AMBER("Amber", 0xFFF2C14E),
    LILAC("Lilac", 0xFFC3A8E0),
    SAND("Sand", 0xFFE0CDA8),
}

object ProfileRules {
    const val MAX_PROFILES = 4
    const val NICKNAME_MAX_LENGTH = 16

    sealed interface NicknameResult {
        data class Valid(val nickname: String) : NicknameResult
        data class Invalid(val message: String) : NicknameResult
    }

    /** Trims, collapses whitespace and allows letters, digits, spaces and a few name punctuation marks. */
    fun validateNickname(raw: String): NicknameResult {
        val cleaned = raw.trim().replace(Regex("\\s+"), " ")
        return when {
            cleaned.isEmpty() -> NicknameResult.Invalid("Please type a name.")
            cleaned.length > NICKNAME_MAX_LENGTH ->
                NicknameResult.Invalid("Please use $NICKNAME_MAX_LENGTH letters or fewer.")
            !cleaned.all { it.isLetterOrDigit() || it == ' ' || it == '\'' || it == '-' || it == '.' } ->
                NicknameResult.Invalid("Please use letters and numbers only.")
            else -> NicknameResult.Valid(cleaned)
        }
    }
}
