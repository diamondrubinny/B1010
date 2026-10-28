# Design System — Willowbrook Mysteries (TV, 10-foot UI)

מקור האמת בקוד: `app/src/main/java/net/rubinny/willowbrook/ui/theme/`.

## קנבס
- Android TV מציג קנבס של **960×540dp** (1080p = xhdpi ×2, 4K = ×4).
- Safe area: **48dp** אופקי, **27dp** אנכי (~5% overscan).
- כל מסך עוצב כך שייכנס בלי גלילה ב-100% טקסט; ב-140%–160% המסך גולל אוטומטית אל הרכיב שבפוקוס.

## צבעים ויחסי ניגודיות (נמדדו לפי נוסחת WCAG 2.1)

| טוקן | Standard | High contrast |
|---|---|---|
| background | `#16202A` | `#000000` |
| surface | `#243444` | `#1A1A1A` |
| surfaceFocused (מילוי פוקוס) | `#F2C14E` | `#FFFF00` |
| textPrimary | `#FFF8EC` | `#FFFFFF` |
| textSecondary | `#D9D2C3` | `#FFFFFF` |
| textOnFocus | `#16202A` | `#000000` |
| focusRing | `#FFFFFF` | `#FFFFFF` |
| danger (הודעות, לא אדום מבהיל) | `#FFB4A8` | `#FFB4A8` |

| צמד | יחס | יעד |
|---|---|---|
| textPrimary / background | 15.6:1 | AAA ✅ |
| textPrimary / surface | 12.1:1 | AAA ✅ |
| textSecondary / background | 10.9:1 | AAA ✅ |
| textSecondary / surface | 8.5:1 | AAA ✅ |
| textOnFocus / gold | 9.8:1 | AAA ✅ |
| danger / surface | 7.5:1 | AAA ✅ |
| gold focus fill / background (רכיב לא-טקסטואלי) | 9.8:1 | ≥3:1 ✅ |
| High contrast: text / background | 21:1 | AAA ✅ |
| High contrast: text on yellow | 19.6:1 | AAA ✅ |

## פוקוס — תמיד בשלוש דרכים, לא רק בצבע
1. מילוי זהב (או צהוב ב-High contrast).
2. מסגרת לבנה עבה 6dp.
3. הגדלה של 5% (מבוטלת ב-"Less movement on screen").

## טיפוגרפיה — Atkinson Hyperlegible (SIL OFL 1.1)
| סגנון | גודל / גובה שורה | משקל |
|---|---|---|
| display | 56 / 66sp | Bold |
| title | 40 / 50sp | Bold |
| button | 34 / 42sp | Bold |
| body | 30 / 42sp | Regular |
| label | 28 / 36sp | Regular |

**הגדלת טקסט של המלווה (100%–160%) מוחלת לינארית על הגדלים**, ולא דרך `fontScale`:
באנדרואיד 14 ומעלה, הגדלת גופן היא לא-לינארית, וטקסט של 30sp ומעלה גדל רק בכ-5% בהגדרה של 160%.
הגדרת גודל הגופן של מערכת ההפעלה עדיין חלה בנוסף. מכוסה בבדיקה `TextScaleTest`.

## רכיבים
| רכיב | שימוש | הערות נגישות |
|---|---|---|
| `BigButton` | כל פעולה | גובה מינ' 96dp, `contentDescription` אופציונלי |
| `ToggleRow` | הפעלה/כיבוי | "On"/"Off" נכתב ונאמר (`stateDescription`) |
| `StepperRow` | ערך מספרי | כפתורי Less/More; בקצה הטווח הכפתור מעומעם אך **נשאר בר-פוקוס** כדי לא לאבד פוקוס |
| `ConfirmDialog` | אישור פעולה | הפוקוס ההתחלתי תמיד על האפשרות **הבטוחה** |
| `PinPad` | PIN מלווה | תומך גם במקשי ספרות בשלט; הספרות לא מוצגות ולא מוקראות |
| `ReadableBlock` | טקסט ארוך | כל פסקה מקבלת פוקוס כדי שאפשר יהיה לגלול בשלט |
| `AvatarBadge` | זיהוי שחקן | טבעת כהה כדי שיישאר גלוי על רקע זהב |

## כללי ניווט בשלט
- בכל מסך יש פוקוס התחלתי על הפעולה העיקרית.
- הפעולה הראשית תמיד משמאל או למעלה.
- Back במסך הראשי לא יוצא מהמשחק בלי שאלה, ו-"Keep playing" נבחר מראש.
- בשדה טקסט, למעלה/למטה תמיד יוצאים מהשדה (כדי שלא אפשר יהיה להיתקע בו), ושמאלה/ימינה מזיזים את הסמן.
