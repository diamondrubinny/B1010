# Willowbrook Mysteries — אפיון ותוכנית עבודה (MVP)

> משחק מסתורין/בילוש קוזי עם קסמים עדינים, חיפוש חפצים, חידות ותרגול זיכרון,
> המיועד למבוגרים ולאנשים החיים עם דמנציה/אלצהיימר. שפת המשחק: **אנגלית**. פלטפורמה: **Android TV**.
>
> סטטוס: **טיוטת אפיון — ממתין לאישור לפני בנייה.**

---

## 1. סיכום ההבנה

| נושא | הבנה |
|---|---|
| מוצר | משחק הרפתקה-בילוש רגוע, מחולק ל"תיקים" (Cases) קצרים, עם מיני-משחקים קוגניטיביים |
| קהל יעד | מבוגרים 65+, דמנציה קלה-בינונית, לעיתים בליווי מטפל/בן משפחה |
| מכשיר | טלוויזיה אנדרואיד (Android TV / Google TV), שליטה בשלט D-pad בלבד (חיצים + OK + Back) |
| ערך | גירוי קוגניטיבי, זיכרון, קשב, תחושת הצלחה, רמיניסצנציה, זמן איכות משותף |
| מה זה **לא** | לא מכשיר רפואי, לא אבחון, לא טיפול. אין טענות קליניות |

### הנחות עבודה (אם לא תתקן אותי)
1. המשחק עובד **אופליין לחלוטין** — אין שרת, אין חשבונות, אין אנליטיקה.
2. שחקן אחד בכל פעם, לרוב עם מלווה בחדר. עד 4 פרופילים מקומיים (כינוי בלבד).
3. קריינות באמצעות **TTS מובנה של אנדרואיד** (חינמי, אופליין) + כתוביות תמיד.
4. אמנות: איורים וקטוריים פשוטים ונקיים שאני מייצר (Vector Drawables) ל-MVP; החלפה באמנות מקצועית בהמשך.
5. הפצה: APK להתקנה ידנית בשלב ראשון, Google Play בשלב מאוחר.

---

## 2. עקרונות עיצוב קליניים (הליבה של המוצר)

אלו ההחלטות שמבדילות משחק "לחולי דמנציה" ממשחק רגיל עם פונט גדול:

1. **Errorless Learning — אין כישלון.** אין "Wrong!", אין צליל באזר, אין אובדן נקודות/חיים, אין Game Over. תשובה לא נכונה → "Hmm, let's look a little more" + רמז.
2. **אין לחץ זמן.** אין טיימרים, אין ספירה לאחור. המשחק מחכה לנצח.
3. **⚠️ אין גניבות, פשעים או נבלים.** חשדנות ומחשבות "גנבו לי דברים" הן תסמין שכיח בדמנציה. לכן כל התעלומות הן **"אבדות ומציאות"**: חפצים שהתגלגלו, נשכחו, הוזזו על ידי חתול שובב או רוח. אין מאשימים איש.
4. **קסם עדין ולא מפחיד.** "כישופים" = הדלקת פנס, פריחת פרחים, החזרת צבע. אין חושך מאיים, מכשפות רעות, הפחדות או קפיצות.
5. **מכבד, לא ילדותי.** סגנון של כפר אנגלי משנות ה-50, טון של ספר מתח קוזי (Agatha Christie / Miss Marple). אין "Good job, sweetie".
6. **עומס קוגניטיבי נמוך.** משימה אחת על המסך, הוראה אחת בכל פעם, מקסימום 3 אפשרויות בחירה (מתכוונן).
7. **חזרה ועקביות.** אותו מבנה מסך בכל מקום, אותו מיקום לכפתורים, דמות מלווה קבועה (החתולה **Marmalade**) שמזכירה "מה אנחנו מחפשים".
8. **תזכורת הקשר מתמדת.** בכל רגע לוחץ על כפתור "What are we doing?" ושומע/רואה את המטרה הנוכחית. בכניסה לסשן — סיכום "Last time we…".
9. **התאמת קושי שקטה.** המערכת מורידה/מעלה קושי לפי שימוש ברמזים — בלי להודיע לשחקן.
10. **סשנים קצרים.** כל פרק 5–10 דקות, שמירה אוטומטית אחרי כל צעד.

> נדרש ייעוץ של מרפא/ה בעיסוק או מומחה/ית לגריאטריה/דמנציה לאישור העקרונות ולבדיקות שימושיות.

---

## 3. התוכן: התיק הראשון (MVP)

### "The Lantern of Willowbrook"
הכפר Willowbrook מתכונן ליריד הסתיו. הפנס הקסום שמאיר את היריד כבה, כי שלושת "קמעות האור" (Light Charms) התגלגלו ונשכחו ברחבי הכפר. השחקן — הבלש/ית של הכפר — יחד עם החתולה Marmalade, מוצא אותם ומדליק את הפנס מחדש.

| פרק | מקום | דמות | פעילויות |
|---|---|---|---|
| 0 | בית הבלש | Marmalade | הדרכה: שימוש בשלט, לחיצת OK, כפתור "What are we doing?" |
| 1 | המאפייה | Mrs. Bramble האופה | שיחה → חיפוש חפצים במטבח → "Which ingredient did Mrs. Bramble mention?" → קמע #1 |
| 2 | הגינה | Mr. Hollis הגנן | חיפוש חפצים → כישוף פריחה (רצף) → התאמת זוגות זרעים/פרחים → קמע #2 |
| 3 | הספרייה | Miss Penrose הספרנית | חידת תמונה → משחק זיכרון קלפים → מיון ספרים לפי צבע → קמע #3 |
| 4 | כיכר הכפר | כולם | שחזור: "Who gave us each charm?" (התאמת דמות↔מקום) → כישוף הדלקה → חגיגה |

### סוגי מיני-משחקים (Activity Types)

| סוג | תיאור | מה מתרגל | התאמת קושי |
|---|---|---|---|
| `HIDDEN_OBJECT` | סצנה מחולקת לרשת של אזורים ניתנים לפוקוס; למצוא 1–3 חפצים | סריקה חזותית, קשב | מספר חפצים, כמות "רעש" בסצנה |
| `MEMORY_MATCH` | קלפים הפוכים, למצוא זוגות | זיכרון עבודה | 4/6/8 קלפים |
| `CLUE_RECALL` | שאלה על משהו שנאמר/נראה קודם | זיכרון אפיזודי קצר טווח | 2/3 אפשרויות, הצגת רמז חזותי |
| `SPELL_SEQUENCE` | צפייה ברצף סמלים קסומים וחזרה עליו | זיכרון רצף | אורך 2–4, מהירות הצגה |
| `SORTING` | לשייך פריט לקטגוריה (מטבח/גינה/ספרייה) | חשיבה מושגית | מספר קטגוריות |
| `PICTURE_RIDDLE` | חידה קצרה עם תשובה בתמונה | הסקה, שפה | מספר אפשרויות |
| `REMINISCENCE` (אופציונלי) | כרטיס שיחה למלווה: "Did you ever bake bread? What was your favourite?" | רמיניסצנציה, שיח | — |

### סולם רמזים (בכל פעילות)
1. אחרי ~20 שניות ללא פעולה: Marmalade מנצנצת ומזכירה את המטרה.
2. אחרי ~40 שניות: רמז מילולי ("It's near something warm…").
3. אחרי ~60 שניות: האזור הנכון זוהר בעדינות.
4. אחרי ~90 שניות: הפוקוס עובר אוטומטית לתשובה — לחיצת OK מנצחת.
(כל הזמנים ניתנים לשינוי בהגדרות המלווה.)

---

## 4. User Stories

**שחקן/ית**
- כשחקן, אני רוצה להבין בכל רגע מה אני צריך לעשות, כדי לא להרגיש אבוד.
- כשחקן, אני רוצה לשלוט בהכל עם חיצים ו-OK בלבד.
- כשחקן, אני רוצה לשמוע את הטקסט מוקרא, כי קשה לי לקרוא.
- כשחקן, אני רוצה להצליח תמיד בסוף, ולהרגיש שאני בלש טוב.
- כשחקן, אני רוצה לחזור ולהמשיך מאותה נקודה מחר.

**מלווה / מטפל/ת**
- כמלווה, אני רוצה לבחור פרופיל ולהתחיל משחק בתוך 2 לחיצות.
- כמלווה, אני רוצה להגדיל טקסט, להאט קריינות ולשנות קושי — מאחורי קוד PIN.
- כמלווה, אני רוצה לראות סיכום פשוט (פרקים שהושלמו, שימוש ברמזים) כדי לשים לב לשינויים.
- כמלווה, אני רוצה למחוק את כל המידע בלחיצה.
- כמלווה, אני רוצה שלחיצה מקרית על Back לא תצא מהמשחק.

---

## 5. מסכים וזרימות

```
Splash (2s) ──► Profile Picker ──► Home ("Continue the case" | "Case Files" | "Settings 🔒")
                                     │
                                     ├─► Continue ──► Chapter Intro (narration) ──► Scene/Activity ──► Reward ──► next…
                                     ├─► Case Files ──► Case list ──► Chapter list (replay any)
                                     └─► Settings (PIN) ──► Display | Audio | Difficulty | Hints | Profiles | Progress | Delete data | About/Privacy/Accessibility
```

| מסך | תוכן עיקרי |
|---|---|
| Profile Picker | 1–4 כרטיסים גדולים: כינוי + אייקון (לא תמונה אמיתית) |
| Home | 2–3 כפתורים ענקיים בלבד, "Continue" מקבל פוקוס ראשוני |
| Chapter Intro | איור + קריינות + כתובית + "Press OK to begin" |
| Activity | אזור משחק 75% מהמסך, פס עליון: מטרה נוכחית, פס תחתון: "What are we doing?" / "Help me" |
| Reward | אנימציה עדינה (ללא הבהובים), משפט מחמיא מכבד, קמע נאסף |
| Exit Confirm | "Would you like to rest now? Your progress is saved." — Yes / Keep playing (ברירת מחדל: Keep playing) |
| Settings | נעול ב-PIN (4 ספרות, מוזן עם חיצים) |

---

## 6. ארכיטקטורה

### 6.1 החלטה טכנולוגית — המלצה: Kotlin + Jetpack Compose for TV (Native)

| אפשרות | יתרונות | חסרונות |
|---|---|---|
| **✅ Kotlin + Compose for TV (`androidx.tv:tv-material`)** | ניווט D-pad ופוקוס מובנים ומדויקים, ביצועים טובים בממירים זולים, TTS מקורי אופליין, תמיכה ב-TalkBack, עמידה בדרישות Google Play ל-TV | לא TypeScript; דורש ידע Android |
| TypeScript (Vite) בתוך WebView מעטפת | שפה מועדפת, בדיקה בדפדפן | WebView ישן/איטי בטלוויזיות רבות; `speechSynthesis` לא נתמך באופן אמין ב-Android WebView → צריך גשר JS↔Native ל-TTS; ניהול פוקוס D-pad ידני ושביר |
| Godot / Unity | מנוע משחק מלא | כבד, נגישות (TalkBack) חלשה, עקומת למידה, Overkill לממשק כרטיסים/תמונות |
| React Native for TV | TypeScript | Fork קהילתי (react-native-tvos), תחזוקה ותמיכת Android TV פחות יציבות — דורש אימות מול התיעוד הרשמי |

**נימוק:** במוצר הזה אמינות הפוקוס בשלט, הקראה אופליין ותגובה מהירה על חומרה חלשה קריטיות יותר מהעדפת השפה. כדי לשמור על גמישות, **כל התוכן (סיפורים, דיאלוגים, פעילויות) יוגדר ב-JSON** — כך שאפשר להוסיף תיקים בלי לגעת בקוד, ובעתיד אפשר להשתמש באותו תוכן גם לגרסת Web.

### 6.2 שכבות

```
┌──────────────────────────── UI (Compose for TV) ────────────────────────────┐
│ screens/ (Home, Profiles, Chapter, Activity*, Reward, Settings)             │
│ components/ (BigButton, FocusRing, CaptionBar, CompanionHint, PinPad)       │
│ theme/ (colors 7:1, Atkinson Hyperlegible, spacing, overscan safe area)     │
└──────────────▲──────────────────────────────────────────────────────────────┘
               │ StateFlow / UiState
┌──────────────┴──────────── ViewModels ──────────────────────────────────────┐
└──────────────▲──────────────────────────────────────────────────────────────┘
┌──────────────┴──────────── Game Engine (pure Kotlin, unit-tested) ──────────┐
│ CaseRunner · ActivityEngines (per type) · HintScheduler · DifficultyAdapter │
└──────────────▲──────────────────────────────────────────────────────────────┘
┌──────────────┴──────────── Data ────────────────────────────────────────────┐
│ ContentRepository (assets/cases/*.json, kotlinx.serialization + validation) │
│ ProgressRepository / SettingsRepository (Jetpack DataStore, local only)     │
│ Narrator (Android TextToSpeech) · SoundPlayer (SoundPool)                   │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 6.3 Backend / API
**אין.** אין שרת, אין API endpoints, אין בסיס נתונים מרוחק. האפליקציה לא מבקשת הרשאת `INTERNET`. זו החלטת פרטיות ואבטחה מכוונת.

### 6.4 מודל נתונים מקומי (DataStore / JSON)

```kotlin
// Persisted (DataStore)
data class Profile(val id: String, val nickname: String, val avatar: AvatarId, val createdAt: Long)
data class Settings(
    val textScale: Float = 1.0f,          // 1.0 – 1.6
    val speechRate: Float = 0.85f,        // slower than default
    val narrationEnabled: Boolean = true,
    val captionsEnabled: Boolean = true,  // always on by default
    val difficulty: Difficulty = Difficulty.AUTO,
    val hintDelaySec: Int = 20,
    val reduceMotion: Boolean = false,
    val highContrast: Boolean = false,
    val musicVolume: Float = 0.3f,
    val pinHash: String? = null           // salted hash, never plain
)
data class ProgressEntry(
    val profileId: String, val caseId: String, val chapterId: String,
    val stepIndex: Int, val completed: Boolean, val hintsUsed: Int, val updatedAt: Long
)

// Content (assets/cases/lantern.json) — read-only
data class Case(val id: String, val title: String, val chapters: List<Chapter>)
data class Chapter(val id: String, val title: String, val location: String, val steps: List<Step>)
sealed interface Step  // Narration | Dialogue | Activity(type, params, hints) | Reward
```

### 6.5 מבנה תיקיות

```
B1010/
├── README.md
├── docs/
│   ├── PLAN.md                    ← המסמך הזה
│   ├── CONTENT_GUIDE.md           ← איך כותבים תיק חדש (JSON schema + כללי טון)
│   ├── ACCESSIBILITY_STATEMENT.md ← טיוטה
│   ├── PRIVACY_POLICY.md          ← טיוטה
│   └── QA_CHECKLIST.md
├── settings.gradle.kts
├── build.gradle.kts
├── gradle/libs.versions.toml
└── app/
    ├── build.gradle.kts
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml          (LEANBACK_LAUNCHER, banner, touchscreen required=false)
        │   ├── assets/cases/lantern.json
        │   ├── res/ (drawable vectors, banner 320x180, fonts, raw sounds, values)
        │   └── java/net/rubinny/willowbrook/
        │       ├── MainActivity.kt
        │       ├── ui/theme/  ui/components/  ui/screens/  ui/navigation/
        │       ├── game/engine/  game/activities/  game/model/
        │       ├── data/content/  data/progress/  data/settings/
        │       └── audio/Narrator.kt  audio/SoundPlayer.kt
        ├── test/        (engine + content validation unit tests)
        └── androidTest/ (Compose UI tests with D-pad key events)
```

### 6.6 דרישות Android TV
- `<uses-feature android:name="android.software.leanback" android:required="false"/>` ו-`android.hardware.touchscreen required="false"` (כדי שהאפליקציה תופיע בחנות ל-TV).
- `intent-filter` עם `android.intent.category.LEANBACK_LAUNCHER` + `android:banner` 320×180.
- Safe area של ~5% מכל צד (Overscan).
- גרסת `minSdk`/`targetSdk` מדויקת — **דורש אימות מול התיעוד הרשמי** ודרישות Google Play העדכניות בזמן הבנייה.

---

## 7. אבטחה — Threat Model קצר

| נכס | איום | סיכון | בקרה |
|---|---|---|---|
| נתוני התקדמות ופרופילים (עלולים להיחשב מידע בריאותי עקיף) | גישה של אפליקציה אחרת / גיבוי ענן | נמוך-בינוני | אחסון פרטי של האפליקציה בלבד, `android:allowBackup="false"` / כללי `dataExtractionRules` שמחריגים נתונים, אין הרשאות מיותרות |
| הגדרות מלווה | שינוי בטעות על ידי השחקן | נמוך | PIN מגובב עם salt (לא אבטחה חזקה — מניעת שינוי בטעות בלבד), השהיה אחרי 5 ניסיונות |
| קבצי תוכן | הזרקת תוכן זדוני | נמוך | התוכן ארוז ב-APK בלבד, אין טעינה מרשת, ולידציה של סכמה בטעינה |
| שרשרת אספקה | תלות פגיעה | בינוני | מינימום תלויות (AndroidX רשמיות בלבד), Dependabot, `./gradlew dependencyCheck`/OWASP, חתימת APK עם מפתח מחוץ לריפו |
| קריסה | אובדן התקדמות / מסך תקוע | בינוני (UX) | שמירה אטומית אחרי כל צעד, מסך שגיאה ידידותי "Let's go back home", לוג מקומי ללא מידע אישי |

אין: התחברות, תשלומים, העלאת קבצים, תקשורת רשת → רוב וקטורי התקיפה הקלאסיים (XSS/SQLi/CSRF/SSRF/IDOR) לא רלוונטיים.

## 8. פרטיות

### Data Map
| סוג מידע | מקור | מטרה | גישה | אחסון | צד שלישי | שמירה | מחיקה |
|---|---|---|---|---|---|---|---|
| כינוי + אייקון פרופיל | מלווה | זיהוי פרופיל | המכשיר בלבד | DataStore פרטי | אין | עד מחיקה | "Delete profile" / הסרת אפליקציה |
| התקדמות + שימוש ברמזים | שימוש במשחק | המשך משחק, התאמת קושי, סיכום למלווה | המכשיר בלבד | DataStore פרטי | אין | 90 יום לפרטי רמזים (מתכוונן), התקדמות עד מחיקה | "Delete all data" |
| הגדרות | מלווה | התאמה אישית | המכשיר בלבד | DataStore | אין | עד מחיקה | "Reset" |
| קול/טקסט TTS | — | הקראה | — | מנוע TTS במכשיר | מנוע TTS של המכשיר (אופליין) | לא נשמר | — |

- **אין עוגיות, פיקסלים, אנליטיקה או SDK פרסום** → אין צורך בבאנר עוגיות. אם תתווסף אנליטיקה בעתיד — רק opt-in.
- המלצה: לא לבקש שם אמיתי, גיל או אבחנה — כינוי בלבד (Data Minimization).
- ⚖️ **דורש בדיקה משפטית:** האם נתוני שימוש של אדם עם דמנציה מהווים "מידע בעל רגישות מיוחדת" לפי חוק הגנת הפרטיות (תיקון 13) ו-GDPR Art. 9; Google Play Data Safety form והצהרת קטגוריית Health; מדיניות פרטיות גם ללא איסוף נתונים.

## 9. נגישות (10-foot UI + קשישים)

- יעד: WCAG 2.1 AA כבסיס, ובפועל ניגודיות **≥ 7:1 (AAA)** לטקסט; בדיקת התאמה לת"י 5568 אם מופץ בישראל (הממשק באנגלית — דורש בדיקה מול יועץ נגישות לגבי תחולה).
- גופן: **Atkinson Hyperlegible** (רישיון OFL) — גוף טקסט מינימום 28sp על TV, כותרות 40sp+, מתכוונן עד ×1.6.
- טבעת פוקוס עבה (6dp) בצבע בולט + הגדלה קלה של הרכיב — לא רק צבע.
- כל קריינות מלווה בכתובית; כל משוב מועבר גם בצליל עדין, גם בטקסט וגם בצורה (לא בצבע בלבד).
- `contentDescription` לכל רכיב, תמיכה ב-TalkBack.
- ללא הבהובים (>3 לשנייה), אפשרות Reduce Motion, מוזיקה שקטה וניתנת לכיבוי.
- רכיבים גדולים: יעדי פוקוס של מינימום ~10% רוחב מסך במיני-משחקים.
- כפתור "Help me" קבוע = כפתור הנגישות במשחק; הגדרות נגישות מלאות במסך המלווה.
- עמוד "Accessibility Statement" בתוך About + קובץ טיוטה ב-docs.

---

## 10. תוכנית עבודה בשלבים

| שלב | תוצר | קריטריון סיום |
|---|---|---|
| **0** | אפיון זה + אישור | אישור שלך על ההחלטות הפתוחות |
| **1** | שלד פרויקט Gradle, Manifest ל-TV, Theme/Design system, ניווט, Profile Picker, Home, Exit Confirm, Settings+PIN | רץ באמולטור Android TV, ניווט מלא בשלט, בדיקות UI בסיסיות |
| **2** | מנוע משחק + טעינת JSON + ולידציה + `HIDDEN_OBJECT` + `MEMORY_MATCH` + סולם רמזים | בדיקות יחידה ירוקות, פרק 1 ניתן למשחק |
| **3** | `CLUE_RECALL`, `SPELL_SEQUENCE`, `SORTING`, `PICTURE_RIDDLE` + Narrator (TTS) + צלילים | כל סוגי הפעילויות עובדים עם קריינות וכתוביות |
| **4** | תוכן מלא "The Lantern of Willowbrook" + איורים וקטוריים + התאמת קושי | תיק שלם מתחילה ועד סוף |
| **5** | מסך התקדמות למלווה, מחיקת נתונים, Reminiscence cards, About/Privacy/Accessibility | כל ה-User Stories מכוסים |
| **6** | QA, נגישות, ביצועים על ממיר זול, Release build חתום, README, טיוטות מסמכים, Checklist פרודקשן | Checklist מלא; בדיקת שימושיות עם משתמשים אמיתיים (בליווי מקצועי) |

## 11. בדיקות
- **יחידה:** מנועי פעילויות, HintScheduler, DifficultyAdapter, ולידציית כל קובצי התוכן (כל רמז מוביל לפתרון, אין פעילות בלתי פתירה).
- **UI:** Compose UI tests עם `KeyEvent` של D-pad — כל מסך נגיש בשלט, Back לא יוצא בלי אישור, הפוקוס הראשוני תמיד על הפעולה העיקרית.
- **ידני:** אמולטור Android TV (1080p + 4K), ממיר זול אמיתי (למשל 2GB RAM), TalkBack, טקסט ×1.6, Reduce Motion.
- **שימושיות:** בדיקה עם 3–5 משתמשים מקהל היעד ומלוויהם — ⚖️ אם זה במסגרת מחקר/מוסד, ייתכן שנדרש אישור ועדת הלסינקי/אתיקה.

## 12. סיכונים ונושאים לבדיקה מקצועית
1. **רגולציה רפואית:** כל טענה כמו "משפר זיכרון", "מאט דמנציה", "טיפולי" עלולה להפוך את המוצר ל-Software as a Medical Device (FDA / EU MDR / אמ"ר). ניסוח שיווקי מומלץ: *"A calm, engaging activity designed with people living with dementia in mind."* — ⚖️ דורש בדיקה משפטית.
2. **התאמה קלינית:** אישור עקרונות העיצוב ע"י מרפא/ה בעיסוק / גריאטר/ית.
3. **תוכן רגשי:** בדיקה שהסיפורים לא מעוררים מצוקה (אובדן, חיפוש בני משפחה, "לחזור הביתה").
4. **TTS:** איכות קולות משתנה בין מכשירים; ייתכן שבעתיד יידרשו הקלטות אנושיות.
5. **רישוי נכסים:** גופנים, צלילים ואיורים — רק רישיונות מתירים (OFL/CC0) או יצירה מקורית; AI-art — לבדוק תנאי שימוש.
6. **חומרה:** ממירי TV זולים מאוד — לשמור על APK קטן ואנימציות קלות.
7. **Google Play:** בדיקת TV App Quality, Data Safety, ומדיניות אפליקציות בריאות — דורש אימות מול התיעוד הרשמי.

---

## 13. החלטות פתוחות (נדרש אישורך)
1. **טכנולוגיה:** Kotlin + Compose for TV (מומלץ) או TypeScript ב-WebView?
2. **אמנות:** איורים וקטוריים שאני מייצר ל-MVP (מומלץ) / תספק אמנות / אמנות AI?
3. **קריינות:** TTS של המכשיר (מומלץ ל-MVP) / הקלטות קול אנושי?
4. **סביבת שימוש:** בית עם מלווה / בית אבות (משחק קבוצתי מול מסך אחד — משנה עיצוב) / שניהם?
5. **הפצה:** APK ידני / Google Play / הפצה מוסדית?
