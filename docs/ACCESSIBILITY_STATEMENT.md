# Accessibility Statement — Willowbrook Mysteries

> **DRAFT — must be reviewed by an accessibility consultant before publication.**
> If the app is distributed in Israel, check applicability of the Equal Rights for Persons with Disabilities
> regulations and Israeli Standard 5568. Placeholders in `[brackets]` must be completed.

_Last updated: [date] · App version 0.1.0_

## Our commitment
Willowbrook Mysteries is designed for older adults and people living with dementia. Accessibility is part of how the game is built, not an add-on.

## Standard
We aim to meet **WCAG 2.1 level AA**, and use **AAA contrast (7:1 or higher)** for all text.

## What we have done
- Everything works with a TV remote: arrow keys, OK and Back. No pointer or touch needed.
- A clear focus highlight shown three ways: colour fill, thick white outline and slight enlargement.
- Every screen starts with the main action already selected.
- Back on the main screen never closes the game without asking; "Keep playing" is pre-selected.
- Atkinson Hyperlegible font, designed for low vision. Body text at least 28sp.
- Text size adjustable 100%–160%; Extra-strong contrast mode (21:1).
- "Less movement on screen" option; no flashing content.
- Screen-reader (TalkBack) labels on all controls; states such as "On/Off" are spoken.
- No time limits anywhere in the game.
- No wrong-answer sounds or penalties; help is always available with "Help me", and Marmalade always shows what is left to do.
- The story is read aloud, with "Listen again" on every story screen. Captions are always available, and can only be turned off while narration is on.
- When TalkBack is on, Marmalade's messages are announced by TalkBack only, so nothing is heard twice.
- "Let's chat" cards offer open, gentle conversation starters after each chapter, shown and read aloud.
- Sounds are soft bells with gentle starts; there is no sound for a wrong answer.
- Gentle background music lowers automatically whenever someone speaks, has its own volume setting, and can be turned off.
- Narration is paced slowly (at most about 2.7 words per second, with pauses between sentences), and the speaking-speed setting also applies to recordings.
- Lantern Beads: every bead has a shape (star, moon, leaf, flower) as well as a colour; there is no wrong move, no timer and no moving string; places on the string are described for TalkBack ("Between the moon and the star").
- Living scenes and objects placed on them; with "Less movement on screen" the scene is a still picture.
- Hints never rely on colour alone: a glowing frame, a spoken-style message, and finally the answer is selected for you.

## Known limitations
- Narration is recorded with synthetic voices; a few sentences may sound less natural than a human reader.
- Typing a player name needs the TV's on-screen keyboard, which is controlled by the TV, not by us.
- Automated tests run in a simulated TV environment; testing with real users and TalkBack on physical devices is still pending.

## Feedback and contact
Accessibility coordinator: [name], [email], [phone].
We aim to respond within [5] working days.

## Testing performed
- Automated D-pad navigation tests on every stage-1 screen (Robolectric).
- Contrast ratios calculated for every colour pair (see `docs/DESIGN_SYSTEM.md`).
- Android Lint accessibility checks: no issues.
- Pending: manual TalkBack test on a physical TV, test at 160% text, usability test with older adults.
