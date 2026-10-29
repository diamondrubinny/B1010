# Prompts לאיורי AI — כל התיק (פרקים 0–4)

כל prompt מתחיל ב-**prompt הבסיס** מתוך [`ASSET_PIPELINE.md`](ASSET_PIPELINE.md), ובמקום `<SUBJECT>` מכניסים את הנושא מהטבלאות.
את הקבצים המוכנים שמים בתיקייה `app/src/main/res/drawable-nodpi/` בשם המדויק שבטבלה.
המשחק מציג אותם אוטומטית במקום ה-placeholder, בלי שינוי בקוד.

> לפני הכנסה של כל תמונה, עוברים על ה-Checklist ב-ASSET_PIPELINE (בלי טקסט בתמונה, שום דבר מפחיד, החפץ ברור, תיעוד רישוי).

## סצנות (1920×1080, WebP)
| קובץ | `<SUBJECT>` |
|---|---|
| `scene_cottage.webp` | the cosy sitting room of a small English cottage, armchair with a knitted blanket, fireplace with a gentle fire, bookshelf, window with autumn trees outside, a ginger cat basket by the fire, wide view with calm empty space in the centre |
| `scene_garden.webp` | an English cottage garden in autumn, flower beds, a wooden wheelbarrow, a neat pile of raked leaves, an apple tree, a low stone wall, soft golden afternoon light, calm empty space in the centre |
| `scene_library.webp` | inside a small 1950s village library, tall wooden bookshelves, a reading table with a green lamp, a large window, a tall clock, warm quiet light, calm empty space in the centre |
| `scene_square.webp` | an English village square at dusk on fair night, bunting between old houses, a small bandstand, stalls with warm lights, a large old iron lantern in the middle, happy and calm, calm empty space in the centre |
| `scene_bakery.webp` | the inside of a small 1950s village bakery, wooden counter, shelves of bread loaves, a large old oven glowing warmly, flour on a wooden table, jars on shelves, autumn light through the window, calm empty space in the centre |

הרקע מוצג מתחת לשכבה כהה (72%) כדי לשמור על קריאות הטקסט, ולכן כדאי לבחור תמונה עם אזורים רגועים ובלי פרטים קטנים במרכז.

## דמויות (768×1024, רקע שקוף, WebP lossless)
| קובץ | `<SUBJECT>` |
|---|---|
| `char_marmalade.webp` | a friendly ginger tabby cat sitting upright, gentle smile, bright kind eyes, soft fur, looking at the viewer, full body, transparent background |
| `char_hollis.webp` | Mr. Hollis, a gentle gardener in his seventies, flat cap, tweed waistcoat over a rolled-sleeve shirt, kind wrinkled smile, holding a small trowel, half body, transparent background |
| `char_penrose.webp` | Miss Penrose, a kind librarian in her sixties, neat silver hair, cardigan and pearls, reading glasses on a chain, gentle smile, holding a book, half body, transparent background |
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
| `obj_pear.webp` | ripe green pear with a leaf |
| `obj_sunflower.webp` | tall yellow sunflower |
| `obj_tulip.webp` | single pink tulip |
| `obj_rose.webp` | single red rose |
| `obj_ladybird.webp` | friendly red ladybird with black spots (not an insect close-up — soft and simple) |
| `obj_mushroom.webp` | small brown woodland mushroom |
| `obj_chestnut.webp` | shiny brown chestnut |
| `obj_bee.webp` | friendly round bumblebee (soft and simple) |
| `obj_bird.webp` | small robin |
| `obj_maple_leaf.webp` | orange autumn leaf |
| `obj_magnifier.webp` | brass magnifying glass |
| `obj_candle.webp` | white candle in a brass holder with a small calm flame |
| `obj_key.webp` | old golden key |
| `obj_globe.webp` | small desk globe |
| `obj_pencil.webp` | yellow pencil |
| `obj_scissors.webp` | pair of scissors with red handles |
| `obj_newspaper.webp` | folded newspaper (no readable text) |
| `obj_map.webp` | folded old map (no readable text) |
| `obj_top_hat.webp` | black hat with a ribbon |
| `obj_umbrella.webp` | closed black umbrella |
| `obj_boot.webp` | brown leather boot |
| `obj_books.webp` | small pile of hardback books |
| `obj_pen.webp` | fountain pen |
| `obj_letter.webp` | sealed envelope (no readable text) |
| `obj_sea.webp` | calm blue sea with a little sailing boat |
| `obj_mountains.webp` | green hills and mountains |
| `obj_stars.webp` | night sky with a few bright stars |
| `obj_bramble.webp` | head-and-shoulders portrait of Mrs. Bramble (same look as char_bramble), round frame |
| `obj_hollis.webp` | head-and-shoulders portrait of Mr. Hollis (same look as char_hollis), round frame |
| `obj_penrose.webp` | head-and-shoulders portrait of Miss Penrose (same look as char_penrose), round frame |

## כללים לחפצים
- כל חפץ צריך להיות מזוהה גם בגודל קטן (כ-56dp על המסך). מומלץ לבדוק בהקטנה ל-128px.
- החפצים בכל סצנה צריכים להיות שונים זה מזה בצורה ובצבע, כדי לא לבלבל.
- שומרים על סגנון אחיד לכל החפצים: אותה תאורה, אותו כיוון צל, אותה רמת פירוט.

## קריינות
סקריפט הקול נמצא ב-`docs/voice/lantern.csv`. מקליטים כל שורה לקובץ `app/src/main/assets/voice/<id>.ogg`.
