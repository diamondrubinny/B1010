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
| Dr. Rubinny (guide) | `en_US-john-medium` | LibriVox recordings (fine-tuned from `kristin`, also public domain) | Public domain |

- Voice models: https://huggingface.co/rhasspy/piper-voices (each folder contains a MODEL_CARD with the dataset licence).
- The Piper engine was only used as a development tool; it is **not** part of the app.
- Voices are either trained from scratch on public-domain data, or fine-tuned only from such a voice.
- **Deliberately excluded:** voices fine-tuned from `lessac` (unclear commercial terms of the original dataset)
  and voices under NC (non-commercial) or SA (share-alike) licences.
- The voices are synthetic and do not imitate any real person, including Dr. Rubinny's voice.

## Music and sounds
Composed and generated in code by this project (`tools/generate_music.py`, `tools/generate_sounds.py`,
`tools/generate_ambience.py`): 6 music pieces (menu and one per place), 5 scene soundscapes, and the bells.
No samples or third-party recordings. Original work of the project.

## Illustrations
All scenes, characters, objects, icons and the TV banner were drawn as original vector code (SVG) by this
project (`tools/art/`) and rendered with Chromium. No AI image models, stock images or third-party art.

**Exception, Dr. Rubinny's portrait (`char_rubinny`):** a painted version of a photograph of a real person,
supplied by the project owner (`tools/art/portrait_from_photo.py`). The original photo is not stored in the
repository, and no photo metadata is carried over. Publishing requires the written consent of the person
pictured (or of whoever holds that right), and the rights to the photograph.

## Font
**Atkinson Hyperlegible**: © Braille Institute of America, Inc. — SIL Open Font License 1.1
(`app/src/main/assets/licenses/AtkinsonHyperlegible-OFL.txt`).

## Libraries
AndroidX / Jetpack Compose / Compose for TV / Navigation / DataStore (Apache 2.0),
Kotlin & kotlinx.serialization / kotlinx.coroutines (Apache 2.0).
Full list with versions: the SBOM `docs/sbom/app-sbom.json` (CycloneDX).

## לבדיקה משפטית
- **דמות Dr. Rubinny:** הסכמה בכתב של האדם המצולם (או של מי שמוסמך לתת אותה) לשימוש בדמותו ובשמו במשחק,
  וזכויות בתצלום המקורי (מי צילם אותו).
- אישור שהקלטות סינתטיות ממודל שאומן על נתונים בנחלת הכלל מותרות לשימוש מסחרי במדינות ההפצה.
- אישור שאין צורך בהצהרה מיוחדת על "קול סינתטי". מומלץ בכל מקרה לציין זאת ב-About.
