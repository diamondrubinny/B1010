#!/usr/bin/env python3
"""Composes and renders the game's background music (original; no samples, no third-party licence).

Style: a soft music box over a warm pad and a gentle bass, slow tempo, major keys, no percussion,
no sudden changes. Each track is a seamless loop (the reverb tail is wrapped to the start).
Output: app/src/main/res/raw/music_<name>.ogg (mono Ogg Vorbis, about -26 LUFS: background level).

Usage: python3 tools/generate_music.py
"""
import os
import subprocess
import tempfile
import wave

import numpy as np

RATE = 32000
OUT = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "res", "raw")


def midi_hz(n: float) -> float:
    return 440.0 * 2 ** ((n - 69) / 12)


def envelope(n: int, attack: float, release: float) -> np.ndarray:
    """Raised-cosine attack and release: no clicks."""
    env = np.ones(n)
    a = min(int(attack * RATE), n // 2)
    r = min(int(release * RATE), n // 2)
    if a:
        env[:a] = 0.5 - 0.5 * np.cos(np.linspace(0, np.pi, a))
    if r:
        env[-r:] = 0.5 + 0.5 * np.cos(np.linspace(0, np.pi, r))
    return env


def music_box(freq: float, dur: float) -> np.ndarray:
    n = int(dur * RATE)
    t = np.arange(n) / RATE
    tone = (np.sin(2 * np.pi * freq * t) * np.exp(-2.4 * t)
            + 0.30 * np.sin(2 * np.pi * freq * 2.0 * t) * np.exp(-4.0 * t)
            + 0.08 * np.sin(2 * np.pi * freq * 3.0 * t) * np.exp(-6.0 * t))
    return tone * envelope(n, 0.008, 0.05)


def pad(freqs: list[float], dur: float) -> np.ndarray:
    n = int(dur * RATE)
    t = np.arange(n) / RATE
    out = np.zeros(n)
    for f in freqs:
        for detune in (-0.12, 0.0, 0.12):  # gentle chorus
            ff = f * 2 ** (detune / 12)
            out += np.sin(2 * np.pi * ff * t) + 0.25 * np.sin(2 * np.pi * 2 * ff * t)
    slow = 1 + 0.08 * np.sin(2 * np.pi * 0.2 * t)  # very slow swell
    return out / (3 * len(freqs)) * slow * envelope(n, 1.2, 1.2)


def bass(freq: float, dur: float) -> np.ndarray:
    n = int(dur * RATE)
    t = np.arange(n) / RATE
    return (np.sin(2 * np.pi * freq * t) + 0.2 * np.sin(2 * np.pi * 2 * freq * t)) * envelope(n, 0.15, 0.6)


def reverb_ir(seconds: float = 2.4, seed: int = 7) -> np.ndarray:
    rng = np.random.default_rng(seed)
    n = int(seconds * RATE)
    t = np.arange(n) / RATE
    ir = rng.standard_normal(n) * np.exp(-3.2 * t)
    # Soften the highs of the tail (simple one-pole low-pass).
    out = np.zeros(n)
    acc = 0.0
    for i in range(n):
        acc += 0.25 * (ir[i] - acc)
        out[i] = acc
    out[0] = 1.0  # dry impulse
    return out / np.abs(out).sum() * 6


# Chords as scale-degree triads in a major key (semitones from the tonic).
CHORDS = {
    "I": [0, 4, 7], "ii": [2, 5, 9], "iii": [4, 7, 11], "IV": [5, 9, 12], "V": [7, 11, 14], "vi": [9, 12, 16],
}


def compose(tonic: int, bpm: float, progression: list[str], bars_per_chord: int, seed: int, density: float) -> np.ndarray:
    """A calm piece: pad and bass follow the chords; the music box sings short stepwise phrases
    (contours) built on each chord, with one rhythm reused so the tune feels familiar, not random."""
    rng = np.random.default_rng(seed)
    beat = 60.0 / bpm
    bar = 4 * beat
    total_bars = len(progression) * bars_per_chord
    length = int(total_bars * bar * RATE)
    mix_pad = np.zeros(length + RATE * 3)
    mix_box = np.zeros(length + RATE * 3)
    mix_bass = np.zeros(length + RATE * 3)

    # A two-bar rhythm (beat positions of melody notes). Strong beats always sound.
    rhythm = [b for b in [0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0] if b in (0.0, 2.0, 4.0, 6.0) or rng.random() < density]
    # Melodic contours in scale steps above the chord root: stepwise and singable, never jumpy.
    contours = [[0, 2, 4, 2, 1, 0, 2, 0], [4, 3, 2, 1, 2, 0, -1, 0], [0, 1, 2, 4, 3, 2, 1, 0], [2, 1, 0, 1, 2, 3, 2, 2]]
    major = [0, 2, 4, 5, 7, 9, 11]
    root_degree = {"I": 0, "ii": 1, "iii": 2, "IV": 3, "V": 4, "vi": 5}

    def melody_midi(index: int) -> int:
        """Scale-step index -> MIDI note, an octave above the tonic."""
        return tonic + 12 + 12 * (index // 7) + major[index % 7]

    phrase = 0
    for ci, name in enumerate(progression):
        chord = [tonic + s for s in CHORDS[name]]
        start = int(ci * bars_per_chord * bar * RATE)
        dur = bars_per_chord * bar
        p = pad([midi_hz(n - 12) for n in chord], dur + 1.0)
        mix_pad[start:start + len(p)] += p
        b = bass(midi_hz(chord[0] - 24), dur)
        mix_bass[start:start + len(b)] += b

        degree = root_degree[name]
        anchor = degree if degree >= 3 else degree + 7  # keeps the tune in one comfortable octave
        last_chord = ci == len(progression) - 1
        for rep in range(bars_per_chord // 2):
            contour = contours[phrase % len(contours)]
            phrase += 1
            for k, beat_pos in enumerate(rhythm):
                at = start + int((rep * 8 + beat_pos) * beat * RATE)
                index = anchor + contour[int(beat_pos)]
                final_note = last_chord and rep == bars_per_chord // 2 - 1 and k == len(rhythm) - 1
                if final_note:
                    index = 7  # every loop ends on the tonic: settled and calm
                note = music_box(midi_hz(melody_midi(index)), beat * (4 if final_note else 3))
                mix_box[at:at + len(note)] += note * (0.9 if beat_pos in (0.0, 4.0) else 0.65)

    dry = 0.55 * mix_pad + 0.45 * mix_box + 0.35 * mix_bass
    ir = reverb_ir()
    size = 1 << int(np.ceil(np.log2(len(dry) + len(ir))))
    wet = np.fft.irfft(np.fft.rfft(dry, size) * np.fft.rfft(ir, size), size)[: len(dry)]  # fast convolution
    mixed = 0.6 * dry + 0.4 * wet
    # Seamless loop: fold everything past the loop point back onto the start.
    loop = mixed[:length].copy()
    tail = mixed[length:]
    loop[: len(tail)] += tail[: length]
    return loop / np.max(np.abs(loop)) * 0.5


def write_ogg(name: str, samples: np.ndarray) -> None:
    with tempfile.TemporaryDirectory() as tmp:
        wav = os.path.join(tmp, name + ".wav")
        with wave.open(wav, "wb") as w:
            w.setnchannels(1)
            w.setsampwidth(2)
            w.setframerate(RATE)
            w.writeframes((samples * 32767).astype(np.int16).tobytes())
        subprocess.run(
            ["ffmpeg", "-y", "-loglevel", "error", "-i", wav,
             "-af", "loudnorm=I=-26:TP=-6:LRA=7",
             "-ar", str(RATE), "-ac", "1", "-c:a", "libvorbis", "-q:a", "2",
             os.path.join(OUT, f"music_{name}.ogg")],
            check=True,
        )


def main() -> None:
    os.makedirs(OUT, exist_ok=True)
    # Menu: C major, very calm.
    write_ogg("menu", compose(60, 60, ["I", "vi", "IV", "V", "I", "iii", "IV", "I"], 2, seed=11, density=0.55))
    # Story: F major, gentle walking feel.
    write_ogg("story", compose(65, 66, ["I", "IV", "vi", "V", "I", "ii", "V", "I"], 2, seed=23, density=0.7))
    # Finale: G major, a little brighter for the fair.
    write_ogg("finale", compose(67, 76, ["I", "V", "vi", "IV", "I", "IV", "V", "I"], 2, seed=5, density=0.85))


if __name__ == "__main__":
    main()
