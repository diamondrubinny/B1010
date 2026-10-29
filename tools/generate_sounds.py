#!/usr/bin/env python3
"""Generates the game's gentle bell sounds (original, no third-party licence).

Soft attack (no clicks), slow decay, peak about -8 dBFS. Notes are pentatonic, so any order sounds pleasant.
Usage: python3 tools/generate_sounds.py  -> writes app/src/main/res/raw/*.wav
"""
import math
import os
import struct
import wave

RATE = 22050
OUT = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "res", "raw")
PEAK = 0.4

NOTE = {"C5": 523.25, "D5": 587.33, "E5": 659.25, "G5": 783.99, "A5": 880.00, "C6": 1046.50, "E6": 1318.51, "G4": 392.00}


def bell(freq, duration, start, buf):
    """Adds one soft bell strike to buf (list of floats)."""
    partials = [(1.0, 1.0, 2.2), (2.0, 0.35, 3.5), (3.01, 0.12, 5.0), (4.2, 0.05, 7.0)]
    attack = int(0.015 * RATE)
    n = int(duration * RATE)
    s0 = int(start * RATE)
    while len(buf) < s0 + n:
        buf.append(0.0)
    for i in range(n):
        t = i / RATE
        env_a = 0.5 - 0.5 * math.cos(math.pi * min(i, attack) / attack)  # raised-cosine attack
        v = 0.0
        for ratio, amp, decay in partials:
            v += amp * math.exp(-decay * t) * math.sin(2 * math.pi * freq * ratio * t)
        buf[s0 + i] += env_a * v


def write(name, buf):
    peak = max(abs(x) for x in buf) or 1.0
    fade = int(0.05 * RATE)  # gentle fade-out at the very end
    with wave.open(os.path.join(OUT, name + ".wav"), "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(RATE)
        frames = bytearray()
        for i, x in enumerate(buf):
            g = min(1.0, (len(buf) - i) / fade)
            frames += struct.pack("<h", int(max(-1, min(1, x / peak * PEAK * g)) * 32767))
        w.writeframes(bytes(frames))


def main():
    os.makedirs(OUT, exist_ok=True)
    # Spell notes: one per symbol.
    for name, note in [("note_star", "C5"), ("note_moon", "E5"), ("note_leaf", "G5"), ("note_flower", "A5")]:
        b = []
        bell(NOTE[note], 1.0, 0, b)
        write(name, b)
    # Found / correct: two rising notes.
    b = []
    bell(NOTE["E5"], 0.8, 0.0, b)
    bell(NOTE["A5"], 1.0, 0.12, b)
    write("sfx_found", b)
    # Activity complete: soft arpeggio.
    b = []
    for i, note in enumerate(["C5", "E5", "G5", "C6"]):
        bell(NOTE[note], 1.2, i * 0.14, b)
    write("sfx_complete", b)
    # Reward: light sparkle.
    b = []
    for i, note in enumerate(["G5", "C6", "E6", "C6", "E6"]):
        bell(NOTE[note], 1.0, i * 0.09, b)
    write("sfx_reward", b)
    # Card flip: very short, low and soft.
    b = []
    bell(NOTE["G4"], 0.25, 0, b)
    write("sfx_flip", [x * 0.6 for x in b])


if __name__ == "__main__":
    main()
