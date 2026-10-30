# Willowbrook Mysteries

משחק מסתורין רגוע לטלוויזיית אנדרואיד, שנבנה עבור מבוגרים ואנשים החיים עם דמנציה או אלצהיימר.
המשחק עצמו באנגלית, ועובד אופליין לגמרי.

- אפיון ותוכנית: [`docs/PLAN.md`](docs/PLAN.md)
- מערכת עיצוב וניגודיות: [`docs/DESIGN_SYSTEM.md`](docs/DESIGN_SYSTEM.md)
- הפקת איורים וקול ב-AI: [`docs/ASSET_PIPELINE.md`](docs/ASSET_PIPELINE.md)
- בדיקות ידניות: [`docs/QA_CHECKLIST.md`](docs/QA_CHECKLIST.md)
- טיוטות: [`docs/PRIVACY_POLICY.md`](docs/PRIVACY_POLICY.md), [`docs/ACCESSIBILITY_STATEMENT.md`](docs/ACCESSIBILITY_STATEMENT.md)

## סטטוס
- **שלב 1 ✅** שלד לטלוויזיה, ניווט בשלט, פרופילים, הגדרות מלווה עם PIN, אישור לפני יציאה.
- **שלב 2 ✅** מנוע סיפור (JSON), פרק 0 (הדרכה) ופרק 1 (המאפייה) ניתנים למשחק מלא:
  חיפוש חפצים, זוגות זיכרון, סולם רמזים בן 5 רמות, התאמת קושי שקטה, שמירת התקדמות,
  "Welcome back" בחזרה למשחק, ו-Case files להפעלה חוזרת של פרקים.
- **שלב 3 ✅** קריינות (קבצי קול AI כשקיימים, אחרת הקריינות המובנית של הטלוויזיה), "Listen again", צלילי פעמון עדינים,
  ושלוש פעילויות חדשות: שאלת זיכרון / חידה, מיון, וכישוף-רצף. פרק 2 (הגינה) ניתן למשחק מלא.
- **שלב 4 ✅** התיק המלא, פרקים 0–4: הספרייה, וסיום בכיכר הכפר בהדלקת הפנס. נוספו כרטיסי "Let's chat" בסוף כל פרק,
  וסיכום התקדמות למלווה בהגדרות (מאחורי PIN).
- **שלב 5 ✅** כל הנכסים אמיתיים ומקוריים:
  - 285 קטעי קריינות (Piper, קולות בנחלת הכלל), שנבדקו אוטומטית לתוכן, קצב ועוצמה.
  - 3 קטעי מוזיקה, ומוזיקה שיורדת כשמישהו מדבר.
  - 54 איורי חפצים וסמלים, 4 דמויות, 5 סצנות, ובאנר.
  - מסך התאוששות אחרי קריסה, SBOM, סריקת חולשות, בנייה חתומה, ו-[`docs/PRODUCTION_CHECKLIST.md`](docs/PRODUCTION_CHECKLIST.md).
- **מדריך: Dr. Rubinny ✅** דמות המדריך מציגה את התיק, מלווה כל פרק ומברכת בסיום. הפורטרט צויר מתמונה אמיתית
  (`tools/art/portrait_from_photo.py`; התמונה המקורית לא נשמרת בריפו), והקול הוא `en_US-john-medium` (נחלת הכלל).
  פרסום מחייב הסכמה בכתב של האדם המצולם (ראו `docs/CREDITS.md`).
- **שלב 6.1 ✅ סצנות חיות:** שכבות עם parallax, מצלמה איטית, אור מהבהב וחלקיקים (`game/stage`, `ui/stage`).
  "Less movement on screen" מציג תמונה סטטית. תוכנית ההמשך: [`docs/STAGE6.md`](docs/STAGE6.md).
- **שלב 6.2 ✅** חפצים מונחים בתוך הסצנה, עם זכוכית מגדלת.
- **שלב 6.2b ✅** מוזיקה לכל מקום וצלילי סצנה עדינים (הגדרה חדשה: "Scene sounds").
- **שלב 6.6 ✅** "Lantern Beads": משחק חרוזים רגוע ומקורי, בלי הפסד, בפרק 4 וגם מהמסך הראשי.
- **הבא:** בדיקה על טלוויזיה אמיתית ועם משתמשים, אישורים מקצועיים, ומפתח חתימה אמיתי.

## הגדרות (כניסה סודית)
אין כפתור הגדרות במסך, כדי שהשחקן לא ייתקל בו. כדי לפתוח את ההגדרות, במסך הבית או במסך בחירת השחקן,
לוחצים לחיצה ארוכה על **Menu** בשלט (3 שניות) או על **OK** (5 שניות). מופיעה שאלת אישור, שבה "Back to the game" נבחר מראש.
בהגדרות: גודל טקסט, ניגודיות, פחות תנועה, קריינות, עוצמות, קושי, סיכום התקדמות, הסרת שחקן ומחיקת כל הנתונים.

## טכנולוגיות
Kotlin 2.4 · Jetpack Compose (BOM 2026.09) · Compose for TV (`tv-material` 1.1) · Navigation Compose ·
DataStore · kotlinx.serialization · AGP 9.5 / Gradle 9.8 · minSdk 24 (Android 7.0) · compileSdk 37 · targetSdk 36.

אין שרת, אין חשבונות, אין אנליטיקה, ואין הרשאת `INTERNET`.

## דרישות לפיתוח
- JDK 17 ומעלה (נבדק עם JDK 21)
- Android SDK עם `platforms;android-37.0` ו-`build-tools`
- Android Studio (מומלץ) או שורת פקודה

צרו קובץ `local.properties` (לא נכנס ל-git):
```properties
sdk.dir=/path/to/Android/sdk
```

## בנייה ובדיקות
```bash
./gradlew :app:testDebugUnitTest   # בדיקות יחידה + בדיקות UI בשלט (Robolectric) + צילומי מסך
./gradlew :app:lintDebug           # Lint, כולל בדיקות נגישות
./gradlew :app:assembleDebug       # APK לבדיקות: app/build/outputs/apk/debug/app-debug.apk
```
צילומי המסך (1920×1080) נשמרים ב-`app/build/screenshots/`.

סקריפט ההקלטה לקול ה-AI (`docs/voice/lantern.csv`) נוצר מהקוד. אחרי שינוי טקסט בסיפור, מעדכנים אותו כך:
```bash
./gradlew :app:testDebugUnitTest -PupdateVoiceScript=true
```
### נכסים (כולם נוצרים מקוד מקור בריפו)
```bash
python3 tools/art/build.py                 # איורים: SVG ← Chromium ← WebP (res/drawable-nodpi, res/drawable-xhdpi)
python3 tools/generate_music.py            # מוזיקת רקע (res/raw/music_*.ogg)
python3 tools/generate_sounds.py           # צלילי פעמון (res/raw/*.wav)
# קריינות: Piper ב-virtualenv + הורדת 4 הקולות (ראו docs/CREDITS.md):
venv/bin/python tools/voice/generate_voice.py --voices VOICES_DIR --prune
python3 tools/osv_scan.py docs/sbom/app-sbom.json   # סריקת חולשות (אחרי ./gradlew :app:cyclonedxDirectBom)
```
קרדיטים ורישוי: [`docs/CREDITS.md`](docs/CREDITS.md).

> אם Maven Central מחזיר שגיאה 429, אפשר להגדיר mirror מקומית ב-`~/.gradle/gradle.properties`:
> `robolectricRepoUrl=https://maven-central.storage-download.googleapis.com/maven2/`

## התקנה על טלוויזיה (התקנה ידנית / sideload)
1. בטלוויזיה: Settings → Device Preferences → About → ללחוץ 7 פעמים על **Build** כדי להפעיל Developer options.
2. Developer options → להפעיל **USB debugging** (ובחלק מהמכשירים גם **Network debugging**).
3. במחשב (באותה רשת):
   ```bash
   adb connect <TV-IP>:5555          # לאשר את החיבור בטלוויזיה
   adb install -r app/build/outputs/apk/release/app-release.apk
   ```
4. המשחק יופיע בשורת האפליקציות של הטלוויזיה.

השמות המדויקים של התפריטים משתנים בין יצרנים, ולכן יש לאמת מול מדריך המכשיר.
אחרי ההתקנה מומלץ לכבות את USB/Network debugging.

## Release חתום
```bash
cp keystore.properties.example keystore.properties   # למלא ערכים אמיתיים (הקובץ ב-.gitignore)
./gradlew :app:assembleRelease
```
את מפתח החתימה (`*.jks`) והסיסמאות שומרים מחוץ לריפו, עם גיבוי מאובטח. מפתח שאבד אי אפשר לשחזר,
ובלעדיו לא ניתן לעדכן התקנות קיימות.

## מבנה
```
app/src/main/java/net/rubinny/willowbrook/
├── audio/       narrator (recorded voice → on-device TTS fallback), gentle sound effects
├── data/        settings, profiles, progress (DataStore, local only), content loader
├── game/stage/  living scenes: camera, particles, light flicker, quality governor, per-scene effects
├── ui/stage/    LivingScene renderer (Canvas, one draw per frame, no recomposition)
├── game/        content model, validator, engines (hints, difficulty, hidden object, memory,
│                choice, sorting, spell), VoiceLines (ids + wording of every spoken line)
├── ui/theme/    colours, typography, dimensions
├── ui/components/  BigButton, ConfirmDialog, SecretEntry (hidden settings), setting rows…
├── ui/navigation/  routes + NavHost
└── ui/screens/  profiles, home, settings, play (chapter player), case files
app/src/main/assets/cases/lantern.json   story content (validated by tests)
```
