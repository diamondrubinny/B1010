"""Original character drawings (768 x 1024, half-body) and round portraits (512 x 512) for "Who…?" questions.

Faces follow one gentle recipe — soft oval face, small kind eyes with a highlight, rosy cheeks,
a warm closed smile and soft smile lines — so every character feels friendly and respectful.
"""
from objects import O, S, THIN

SKIN = "#f2c9a5"


def face(skin=SKIN, brows="#8a6a52", smile_lines=True, wide_smile=False):
    """Face in local coordinates, centred on (0, 0); oval about 300 x 350."""
    lines = ""
    if smile_lines:
        lines = (f'<path d="M-98 -18 L-114 -26 M-98 -6 L-116 -4 M98 -18 L114 -26 M98 -6 L116 -4" '
                 f'stroke="{O}" stroke-width="5" stroke-linecap="round" opacity="0.45"/>')
    mouth = ('M-50 78 Q0 128 50 78' if wide_smile else 'M-42 82 Q0 115 42 82')
    return f'''
      <ellipse cx="-150" cy="15" rx="26" ry="42" fill="{skin}" {S}/>
      <ellipse cx="150" cy="15" rx="26" ry="42" fill="{skin}" {S}/>
      <ellipse cx="0" cy="0" rx="150" ry="175" fill="{skin}" {S}/>
      <circle cx="-88" cy="48" r="30" fill="#ee8f8f" opacity="0.45"/>
      <circle cx="88" cy="48" r="30" fill="#ee8f8f" opacity="0.45"/>
      <path d="M-82 -58 Q-55 -76 -28 -60 M28 -60 Q55 -76 82 -58" fill="none" stroke="{brows}" stroke-width="11" stroke-linecap="round"/>
      <ellipse cx="-55" cy="-12" rx="14" ry="18" fill="{O}"/><circle cx="-50" cy="-18" r="5" fill="#fff"/>
      <ellipse cx="55" cy="-12" rx="14" ry="18" fill="{O}"/><circle cx="60" cy="-18" r="5" fill="#fff"/>
      {lines}
      <path d="M-6 22 Q-14 50 6 52" fill="none" stroke="{O}" stroke-width="7" stroke-linecap="round" opacity="0.7"/>
      <path d="{mouth}" fill="none" stroke="{O}" stroke-width="9" stroke-linecap="round"/>'''


def marmalade():
    stripes = "".join(
        f'<path d="{d}" fill="none" stroke="#c96f22" stroke-width="18" stroke-linecap="round"/>'
        for d in ["M384 175 L384 225", "M340 182 L350 228", "M428 182 L418 228",
                  "M200 560 Q240 580 245 620", "M190 650 Q235 665 240 705", "M570 560 Q530 580 525 620",
                  "M580 650 Q535 665 530 705"])
    whiskers = (f'<path d="M260 345 L170 330 M260 362 L172 372 M508 345 L598 330 M508 362 L596 372" '
                f'stroke="{O}" stroke-width="5" stroke-linecap="round" opacity="0.7"/>')
    return f'''
      <ellipse cx="384" cy="975" rx="250" ry="28" fill="{O}" opacity="0.16"/>
      <path d="M560 920 C 700 930 720 760 640 700 C 610 680 600 720 630 735 C 680 770 650 880 560 880 Z" fill="#f0a04b" {S}/>
      <path d="M180 960 C 150 760 200 520 384 500 C 568 520 618 760 588 960 Z" fill="#f0a04b" {S}/>
      <path d="M300 960 C 290 820 310 600 384 590 C 458 600 478 820 468 960 Z" fill="#fff3e0"/>
      <path d="M180 960 C 150 760 200 520 384 500 C 568 520 618 760 588 960 Z" fill="none" {S}/>
      {stripes}
      <ellipse cx="300" cy="955" rx="70" ry="40" fill="#fff3e0" {S}/><ellipse cx="468" cy="955" rx="70" ry="40" fill="#fff3e0" {S}/>
      <path d="M200 200 L215 60 L320 150 Z M568 200 L553 60 L448 150 Z" fill="#f0a04b" {S}/>
      <path d="M225 165 L232 100 L285 148 Z M543 165 L536 100 L483 148 Z" fill="#f4a9b5"/>
      <ellipse cx="384" cy="320" rx="215" ry="185" fill="#f0a04b" {S}/>
      <ellipse cx="384" cy="400" rx="110" ry="80" fill="#fff3e0"/>
      <ellipse cx="384" cy="320" rx="215" ry="185" fill="none" {S}/>
      {"".join(f'<path d="{d}" fill="none" stroke="#c96f22" stroke-width="16" stroke-linecap="round"/>' for d in ["M384 150 L384 200", "M335 158 L345 205", "M433 158 L423 205", "M185 300 L230 310", "M188 345 L232 342", "M583 300 L538 310", "M580 345 L536 342"])}
      <ellipse cx="310" cy="300" rx="30" ry="36" fill="{O}"/><circle cx="320" cy="288" r="10" fill="#fff"/>
      <ellipse cx="458" cy="300" rx="30" ry="36" fill="{O}"/><circle cx="468" cy="288" r="10" fill="#fff"/>
      <path d="M368 360 L400 360 L384 380 Z" fill="#e5788a" {THIN}/>
      <path d="M384 380 Q384 400 360 405 M384 380 Q384 400 408 405" fill="none" stroke="{O}" stroke-width="7" stroke-linecap="round"/>
      {whiskers}
      <circle cx="300" cy="370" r="24" fill="#ee8f8f" opacity="0.35"/><circle cx="468" cy="370" r="24" fill="#ee8f8f" opacity="0.35"/>'''


def bramble_head():
    # Bun behind, then the face, then a soft swept fringe over the top of the forehead.
    return f'''
      <circle cx="0" cy="-205" r="70" fill="#cfcfd6" {S}/>
      <path d="M-165 0 Q-175 -195 0 -200 Q175 -195 165 0 Z" fill="#cfcfd6" {S}/>
      {face(brows="#9a9aa3")}
      <path d="M-152 -20 Q-160 -185 0 -188 Q160 -185 152 -20 Q140 -95 80 -105 Q40 -80 0 -102 Q-40 -125 -90 -100 Q-140 -95 -152 -20 Z" fill="#cfcfd6" {S}/>
      <path d="M-110 -130 Q-60 -165 10 -160" fill="none" stroke="#e6e6ec" stroke-width="12" stroke-linecap="round"/>'''


def bramble():
    flowers = "".join(f'<circle cx="{x}" cy="{y}" r="9" fill="#f6e3ea"/><circle cx="{x}" cy="{y}" r="4" fill="#e8798f"/>'
                      for x, y in [(150, 760), (230, 860), (140, 940), (600, 770), (640, 880), (560, 960), (300, 990), (470, 1000)])
    flour = "".join(f'<circle cx="{x}" cy="{y}" r="5" fill="#ffffff" opacity="0.8"/>' for x, y in [(330, 820), (420, 850), (380, 910), (450, 950), (300, 900)])
    return f'''
      <path d="M90 1024 Q90 700 384 690 Q678 700 678 1024 Z" fill="#7d9cc0" {S}/>
      {flowers}
      <path d="M260 700 L250 1024 L518 1024 L508 700 Q384 740 260 700 Z" fill="#fffaf0" {S}/>
      <rect x="320" y="880" width="128" height="80" rx="14" fill="#f3ead8" {S}/>
      {flour}
      <rect x="344" y="590" width="80" height="110" fill="{SKIN}" {S}/>
      <g transform="translate(384 420) scale(1.12)">{bramble_head()}</g>
      <path d="M640 1024 L660 760" stroke="{O}" stroke-width="30" stroke-linecap="round"/>
      <path d="M640 1024 L660 760" stroke="#c48a4f" stroke-width="16" stroke-linecap="round"/>
      <ellipse cx="664" cy="720" rx="34" ry="50" fill="#c48a4f" {S}/>
      <circle cx="640" cy="905" r="42" fill="{SKIN}" {S}/>'''


def hollis_head():
    return f'''
      {face(brows="#e6e6e6", wide_smile=True)}
      <path d="M-152 -10 Q-160 60 -135 95 L-125 20 Z M152 -10 Q160 60 135 95 L125 20 Z" fill="#d9d9de" {THIN}/>
      <path d="M-175 -85 Q-170 -215 0 -220 Q170 -215 175 -85 Q90 -110 0 -110 Q-90 -110 -175 -85 Z" fill="#8a6a4a" {S}/>
      <path d="M-175 -85 Q0 -120 215 -60 Q200 -30 150 -50 Q0 -90 -170 -60 Z" fill="#7a5b3c" {S}/>
      <path d="M-120 -170 L110 -175 M-150 -125 L140 -130" stroke="#6b4e33" stroke-width="5" opacity="0.6"/>'''


def hollis():
    return f'''
      <path d="M80 1024 Q80 700 384 690 Q688 700 688 1024 Z" fill="#efe6d2" {S}/>
      <path d="M230 700 L210 1024 L558 1024 L538 700 Q384 760 230 700 Z" fill="#6b7a45" {S}/>
      <path d="M384 720 L384 1024" stroke="{O}" stroke-width="7"/>
      {"".join(f'<circle cx="405" cy="{y}" r="11" fill="#d9ab3c" {THIN}/>' for y in (790, 870, 950))}
      <path d="M90 900 Q100 860 170 850 L200 940 Q140 955 95 950 Z" fill="#e8dcc0" {S}/>
      <rect x="344" y="590" width="80" height="110" fill="{SKIN}" {S}/>
      <path d="M330 690 L384 730 L438 690" fill="#efe6d2" {S}/>
      <g transform="translate(384 420) scale(1.12)">{hollis_head()}</g>
      <circle cx="612" cy="900" r="44" fill="{SKIN}" {S}/>
      <path d="M612 860 L640 700" stroke="{O}" stroke-width="22" stroke-linecap="round"/>
      <path d="M612 860 L640 700" stroke="#9a6237" stroke-width="10" stroke-linecap="round"/>
      <path d="M612 700 Q640 610 668 700 L655 760 L625 760 Z" fill="#c9ced6" {S}/>'''


def penrose_head():
    # Neat silver bob: back layer down to the jaw, then a side-parted fringe over the forehead.
    return f'''
      <path d="M-178 70 Q-205 -205 0 -210 Q205 -205 178 70 Q170 120 125 115 L-125 115 Q-170 120 -178 70 Z" fill="#d8d8e0" {S}/>
      {face(brows="#a9a9b3")}
      <path d="M-155 10 Q-165 -190 0 -192 Q165 -190 155 10 Q150 -70 110 -95 Q20 -70 -40 -120 Q-100 -90 -125 -60 Q-150 -40 -155 10 Z" fill="#d8d8e0" {S}/>
      <path d="M-40 -120 Q10 -160 90 -150" fill="none" stroke="#ececf2" stroke-width="12" stroke-linecap="round"/>
      <circle cx="-55" cy="-12" r="42" fill="none" stroke="#c99a2e" stroke-width="7"/>
      <circle cx="55" cy="-12" r="42" fill="none" stroke="#c99a2e" stroke-width="7"/>
      <path d="M-13 -14 Q0 -24 13 -14" fill="none" stroke="#c99a2e" stroke-width="7"/>
      <path d="M-97 -10 Q-150 120 -110 230 M97 -10 Q150 120 110 230" fill="none" stroke="#c99a2e" stroke-width="4" stroke-dasharray="3 9"/>'''


def penrose():
    pearls = "".join(f'<circle cx="{384 + 105 * __import__("math").cos(a / 10):.0f}" cy="{680 + 50 * __import__("math").sin(a / 10):.0f}" r="11" fill="#fbf7ef" {THIN}/>' for a in range(3, 29, 2))
    return f'''
      <path d="M85 1024 Q85 700 384 690 Q683 700 683 1024 Z" fill="#8e5a7a" {S}/>
      <path d="M300 700 L384 800 L468 700 Q384 720 300 700 Z" fill="#fbf7ef" {S}/>
      <path d="M384 800 L384 1024" stroke="{O}" stroke-width="7"/>
      {"".join(f'<circle cx="405" cy="{y}" r="10" fill="#e6d3e0" {THIN}/>' for y in (850, 920, 990))}
      <rect x="344" y="590" width="80" height="110" fill="{SKIN}" {S}/>
      {pearls}
      <g transform="translate(384 420) scale(1.12)">{penrose_head()}</g>
      <g transform="rotate(-8 384 900)">
        <rect x="250" y="830" width="268" height="190" rx="10" fill="#3f6fa8" {S}/>
        <rect x="268" y="850" width="232" height="16" rx="6" fill="#f4ecd8"/>
        <circle cx="262" cy="930" r="40" fill="{SKIN}" {S}/><circle cx="506" cy="930" r="40" fill="{SKIN}" {S}/>
      </g>'''


def portrait(head_svg, bg):
    return (f'<defs><clipPath id="p"><circle cx="256" cy="256" r="230"/></clipPath></defs>'
            f'<circle cx="256" cy="256" r="230" fill="{bg}"/>'
            f'<g clip-path="url(#p)"><path d="M40 560 Q40 410 256 400 Q472 410 472 560 Z" fill="#00000020"/>'
            f'<g transform="translate(256 290) scale(0.95)">{head_svg}</g></g>'
            f'<circle cx="256" cy="256" r="230" fill="none" {S}/>')


CHARACTERS = {
    "marmalade": marmalade(),
    "bramble": bramble(),
    "hollis": hollis(),
    "penrose": penrose(),
}

PORTRAITS = {
    "bramble": portrait(bramble_head(), "#cfe0f1"),
    "hollis": portrait(hollis_head(), "#dcebc8"),
    "penrose": portrait(penrose_head(), "#ead7e4"),
}
