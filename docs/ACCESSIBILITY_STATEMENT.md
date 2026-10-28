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
- Hints never rely on colour alone: a glowing frame, a spoken-style message, and finally the answer is selected for you.

## Known limitations
- Narration (spoken story) is not available yet; all story text is shown on screen.
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
