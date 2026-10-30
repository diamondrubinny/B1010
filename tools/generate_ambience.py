#!/usr/bin/env python3
"""Renders the soft background soundscapes, one per scene (original; synthesised, no recordings).

Clinical rules (docs/STAGE6.md, 6.2b): nothing that could be mistaken for something real happening at home,
so no voices, knocks, phones, footsteps, doors or alarms. Nothing sudden: every small sound fades in, and
stays well below the room tone it sits in. Very quiet: about -36 LUFS, under the music.

Output: app/src/main/res/raw/amb_<scene>.ogg, seamless 40-second loops (mono Ogg Vorbis).
Usage: python3 tools/generate_ambience.py
"""
import os
import subprocess
import tempfile
import wave

import numpy as np

RATE = 22050
SECONDS = 40
FADE = 2.0  # seconds of cross-fade at the loop point
OUT = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "res", "raw")


def lowpass(x: np.ndarray, cutoff: float) -> np.ndarray:
    """Gentle one-pole low-pass via FFT (steep enough for room tones, no ringing)."""
    spectrum = np.fft.rfft(x)
    freqs = np.fft.rfftfreq(len(x), 1 / RATE)
    spectrum /= np.sqrt(1 + (freqs / cutoff) ** 2)
    return np.fft.irfft(spectrum, len(x))


def bandpass(x: np.ndarray, low: float, high: float) -> np.ndarray:
    spectrum = np.fft.rfft(x)
    freqs = np.fft.rfftfreq(len(x), 1 / RATE)
    spectrum *= 1 / np.sqrt(1 + (low / np.maximum(freqs, 1)) ** 4) / np.sqrt(1 + (freqs / high) ** 4)
    return np.fft.irfft(spectrum, len(x))


def noise(n: int, seed: int) -> np.ndarray:
    return np.random.default_rng(seed).standard_normal(n)


def slow_swell(n: int, seed: int, depth: float, period: float) -> np.ndarray:
    """A slow, smooth loudness wave (like a breeze rising and falling)."""
    t = np.arange(n) / RATE
    rng = np.random.default_rng(seed)
    out = np.ones(n)
    for k in range(3):
        out += depth / 3 * np.sin(2 * np.pi * t / (period * (1 + 0.37 * k)) + rng.uniform(0, 6.28))
    return out


def soft_event(n: int, attack: float, decay: float) -> np.ndarray:
    t = np.arange(n) / RATE
    return (1 - np.exp(-t / attack)) * np.exp(-t / decay)


def scatter(total: int, seed: int, mean_gap: float, make) -> np.ndarray:
    """Places small sounds at calm, irregular intervals (never two at once)."""
    rng = np.random.default_rng(seed)
    out = np.zeros(total)
    at = rng.uniform(0.2, 0.2 + mean_gap) * RATE
    while at < total - RATE:
        piece = make(rng)
        end = min(total, int(at) + len(piece))
        out[int(at):end] += piece[: end - int(at)]
        at += rng.uniform(0.6, 1.4) * mean_gap * RATE
    return out


def fire(total: int) -> np.ndarray:
    """A cosy hearth: warm low rumble with small, soft crackles (each fades in over 4 ms)."""
    rumble = lowpass(noise(total, 1), 180) * slow_swell(total, 2, 0.3, 7)
    rumble /= np.std(rumble)

    def crackle(rng):
        n = int(rng.uniform(0.02, 0.05) * RATE)
        return bandpass(noise(n, int(rng.integers(1 << 30))), 900, 3500) * soft_event(n, 0.004, 0.012) * rng.uniform(0.6, 1.4)

    crackles = scatter(total, 3, 0.35, crackle)
    crackles /= np.std(crackles) + 1e-9
    return rumble + 0.16 * crackles


def oven(total: int) -> np.ndarray:
    """A warm kitchen: soft oven hum and a gently bubbling pot, far away."""
    hum = lowpass(noise(total, 11), 220) * slow_swell(total, 12, 0.15, 11)
    hum /= np.std(hum)
    t = np.arange(total) / RATE
    warm = 0.08 * np.sin(2 * np.pi * 110 * t) * slow_swell(total, 13, 0.4, 5)

    def bubble(rng):
        n = int(0.09 * RATE)
        tt = np.arange(n) / RATE
        f = rng.uniform(300, 520) * (1 + 0.8 * tt / 0.09)  # a small rising "blup"
        return np.sin(2 * np.pi * np.cumsum(f) / RATE) * soft_event(n, 0.01, 0.025)

    bubbles = scatter(total, 14, 0.9, bubble)
    return hum + warm + 0.25 * bubbles / (np.std(bubbles) + 1e-9)


def birds_and_breeze(total: int) -> np.ndarray:
    """A garden: a light breeze in the leaves and a few distant, sweet birdsong phrases."""
    breeze = bandpass(noise(total, 21), 250, 1800) * slow_swell(total, 22, 0.8, 9)
    breeze /= np.std(breeze)

    def phrase(rng):
        notes = int(rng.integers(2, 5))
        base = rng.uniform(2600, 3600)
        parts = []
        for k in range(notes):
            n = int(rng.uniform(0.07, 0.12) * RATE)
            tt = np.arange(n) / RATE
            f = base * (1 + 0.15 * np.sin(np.pi * tt / tt[-1])) * (1 + 0.05 * k)
            tone = np.sin(2 * np.pi * np.cumsum(f) / RATE) * np.sin(np.pi * tt / tt[-1]) ** 2
            parts += [tone, np.zeros(int(0.05 * RATE))]
        return np.concatenate(parts)

    birds = scatter(total, 23, 4.0, phrase)
    return breeze + 0.22 * birds / (np.std(birds[birds != 0]) + 1e-9)


def quiet_room(total: int) -> np.ndarray:
    """A library: a very soft, warm room tone and now and then a page turning far away."""
    tone = lowpass(noise(total, 31), 400) * slow_swell(total, 32, 0.1, 13)
    tone /= np.std(tone)

    def page(rng):
        n = int(0.35 * RATE)
        return bandpass(noise(n, int(rng.integers(1 << 30))), 1500, 6000) * soft_event(n, 0.06, 0.08)

    pages = scatter(total, 33, 12.0, page)
    return tone + 0.25 * pages / (np.std(pages[pages != 0]) + 1e-9)


def evening(total: int) -> np.ndarray:
    """The village square at dusk: a soft evening breeze, crickets far away and a wind chime now and then."""
    breeze = bandpass(noise(total, 41), 150, 1200) * slow_swell(total, 42, 0.6, 10)
    breeze /= np.std(breeze)
    t = np.arange(total) / RATE
    chirp_gate = (np.sin(2 * np.pi * 2.6 * t) > 0.6).astype(float)
    chirp_gate = lowpass(chirp_gate, 40)
    crickets = np.sin(2 * np.pi * 4300 * t) * chirp_gate * slow_swell(total, 43, 0.6, 6)
    pentatonic = [72, 74, 76, 79, 81, 84]

    def chime(rng):
        n = int(2.5 * RATE)
        tt = np.arange(n) / RATE
        f = 440 * 2 ** ((pentatonic[int(rng.integers(len(pentatonic)))] - 69) / 12)
        return (np.sin(2 * np.pi * f * tt) + 0.3 * np.sin(2 * np.pi * 2.76 * f * tt)) * soft_event(n, 0.01, 0.8)

    chimes = scatter(total, 44, 6.0, chime)
    return breeze + 0.12 * crickets / np.std(crickets) + 0.3 * chimes / (np.std(chimes[chimes != 0]) + 1e-9)


SCENES = {"cottage": fire, "bakery": oven, "garden": birds_and_breeze, "library": quiet_room, "square": evening}


def loop(make) -> np.ndarray:
    """Seamless loop: render a little extra and cross-fade the end into the start (equal power)."""
    total = int((SECONDS + FADE) * RATE)
    x = make(total)
    f = int(FADE * RATE)
    body = x[: total - f].copy()
    w = np.sin(np.linspace(0, np.pi / 2, f))
    body[:f] = body[:f] * w + x[total - f:] * np.cos(np.linspace(0, np.pi / 2, f))
    return body / np.max(np.abs(body)) * 0.5


def write_ogg(name: str, samples: np.ndarray) -> None:
    with tempfile.TemporaryDirectory() as tmp:
        wav = os.path.join(tmp, name + ".wav")
        with wave.open(wav, "wb") as w:
            w.setnchannels(1)
            w.setsampwidth(2)
            w.setframerate(RATE)
            w.writeframes((samples * 32767).astype(np.int16).tobytes())
        subprocess.run(
            ["ffmpeg", "-y", "-loglevel", "error", "-i", wav, "-af", "loudnorm=I=-36:TP=-9:LRA=5",
             "-ar", str(RATE), "-ac", "1", "-c:a", "libvorbis", "-q:a", "1", os.path.join(OUT, f"amb_{name}.ogg")],
            check=True,
        )


def main() -> None:
    os.makedirs(OUT, exist_ok=True)
    for name, make in SCENES.items():
        write_ogg(name, loop(make))
        print(f"amb_{name}.ogg")


if __name__ == "__main__":
    main()
