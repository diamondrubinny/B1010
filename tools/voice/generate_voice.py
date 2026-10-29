#!/usr/bin/env python3
"""Records every line of docs/voice/<case>.csv with Piper neural TTS (offline, at development time).

Only voices trained from scratch on public-domain recordings are used (see docs/CREDITS.md).
Output: app/src/main/assets/voice/<lineId>.ogg (mono Ogg Vorbis, loudness-normalised to -18 LUFS).

Usage (Piper installed in a virtualenv, voices downloaded to VOICES_DIR):
    venv/bin/python tools/voice/generate_voice.py --voices VOICES_DIR [--only ID ...]
"""
import argparse
import csv
import json
import os
import re
import subprocess
import sys
import tempfile
import wave

from piper import PiperVoice, SynthesisConfig

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))

# Speaker -> (voice model, length_scale). Calibrated so every voice averages about 2.4 words per second
# (including pauses) — calmer than everyday speech, which helps older listeners follow.
CAST = {
    "Narrator": ("en_GB-cori-high", 1.38),
    "Marmalade": ("en_US-ljspeech-high", 1.22),
    "Mrs. Bramble": ("en_US-kristin-medium", 1.22),
    "Miss Penrose": ("en_US-kristin-medium", 1.22),
    "Mr. Hollis": ("en_US-norman-medium", 1.25),
}

SENTENCE_PAUSE_SEC = 0.45
# Short sentences have no pauses inside them, so they sound rushed at the same setting: slow them a bit more.
SHORT_SENTENCE_WORDS = 10
SHORT_SENTENCE_EXTRA = 1.18
TARGET_LUFS = -18.0
# Hard ceiling on speaking pace (words per second of speech). Faster lines are slowed with a
# pitch-preserving tempo filter, so pacing is guaranteed rather than hoped for.
MAX_WORDS_PER_SEC = 2.7

# Spelling tweaks so the speech engine pronounces names and abbreviations naturally.
PRONUNCIATION = {
    "Mrs. ": "Missus ",
    "Mr. ": "Mister ",
    # A British word the American-trained voices otherwise say as "laid bird".
    "ladybird": "lady bird",
    "Willowbrook": "Willow-brook",
    "Ladybird": "Lady bird",
    # Tested 4/4 with speech recognition; the plain spelling comes out as "quasar" / "corsa".
    "croissant": "kruh-sont",
    "Croissant": "Kruh-sont",
    # Each chosen by repeated speech-recognition tests (see docs/voice/VERIFICATION.md).
    "Hollis": "Holl-iss",
    "chestnut": "chest-nut",
    "autumn": "ortum",
}


def speakable(text: str) -> str:
    for written, spoken in PRONUNCIATION.items():
        text = text.replace(written, spoken)
    return text


def sentences(text: str) -> list[str]:
    """Split after . ! ? or : so each sentence is followed by a short, calm pause."""
    parts = re.split(r"(?<=[.!?:])\s+", speakable(text).strip())
    return [p for p in parts if p]


def synthesize(voice: PiperVoice, text: str, length_scale: float, wav_path: str) -> None:
    rate = voice.config.sample_rate
    pause = b"\x00\x00" * int(SENTENCE_PAUSE_SEC * rate)
    frames = bytearray()
    for i, sentence in enumerate(sentences(text)):
        if i:
            frames += pause
        scale = length_scale * (SHORT_SENTENCE_EXTRA if len(sentence.split()) <= SHORT_SENTENCE_WORDS else 1.0)
        config = SynthesisConfig(length_scale=scale, noise_scale=0.6, noise_w_scale=0.7)
        for chunk in voice.synthesize(sentence, syn_config=config):
            frames += chunk.audio_int16_bytes
    with wave.open(wav_path, "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(rate)
        w.writeframes(bytes(frames))


def tempo_for(text: str, wav_path: str) -> float:
    """Returns the atempo factor (<= 1) needed to keep the line at or below MAX_WORDS_PER_SEC."""
    with wave.open(wav_path, "rb") as w:
        seconds = w.getnframes() / w.getframerate()
    words = len(re.findall(r"[A-Za-z']+", text))
    rate = words / max(seconds, 0.5)
    return 1.0 if rate <= MAX_WORDS_PER_SEC else max(0.7, MAX_WORDS_PER_SEC / rate)


def encode(wav_path: str, ogg_path: str, tempo: float = 1.0) -> None:
    """Pad with a little silence, measure loudness, apply the exact gain to reach TARGET_LUFS,
    let a limiter catch only the peaks (-4.4 dBFS, leaving headroom for Vorbis overshoot), encode mono Ogg Vorbis."""
    pad = (f"atempo={tempo:.3f}," if tempo < 0.999 else "") + "adelay=150,apad=pad_dur=0.3"
    target = f"I={TARGET_LUFS}:TP=-3:LRA=11"
    measure = subprocess.run(
        ["ffmpeg", "-hide_banner", "-i", wav_path, "-af", f"{pad},loudnorm={target}:print_format=json", "-f", "null", "-"],
        capture_output=True, text=True, check=True,
    ).stderr
    m = json.loads(measure[measure.rindex("{"):measure.rindex("}") + 1])
    gain_db = TARGET_LUFS - float(m["input_i"])
    second = f"{pad},volume={gain_db:.2f}dB,alimiter=limit=0.6:level=false:attack=3:release=60,aresample=22050"
    subprocess.run(
        ["ffmpeg", "-y", "-loglevel", "error", "-i", wav_path, "-af", second,
         "-ac", "1", "-c:a", "libvorbis", "-q:a", "4", ogg_path],
        check=True,
    )


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--voices", required=True, help="folder with <voice>.onnx and <voice>.onnx.json")
    parser.add_argument("--csv", default=os.path.join(ROOT, "docs", "voice", "lantern.csv"))
    parser.add_argument("--out", default=os.path.join(ROOT, "app", "src", "main", "assets", "voice"))
    parser.add_argument("--only", nargs="*", help="line ids to (re)generate")
    parser.add_argument("--prune", action="store_true", help="delete recordings whose line is no longer in the script")
    args = parser.parse_args()

    with open(args.csv, newline="", encoding="utf-8") as f:
        rows = list(csv.DictReader(f))
    unknown = sorted({r["speaker"] for r in rows} - CAST.keys())
    if unknown:
        print("No voice cast for: " + ", ".join(unknown), file=sys.stderr)
        return 1

    os.makedirs(args.out, exist_ok=True)
    if args.prune:
        wanted = {r["id"] + ".ogg" for r in rows}
        for name in sorted(os.listdir(args.out)):
            if name.endswith(".ogg") and name not in wanted:
                os.remove(os.path.join(args.out, name))
                print(f"removed {name}")
        if args.only is None:
            args.only = [r["id"] for r in rows if not os.path.exists(os.path.join(args.out, r["id"] + ".ogg"))]
    loaded: dict[str, PiperVoice] = {}
    with tempfile.TemporaryDirectory() as tmp:
        for row in rows:
            if args.only and row["id"] not in args.only:
                continue
            model, scale = CAST[row["speaker"]]
            if model not in loaded:
                loaded[model] = PiperVoice.load(os.path.join(args.voices, model + ".onnx"))
            wav = os.path.join(tmp, row["id"] + ".wav")
            synthesize(loaded[model], row["text"], scale, wav)
            tempo = tempo_for(row["text"], wav)
            encode(wav, os.path.join(args.out, row["id"] + ".ogg"), tempo)
            print(f"{row['id']:32s} {row['speaker']:13s} ok" + (f" (slowed x{tempo:.2f})" if tempo < 0.999 else ""))
    return 0


if __name__ == "__main__":
    sys.exit(main())
