# Credits & Asset Licences

> טיוטה טכנית. לפני הפצה ציבורית נדרש אישור משפטי לרישוי הנכסים (ראו סעיף "לבדיקה משפטית").

## Voices (narration)
All recordings in `app/src/main/assets/voice/` were synthesised offline with **Piper** (open-source neural TTS),
using only voices **trained from scratch on public-domain recordings**:

| Character | Voice model | Training data | Data licence |
|---|---|---|---|
| Narrator | `en_GB-cori-high` | LibriVox recordings | Public domain |
| Marmalade | `en_US-ljspeech-high` | LJ Speech dataset (LibriVox) | Public domain |
| Mrs. Bramble, Miss Penrose | `en_US-kristin-medium` | LibriVox recordings | Public domain |
| Mr. Hollis | `en_US-norman-medium` | LibriVox recordings | Public domain |

- Voice models: https://huggingface.co/rhasspy/piper-voices (each folder contains a MODEL_CARD with the dataset licence).
- The Piper engine was only used as a development tool; it is **not** part of the app.
- **Deliberately excluded:** voices fine-tuned from `lessac` (unclear commercial terms of the original dataset)
  and voices under NC (non-commercial) or SA (share-alike) licences.
- The voices are synthetic and do not imitate any real person.

## Music and sounds
Composed and generated in code by this project (`tools/generate_music.py`, `tools/generate_sounds.py`).
No samples or third-party recordings. Original work of the project.

## Illustrations
All scenes, characters, objects, icons and the TV banner were drawn as original vector code (SVG) by this
project (`tools/art/`) and rendered with Chromium. No AI image models, stock images or third-party art.

## Font
**Atkinson Hyperlegible**: © Braille Institute of America, Inc. — SIL Open Font License 1.1
(`app/src/main/assets/licenses/AtkinsonHyperlegible-OFL.txt`).

## Libraries
AndroidX / Jetpack Compose / Compose for TV / Navigation / DataStore (Apache 2.0),
Kotlin & kotlinx.serialization / kotlinx.coroutines (Apache 2.0).
Full list with versions: the SBOM `docs/sbom/app-sbom.json` (CycloneDX).

## לבדיקה משפטית
- אישור שהקלטות סינתטיות ממודל שאומן על נתונים בנחלת הכלל מותרות לשימוש מסחרי במדינות ההפצה.
- אישור שאין צורך בהצהרה מיוחדת על "קול סינתטי". מומלץ בכל מקרה לציין זאת ב-About.
