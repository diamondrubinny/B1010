# Prompts לאיורי AI — פרקים 0–1

כל prompt מתחיל ב-**prompt הבסיס** מתוך [`ASSET_PIPELINE.md`](ASSET_PIPELINE.md), ובמקום `<SUBJECT>` מכניסים את הנושא מהטבלאות.
את הקבצים המוכנים שמים בתיקייה `app/src/main/res/drawable-nodpi/` בשם המדויק שבטבלה.
המשחק מציג אותם אוטומטית במקום ה-placeholder, בלי שינוי בקוד.

> לפני הכנסה של כל תמונה, עוברים על ה-Checklist ב-ASSET_PIPELINE (בלי טקסט בתמונה, שום דבר מפחיד, החפץ ברור, תיעוד רישוי).

## סצנות (1920×1080, WebP)
| קובץ | `<SUBJECT>` |
|---|---|
| `scene_cottage.webp` | the cosy sitting room of a small English cottage, armchair with a knitted blanket, fireplace with a gentle fire, bookshelf, window with autumn trees outside, a ginger cat basket by the fire, wide view with calm empty space in the centre |
| `scene_bakery.webp` | the inside of a small 1950s village bakery, wooden counter, shelves of bread loaves, a large old oven glowing warmly, flour on a wooden table, jars on shelves, autumn light through the window, calm empty space in the centre |

הרקע מוצג מתחת לשכבה כהה (72%) כדי לשמור על קריאות הטקסט, ולכן כדאי לבחור תמונה עם אזורים רגועים ובלי פרטים קטנים במרכז.

## דמויות (768×1024, רקע שקוף, WebP lossless)
| קובץ | `<SUBJECT>` |
|---|---|
| `char_marmalade.webp` | a friendly ginger tabby cat sitting upright, gentle smile, bright kind eyes, soft fur, looking at the viewer, full body, transparent background |
| `char_bramble.webp` | Mrs. Bramble, a warm and kind baker in her sixties, rosy cheeks, grey hair in a bun, flour-dusted apron over a 1950s floral dress, holding a wooden spoon, friendly smile, half body, transparent background |

## חפצים (512×512, רקע שקוף, WebP lossless)
`<SUBJECT>` לכל חפץ: `a single <object>, centred, simple and instantly recognisable, soft shadow, transparent background`

| קובץ | `<object>` |
|---|---|
| `obj_glasses.webp` | pair of round reading glasses with thin gold frames |
| `obj_teacup.webp` | white china teacup on a saucer with a small flower pattern |
| `obj_clock.webp` | wooden mantel clock with a cream clock face |
| `obj_book.webp` | open hardback book |
| `obj_honey.webp` | round golden honey pot with a wooden honey dipper |
| `obj_bread.webp` | crusty brown loaf of bread |
| `obj_strawberries.webp` | small bowl of fresh red strawberries |
| `obj_milk.webp` | glass of milk |
| `obj_egg.webp` | brown hen's egg |
| `obj_cheese.webp` | wedge of yellow cheese |
| `obj_biscuit.webp` | round golden biscuit |
| `obj_cake.webp` | slice of Victoria sponge cake with jam and cream |
| `obj_croissant.webp` | golden croissant |
| `obj_apple.webp` | shiny red apple with a leaf |
| `obj_lemon.webp` | bright yellow lemon with a leaf |
| `obj_cherries.webp` | pair of red cherries on a stem |

## כללים לחפצים
- כל חפץ צריך להיות מזוהה גם בגודל קטן (כ-56dp על המסך). מומלץ לבדוק בהקטנה ל-128px.
- החפצים בכל סצנה צריכים להיות שונים זה מזה בצורה ובצבע, כדי לא לבלבל.
- שומרים על סגנון אחיד לכל החפצים: אותה תאורה, אותו כיוון צל, אותה רמת פירוט.

## קריינות (שלב 3)
סקריפט הקול (`voice_script.csv`) ייווצר אוטומטית מהתוכן בשלב 3.
