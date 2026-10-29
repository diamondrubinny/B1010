package net.rubinny.willowbrook.audio

import net.rubinny.willowbrook.R

/** Gentle sounds. There is deliberately no "wrong answer" sound. */
enum class SoundCue(val resId: Int) {
    FOUND(R.raw.sfx_found),
    COMPLETE(R.raw.sfx_complete),
    REWARD(R.raw.sfx_reward),
    FLIP(R.raw.sfx_flip),
    NOTE_STAR(R.raw.note_star),
    NOTE_MOON(R.raw.note_moon),
    NOTE_LEAF(R.raw.note_leaf),
    NOTE_FLOWER(R.raw.note_flower),
    ;

    companion object {
        fun noteFor(symbolId: String): SoundCue? = when (symbolId) {
            "star" -> NOTE_STAR
            "moon" -> NOTE_MOON
            "leaf" -> NOTE_LEAF
            "flower" -> NOTE_FLOWER
            else -> null
        }
    }
}
