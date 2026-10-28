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
- **הבא, שלב 3:** קריינות (קול AI עם fallback ל-TTS), צלילים, ועוד סוגי פעילויות.

איורים: עד שתפיקו את איורי ה-AI (ראו [`docs/ART_PROMPTS_CH01.md`](docs/ART_PROMPTS_CH01.md)),
המשחק מציג אימוג'י של המערכת. כשמוסיפים קובץ בשם הנכון, הוא מוצג אוטומטית.

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
├── data/        settings, profiles, progress (DataStore, local only), content loader
├── game/        content model, validator, engines (hints, difficulty, hidden object, memory)
├── security/    PIN hashing (PBKDF2) + lockout
├── ui/theme/    colours, typography, dimensions
├── ui/components/  BigButton, ConfirmDialog, PinPad, setting rows…
├── ui/navigation/  routes + NavHost
└── ui/screens/  profiles, home, settings, play (chapter player), case files
app/src/main/assets/cases/lantern.json   story content (validated by tests)
```
