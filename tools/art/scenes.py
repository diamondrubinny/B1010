"""Original background scenes (1920 x 1080) and the Android TV launcher banner.

Scenes sit under a dark scrim in the game, so they are calm and soft: big shapes, warm light,
an uncluttered middle (where the text and tiles go), and nothing that could look frightening.
"""
import math
import random

from objects import O, S, THIN, sunflower_head

W, H = 1920, 1080


def grad(id_, top, bottom, x2="0", y2="1"):
    return (f'<linearGradient id="{id_}" x1="0" y1="0" x2="{x2}" y2="{y2}">'
            f'<stop offset="0" stop-color="{top}"/><stop offset="1" stop-color="{bottom}"/></linearGradient>')


def glow(id_, color, opacity=0.8):
    return (f'<radialGradient id="{id_}"><stop offset="0" stop-color="{color}" stop-opacity="{opacity}"/>'
            f'<stop offset="1" stop-color="{color}" stop-opacity="0"/></radialGradient>')


def window(x, y, w, h, outside):
    return (f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="12" fill="{outside}" {S}/>'
            f'<path d="M{x + w / 2} {y} L{x + w / 2} {y + h} M{x} {y + h / 2} L{x + w} {y + h / 2}" stroke="#7a4a28" stroke-width="16"/>'
            f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="12" fill="none" stroke="#7a4a28" stroke-width="18"/>')


def tree(x, y, s=1.0, leaves="#e8a33c"):
    return (f'<rect x="{x - 18 * s}" y="{y - 40 * s}" width="{36 * s}" height="{160 * s}" fill="#7a4a28"/>'
            + "".join(f'<circle cx="{x + dx * s}" cy="{y - 90 * s + dy * s}" r="{r * s}" fill="{c}"/>'
                      for dx, dy, r, c in [(0, -40, 90, leaves), (-70, 10, 70, "#d9862c"), (70, 10, 70, "#f0b84a"), (0, 30, 75, leaves)]))


def books_row(x0, y, width, height, seed):
    rnd = random.Random(seed)
    colors = ["#c0503f", "#3f6fa8", "#5e9a54", "#e2b23c", "#8e3b5e", "#7a4a28", "#4f8a8b"]
    out, x = [], x0
    while x < x0 + width - 30:
        w = rnd.randint(26, 46)
        h = rnd.randint(int(height * 0.7), height)
        out.append(f'<rect x="{x}" y="{y + height - h}" width="{w}" height="{h}" rx="4" fill="{rnd.choice(colors)}" stroke="{O}" stroke-width="4"/>')
        out.append(f'<rect x="{x + 6}" y="{y + height - h + 14}" width="{w - 12}" height="6" fill="#f4ecd8" opacity="0.7"/>')
        x += w + 4
    return "".join(out)


def cottage():
    return f'''<defs>{grad("wall", "#e9d8b8", "#d8c09a")}{grad("floor", "#9a6237", "#7a4a28")}{glow("fire", "#ffb347", 0.9)}</defs>
      <rect width="{W}" height="{H}" fill="url(#wall)"/>
      {"".join(f'<rect x="{x}" y="0" width="40" height="780" fill="#dcc7a2" opacity="0.6"/>' for x in range(0, W, 120))}
      <rect y="780" width="{W}" height="300" fill="url(#floor)"/>
      {"".join(f'<path d="M0 {y} L{W} {y}" stroke="#6e4426" stroke-width="5" opacity="0.5"/>' for y in (850, 940, 1030))}
      {window(1380, 180, 360, 420, "#bfe3f5")}
      <g clip-path="inset(0)">{tree(1480, 520, 0.9)}{tree(1660, 540, 0.8, "#f0b84a")}</g>
      {window(1380, 180, 360, 420, "none")}
      <rect x="1350" y="590" width="420" height="30" rx="8" fill="#9a6237" {S}/>
      <circle cx="500" cy="640" r="320" fill="url(#fire)"/>
      <path d="M260 780 L260 380 L740 380 L740 780 Z" fill="#a9563d" {S}/>
      {"".join(f'<path d="M260 {y} L740 {y}" stroke="#8c4430" stroke-width="5"/>' for y in range(420, 780, 50))}
      <rect x="220" y="340" width="560" height="60" rx="10" fill="#7a4a28" {S}/>
      <path d="M350 780 L350 560 Q500 470 650 560 L650 780 Z" fill="#3a2a22" {S}/>
      <path d="M430 760 Q450 640 500 600 Q540 650 560 760 Z" fill="#f6a623"/>
      <path d="M465 760 Q480 680 505 650 Q525 690 535 760 Z" fill="#ffe08a"/>
      <rect x="330" y="750" width="340" height="30" rx="8" fill="#5a3a24"/>
      <rect x="380" y="270" width="70" height="70" rx="8" fill="#e2b23c" {THIN}/>
      <circle cx="600" cy="300" r="36" fill="#fbf0d9" {THIN}/>
      <path d="M940 900 Q940 620 1060 600 L1240 600 Q1290 620 1290 760 L1290 900 Z" fill="#8e5a7a" {S}/>
      <rect x="900" y="700" width="80" height="210" rx="30" fill="#7a4766" {S}/>
      <rect x="1250" y="700" width="80" height="210" rx="30" fill="#7a4766" {S}/>
      <path d="M1000 760 L1240 760 L1260 900 L980 900 Z" fill="#9c6a8a" {S}/>
      <path d="M1010 640 Q1100 600 1150 690 L1110 760 Z" fill="#f2c14e" opacity="0.9" {THIN}/>
      <ellipse cx="760" cy="960" rx="420" ry="70" fill="#b5473a" opacity="0.8"/>
      <ellipse cx="760" cy="960" rx="340" ry="50" fill="#c96a54" opacity="0.8"/>'''


def bakery():
    return f'''<defs>{grad("bwall", "#f3e3c3", "#e6cfa6")}{glow("oven", "#ff9f43", 0.85)}</defs>
      <rect width="{W}" height="{H}" fill="url(#bwall)"/>
      {"".join(f'<rect x="{x}" y="{y}" width="118" height="58" fill="none" stroke="#e0c9a0" stroke-width="5"/>' for x in range(0, W, 120) for y in range(0, 700, 60))}
      <circle cx="420" cy="560" r="330" fill="url(#oven)"/>
      <path d="M150 820 L150 400 Q150 230 420 220 Q690 230 690 400 L690 820 Z" fill="#b6714a" {S}/>
      {"".join(f'<path d="M150 {y} L690 {y}" stroke="#9a5a38" stroke-width="5"/>' for y in range(300, 820, 60))}
      <path d="M260 820 L260 520 Q260 430 420 425 Q580 430 580 520 L580 820 Z" fill="#3a2a22" {S}/>
      <path d="M300 820 Q330 700 420 660 Q510 700 540 820 Z" fill="#f6a623" opacity="0.85"/>
      {"".join(f'<rect x="900" y="{y}" width="900" height="26" rx="8" fill="#9a6237" {S}/>' for y in (230, 430))}
      {"".join(f'<path d="M{x} {y} Q{x} {y - 70} {x + 75} {y - 75} Q{x + 150} {y - 70} {x + 150} {y} Z" fill="#c98a4b" {THIN}/>' for x in range(930, 1750, 190) for y in (230, 430))}
      <rect x="0" y="780" width="{W}" height="300" fill="#7a4a28"/>
      <rect x="760" y="640" width="1160" height="160" rx="14" fill="#9a6237" {S}/>
      <rect x="760" y="640" width="1160" height="40" rx="14" fill="#c48a4f" {S}/>
      {"".join(f'<circle cx="{x}" cy="610" r="34" fill="{c}" {THIN}/>' for x, c in [(900, "#f2a93b"), (1000, "#e6b877"), (1600, "#d93b30")])}
      <ellipse cx="1300" cy="618" rx="120" ry="26" fill="#fbf7ef" {THIN}/>'''


def garden():
    flowers = "".join(
        f'<g transform="translate({x} {y})"><path d="M0 0 L0 60" stroke="#4f8a3a" stroke-width="8"/>'
        f'<circle cx="0" cy="-6" r="22" fill="{c}" {THIN}/><circle cx="0" cy="-6" r="8" fill="#f6c431"/></g>'
        for x, y, c in [(160 + i * 95, 860 + (i % 3) * 25, ["#e8608a", "#f4a7c0", "#c62f3a", "#ffffff", "#c3a8e0"][i % 5]) for i in range(8)]
        + [(1300 + i * 90, 880 + (i % 2) * 30, ["#e8608a", "#ffffff", "#f6c431", "#c62f3a"][i % 4]) for i in range(7)])
    return f'''<defs>{grad("sky", "#bfe3f5", "#f7e2b5")}</defs>
      <rect width="{W}" height="{H}" fill="url(#sky)"/>
      <circle cx="1600" cy="180" r="90" fill="#ffd166" opacity="0.9"/>
      <path d="M0 620 Q480 520 960 600 Q1440 680 1920 560 L1920 1080 L0 1080 Z" fill="#9cc77a"/>
      <path d="M0 760 Q600 680 1200 760 Q1600 810 1920 740 L1920 1080 L0 1080 Z" fill="#7cb35a"/>
      {tree(260, 640, 1.5)}{tree(1700, 620, 1.3, "#d9862c")}
      {"".join(f'<rect x="{x}" y="700" width="26" height="140" rx="6" fill="#f3ead8" {THIN}/>' for x in range(560, 1360, 90))}
      <rect x="540" y="730" width="840" height="22" fill="#f3ead8" {THIN}/><rect x="540" y="790" width="840" height="22" fill="#f3ead8" {THIN}/>
      <path d="M0 830 L1920 830 L1920 1080 L0 1080 Z" fill="#8a5a33"/>
      {flowers}
      <g transform="translate(1000 870)">{sunflower_head(0, -120, 70)}</g>
      {"".join(f'<ellipse cx="{x}" cy="{y}" rx="34" ry="16" fill="{c}" {THIN} transform="rotate({r} {x} {y})"/>' for x, y, r, c in [(1600, 985, -20, "#e8762c"), (1650, 970, 15, "#d9862c"), (1700, 990, -5, "#f0b84a"), (1630, 1000, 30, "#c96a2c"), (1680, 955, -35, "#e8a33c"), (1740, 975, 10, "#e8762c"), (1660, 1015, 0, "#d9862c")])}
      <path d="M700 980 L900 980 L880 1050 L720 1050 Z" fill="#7a8a9a" {S}/><circle cx="740" cy="1060" r="30" fill="#3a3a3a" {S}/>'''


def library():
    shelves = ""
    for x in (60, 1380):
        shelves += f'<rect x="{x}" y="80" width="480" height="820" fill="#7a4a28" {S}/>'
        for i, y in enumerate(range(120, 860, 190)):
            shelves += f'<rect x="{x + 20}" y="{y}" width="440" height="160" fill="#5a3520"/>'
            shelves += books_row(x + 26, y + 10, 430, 150, seed=x + i)
    return f'''<defs>{grad("lwall", "#e8d6b6", "#d6bf98")}{glow("lamp", "#ffe08a", 0.7)}</defs>
      <rect width="{W}" height="{H}" fill="url(#lwall)"/>
      <rect y="880" width="{W}" height="200" fill="#8a5a33"/>
      {shelves}
      {window(760, 140, 400, 440, "#cfe8f5")}
      <rect x="720" y="700" width="480" height="40" rx="10" fill="#9a6237" {S}/>
      <path d="M760 740 L740 900 M1160 740 L1180 900" stroke="{O}" stroke-width="18" stroke-linecap="round"/>
      <circle cx="1040" cy="660" r="140" fill="url(#lamp)"/>
      <path d="M990 640 L1090 640 L1070 580 L1010 580 Z" fill="#3c7a52" {S}/>
      <rect x="1034" y="640" width="12" height="60" fill="{O}"/>
      <rect x="820" y="660" width="140" height="40" rx="6" fill="#c0503f" {THIN}/>'''


def square():
    houses = ""
    rnd = random.Random(3)
    for i, x in enumerate(range(0, 1920, 240)):
        h = rnd.randint(360, 470)
        col = ["#b98b6e", "#9c7a6a", "#c9a27a", "#a8877a"][i % 4]
        houses += f'<rect x="{x}" y="{780 - h}" width="230" height="{h}" fill="{col}" {S}/>'
        houses += f'<path d="M{x - 10} {780 - h} L{x + 115} {700 - h} L{x + 240} {780 - h} Z" fill="#6e4b45" {S}/>'
        for wy in range(800 - h, 740, 110):
            houses += f'<rect x="{x + 40}" y="{wy}" width="50" height="62" rx="6" fill="#ffd98a" {THIN}/><rect x="{x + 140}" y="{wy}" width="50" height="62" rx="6" fill="#ffd98a" {THIN}/>'
    bunting = ""
    for row, (y0, sag) in enumerate([(260, 90), (360, 70)]):
        pts = [(x, y0 + sag * math.sin(math.pi * x / 1920)) for x in range(0, 1921, 80)]
        bunting += '<path d="M' + " L".join(f"{x:.0f} {y:.0f}" for x, y in pts) + f'" fill="none" stroke="{O}" stroke-width="5"/>'
        for i, (x, y) in enumerate(pts[:-1]):
            c = ["#d7263a", "#f6c431", "#3f6fa8", "#5e9a54", "#e8608a"][(i + row) % 5]
            bunting += f'<path d="M{x + 10:.0f} {y + 3:.0f} L{x + 70:.0f} {y + 3:.0f} L{x + 40:.0f} {y + 60:.0f} Z" fill="{c}" {THIN}/>'
    return f'''<defs>{grad("dusk", "#3a4f7a", "#f2a35e")}{glow("lantern", "#ffd166", 0.85)}</defs>
      <rect width="{W}" height="{H}" fill="url(#dusk)"/>
      {"".join(f'<circle cx="{x}" cy="{y}" r="4" fill="#ffffff" opacity="0.8"/>' for x, y in [(200, 80), (520, 140), (900, 60), (1300, 120), (1700, 70), (1550, 200)])}
      {houses}
      <rect y="780" width="{W}" height="300" fill="#a99a8a"/>
      {"".join(f'<path d="M0 {y} L{W} {y}" stroke="#8f8172" stroke-width="5"/>' for y in (850, 930, 1010))}
      {bunting}
      <circle cx="960" cy="520" r="260" fill="url(#lantern)"/>
      <rect x="945" y="560" width="30" height="400" fill="#3a3a44" {S}/>
      <path d="M880 470 L1040 470 L1020 600 L900 600 Z" fill="#ffd166" {S}/>
      <path d="M860 470 L960 400 L1060 470 Z" fill="#3a3a44" {S}/>
      <rect x="880" y="600" width="160" height="22" fill="#3a3a44" {S}/>
      <path d="M960 500 C 990 540 990 570 960 585 C 930 570 930 540 960 500 Z" fill="#fff3b0"/>'''


SCENES = {
    "cottage": cottage(),
    "bakery": bakery(),
    "garden": garden(),
    "library": library(),
    "square": square(),
}

BANNER = f'''<defs>{glow("bglow", "#f2c14e", 0.55)}</defs>
  <rect width="640" height="360" fill="#16202a"/>
  <circle cx="130" cy="180" r="150" fill="url(#bglow)"/>
  <g transform="translate(55 70) scale(0.42)">
    <path d="M150 120 C150 30 300 30 300 120" fill="none" stroke="#e8dcc4" stroke-width="20"/>
    <path d="M110 120 L340 120 L310 165 L140 165 Z" fill="#8c6a3f" {S}/>
    <path d="M140 165 L310 165 L325 420 L125 420 Z" fill="#f2c14e" {S}/>
    <path d="M225 220 C 270 280 270 330 225 365 C 180 330 180 280 225 220 Z" fill="#fff4d6"/>
    <rect x="95" y="420" width="260" height="50" rx="10" fill="#8c6a3f" {S}/>
  </g>
  <text x="250" y="170" font-family="Atkinson" font-weight="700" font-size="64" fill="#fff8ec">Willowbrook</text>
  <text x="252" y="238" font-family="Atkinson" font-weight="700" font-size="46" fill="#f2c14e">Mysteries</text>'''
