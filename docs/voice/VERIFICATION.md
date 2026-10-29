# Voice verification report

Generated 2026-10-29 by automatic checks on every recording in `app/src/main/assets/voice/`.

## Method
- **Content:** each recording is transcribed back to text with Whisper (`small.en`) and compared word-by-word
  with the script (similarity 0–1). Lines below 0.85 are re-recorded automatically (up to 6 attempts).
- **Pace:** words per second. The generator caps each recording at 2.7 words/second measured over the whole file (a pitch-preserving tempo filter slows faster lines). The figures below exclude the silent padding at the start and end, so they read slightly higher.
- **Loudness:** integrated loudness (EBU R128) aimed at −18 LUFS; a limiter keeps peaks at about −4 dBFS before encoding (Vorbis encoding can add up to ~3 dB).

## Results (285 recordings)
| Measure | Result |
|---|---|
| Content similarity | average 0.976, lowest 0.86 |
| Pace (words/second) | average 2.28, highest 3.05 |
| Loudness (LUFS) | -19.3 to -17.0 |
| Highest peak (dBFS) | -0.9 |
| Recordings still below 0.85 similarity | 0 |

## Lines below 0.85 after retries
The speech recogniser is not perfect: a low score can be a recognition error rather than a speaking error.
These lines should be checked by ear first.

| Line id | Similarity | Script | Recogniser heard |
|---|---|---|---|

## Pronunciation fixes applied (tools/voice/generate_voice.py)
`Mrs.`→Missus, `Mr.`→Mister, ladybird→lady bird, Willowbrook→Willow-brook, croissant→kruh-sont,
Hollis→Holl-iss, chestnut→chest-nut, autumn→ortum. Each spelling was chosen by repeated recognition tests.
