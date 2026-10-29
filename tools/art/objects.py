"""Original vector drawings of every findable object, in one consistent storybook style.

Each entry is SVG markup drawn in a 512 x 512 box. Style rules (see docs/ASSET_PIPELINE.md):
warm brown outlines so shapes stay readable when small, flat colour with one soft highlight,
a soft ground shadow, no text, nothing sharp or frightening.
"""
import math

O = "#3d2b1f"  # outline: warm dark brown
S = f'stroke="{O}" stroke-width="10" stroke-linejoin="round" stroke-linecap="round"'
THIN = f'stroke="{O}" stroke-width="6" stroke-linejoin="round" stroke-linecap="round"'


def shadow(cx=256, cy=452, rx=150, ry=20):
    return f'<ellipse cx="{cx}" cy="{cy}" rx="{rx}" ry="{ry}" fill="#3d2b1f" opacity="0.16"/>'


def shine(d):
    """A soft white highlight stroke."""
    return f'<path d="{d}" fill="none" stroke="#ffffff" stroke-width="12" stroke-linecap="round" opacity="0.55"/>'


def leaf(x, y, angle, size=1.0, color="#5fa045"):
    return (f'<g transform="translate({x} {y}) rotate({angle}) scale({size})">'
            f'<path d="M0 0 C 25 -30 70 -30 95 0 C 70 30 25 30 0 0 Z" fill="{color}" {THIN}/>'
            f'<path d="M8 0 L 85 0" stroke="{O}" stroke-width="4" opacity="0.5"/></g>')


def framed(inner, bg):
    """Round 'window' vignette used for places (sea, mountains, stars)."""
    return (f'<defs><clipPath id="c"><circle cx="256" cy="256" r="196"/></clipPath></defs>'
            f'<circle cx="256" cy="256" r="196" fill="{bg}"/>'
            f'<g clip-path="url(#c)">{inner}</g>'
            f'<circle cx="256" cy="256" r="196" fill="none" {S}/>')


def star_path(cx, cy, r_out, r_in, points=5, rot=-90):
    pts = []
    for i in range(points * 2):
        r = r_out if i % 2 == 0 else r_in
        a = math.radians(rot + i * 180 / points)
        pts.append(f"{cx + r * math.cos(a):.1f},{cy + r * math.sin(a):.1f}")
    return "M" + " L".join(pts) + " Z"


def sunflower_head(cx, cy, r):
    petals = "".join(
        f'<ellipse cx="{cx}" cy="{cy - r * 0.95:.1f}" rx="{r * 0.22:.1f}" ry="{r * 0.5:.1f}" fill="#f6c431" {THIN} '
        f'transform="rotate({i * 360 / 16} {cx} {cy})"/>' for i in range(16))
    dots = "".join(
        f'<circle cx="{cx + r * 0.32 * math.cos(i):.1f}" cy="{cy + r * 0.32 * math.sin(i):.1f}" r="5" fill="#3d2414"/>'
        for i in range(0, 12))
    return petals + f'<circle cx="{cx}" cy="{cy}" r="{r * 0.55:.1f}" fill="#7a4a24" {S}/>' + dots


OBJECTS = {
    "glasses": shadow(rx=170) + f'''
      <path d="M110 250 L60 210 M402 250 L452 210" fill="none" stroke="{O}" stroke-width="20"/>
      <path d="M110 250 L60 210 M402 250 L452 210" fill="none" stroke="#c99a2e" stroke-width="9"/>
      <circle cx="165" cy="290" r="88" fill="#dcefff" opacity="0.85"/>
      <circle cx="347" cy="290" r="88" fill="#dcefff" opacity="0.85"/>
      <path d="M232 268 Q256 245 280 268" fill="none" stroke="{O}" stroke-width="22"/>
      <path d="M232 268 Q256 245 280 268" fill="none" stroke="#c99a2e" stroke-width="10"/>
      <circle cx="165" cy="290" r="88" fill="none" stroke="{O}" stroke-width="28"/>
      <circle cx="165" cy="290" r="88" fill="none" stroke="#d9ab3c" stroke-width="14"/>
      <circle cx="347" cy="290" r="88" fill="none" stroke="{O}" stroke-width="28"/>
      <circle cx="347" cy="290" r="88" fill="none" stroke="#d9ab3c" stroke-width="14"/>
      {shine("M118 268 Q130 232 165 222")}{shine("M300 268 Q312 232 347 222")}''',

    "teacup": shadow(cy=440, rx=185) + f'''
      <ellipse cx="256" cy="405" rx="185" ry="42" fill="#fbf7ef" {S}/>
      <path d="M392 255 C 470 250 470 345 378 345" fill="none" stroke="{O}" stroke-width="30"/>
      <path d="M392 255 C 470 250 470 345 378 345" fill="none" stroke="#fbf7ef" stroke-width="14"/>
      <path d="M115 225 L397 225 Q392 370 256 382 Q120 370 115 225 Z" fill="#fbf7ef" {S}/>
      <ellipse cx="256" cy="225" rx="141" ry="30" fill="#8b5a3c" {S}/>
      <circle cx="190" cy="300" r="13" fill="#e8798f"/><circle cx="256" cy="320" r="13" fill="#e8798f"/>
      <circle cx="322" cy="300" r="13" fill="#e8798f"/>
      <path d="M215 170 C 195 140 235 120 215 85 M285 170 C 265 140 305 120 285 85" fill="none" stroke="#c9c2b8" stroke-width="10" stroke-linecap="round"/>
      {shine("M140 255 Q148 320 190 350")}''',

    "clock": shadow(rx=170) + f'''
      <path d="M110 425 L110 265 Q110 105 256 100 Q402 105 402 265 L402 425 Z" fill="#9a6237" {S}/>
      <rect x="85" y="415" width="342" height="38" rx="10" fill="#7a4a28" {S}/>
      <circle cx="256" cy="268" r="108" fill="#fbf0d9" {S}/>
      {"".join(f'<line x1="{256 + 88 * math.cos(math.radians(a)):.1f}" y1="{268 + 88 * math.sin(math.radians(a)):.1f}" x2="{256 + 98 * math.cos(math.radians(a)):.1f}" y2="{268 + 98 * math.sin(math.radians(a)):.1f}" stroke="{O}" stroke-width="8" stroke-linecap="round"/>' for a in range(0, 360, 30))}
      <line x1="256" y1="268" x2="210" y2="225" stroke="{O}" stroke-width="12" stroke-linecap="round"/>
      <line x1="256" y1="268" x2="318" y2="205" stroke="{O}" stroke-width="8" stroke-linecap="round"/>
      <circle cx="256" cy="268" r="11" fill="{O}"/>
      {shine("M140 300 Q130 200 200 150")}''',

    "book": shadow(rx=200) + f'''
      <path d="M50 160 Q150 130 256 165 Q362 130 462 160 L462 405 Q362 380 256 420 Q150 380 50 405 Z" fill="#b5473a" {S}/>
      <path d="M70 145 Q160 115 256 150 L256 395 Q160 360 70 385 Z" fill="#fffaf0" {S}/>
      <path d="M442 145 Q352 115 256 150 L256 395 Q352 360 442 385 Z" fill="#fffaf0" {S}/>
      {"".join(f'<path d="M100 {195 + i * 38} Q160 {180 + i * 38} 225 {200 + i * 38}" stroke="#c9bfae" stroke-width="9" fill="none" stroke-linecap="round"/><path d="M287 {200 + i * 38} Q352 {180 + i * 38} 412 {195 + i * 38}" stroke="#c9bfae" stroke-width="9" fill="none" stroke-linecap="round"/>' for i in range(5))}
      <path d="M248 120 L248 190 L264 175 L280 190 L280 120" fill="#d94a3d" {THIN}/>''',

    "honey": shadow(rx=165) + f'''
      <path d="M330 120 L420 40" stroke="{O}" stroke-width="26" stroke-linecap="round"/>
      <path d="M330 120 L420 40" stroke="#c48a4f" stroke-width="12" stroke-linecap="round"/>
      <ellipse cx="256" cy="310" rx="160" ry="135" fill="#f2a93b" {S}/>
      <rect x="150" y="150" width="212" height="55" rx="20" fill="#e8962a" {S}/>
      <path d="M155 205 Q170 250 190 210 Q205 270 225 212 Q250 245 265 210 Q290 280 305 210 Q325 245 357 205 Z" fill="#ffd166"/>
      <ellipse cx="256" cy="150" rx="118" ry="24" fill="#ffd166" {S}/>
      {shine("M130 300 Q135 230 185 220")}''',

    "bread": shadow(rx=215) + f'''
      <path d="M45 395 L45 270 Q45 175 140 175 L372 175 Q467 175 467 270 L467 395 Q256 415 45 395 Z" fill="#c98a4b" {S}/>
      <path d="M45 270 Q45 175 140 175 L372 175 Q467 175 467 270 Q256 300 45 270 Z" fill="#b9773a"/>
      <path d="M45 395 L45 270 Q45 175 140 175 L372 175 Q467 175 467 270 L467 395 Q256 415 45 395 Z" fill="none" {S}/>
      <path d="M120 230 Q145 200 170 225 M215 215 Q240 185 265 212 M310 215 Q335 185 360 212" fill="none" stroke="#f0c389" stroke-width="18" stroke-linecap="round"/>
      {shine("M80 360 Q75 280 110 240")}''',
    "strawberries": shadow(rx=180) + "".join(
        f'''<g transform="translate({x} {y}) rotate({r})">
          <path d="M0 -70 C 70 -80 95 -20 70 40 C 50 90 15 115 0 120 C -15 115 -50 90 -70 40 C -95 -20 -70 -80 0 -70 Z" fill="#e0423a" {S}/>
          {"".join(f'<ellipse cx="{dx}" cy="{dy}" rx="5" ry="8" fill="#ffe08a"/>' for dx, dy in [(-35, -20), (0, -30), (35, -20), (-45, 25), (-10, 15), (25, 20), (50, 25), (-20, 60), (15, 65)])}
          <path d="M-45 -70 L-20 -55 L0 -85 L20 -55 L45 -70 L30 -45 L-30 -45 Z" fill="#5fa045" {THIN}/>
          {shine("M-55 0 Q-55 -40 -25 -50")}</g>''' for x, y, r in [(180, 290, -12), (330, 300, 14)]),

    "milk": shadow(rx=110) + f'''
      <path d="M160 110 L352 110 L330 430 L182 430 Z" fill="#eef7fb" {S}/>
      <path d="M170 160 L342 160 L327 425 L185 425 Z" fill="#ffffff"/>
      <ellipse cx="256" cy="160" rx="86" ry="16" fill="#f7f7f2" stroke="#c9d7de" stroke-width="5"/>
      <path d="M160 110 L352 110 L330 430 L182 430 Z" fill="none" {S}/>
      {shine("M190 190 L200 390")}''',

    "egg": shadow(rx=120) + f'''
      <path d="M256 80 C 360 80 395 260 380 330 C 365 410 310 440 256 440 C 202 440 147 410 132 330 C 117 260 152 80 256 80 Z" fill="#e9c7a0" {S}/>
      {shine("M175 300 Q170 200 215 140")}''',

    "cheese": shadow(rx=200) + f'''
      <path d="M60 330 L400 170 L460 230 L460 380 L60 380 Z" fill="#f2c14e" {S}/>
      <path d="M60 330 L400 170 L460 230 L120 330 Z" fill="#f7d978" {S}/>
      <path d="M120 330 L460 230" fill="none" {S}/>
      <circle cx="190" cy="355" r="18" fill="#e0a92e"/><circle cx="300" cy="345" r="24" fill="#e0a92e"/>
      <circle cx="400" cy="320" r="16" fill="#e0a92e"/><circle cx="250" cy="275" r="14" fill="#efc256"/>''',

    "biscuit": shadow(rx=165) + f'''
      <circle cx="256" cy="270" r="160" fill="#d9a35f" {S}/>
      <circle cx="256" cy="270" r="128" fill="#e6b877"/>
      {"".join(f'<circle cx="{256 + r * math.cos(math.radians(a)):.0f}" cy="{270 + r * math.sin(math.radians(a)):.0f}" r="8" fill="#b9813f"/>' for r, n in [(0, 1), (60, 6), (105, 10)] for a in range(0, 360, 360 // n))}
      {shine("M140 220 Q160 150 230 130")}''',

    "cake": shadow(rx=190) + f'''
      <path d="M70 380 L70 250 L440 170 L440 300 Z" fill="#f3d6a0" {S}/>
      <path d="M70 312 L440 232" stroke="#c9364a" stroke-width="22"/>
      <path d="M70 290 L440 210" stroke="#fffaf0" stroke-width="14"/>
      <path d="M70 250 L440 170 L470 185 L100 265 Z" fill="#fffaf0" {S}/>
      <path d="M70 380 L70 250 L440 170 L440 300 Z" fill="none" {S}/>
      <circle cx="400" cy="160" r="22" fill="#d7263a" {S}/>''',

    "croissant": shadow(rx=200) + f'''
      <path d="M40 330 Q60 280 110 270 Q140 190 256 175 Q372 190 402 270 Q452 280 472 330 Q440 360 395 345 Q360 380 256 385 Q152 380 117 345 Q72 360 40 330 Z" fill="#e3a64f" {S}/>
      <path d="M110 272 Q135 320 117 345 M180 205 Q200 290 185 372 M256 177 L256 383 M332 205 Q312 290 327 372 M402 272 Q377 320 395 345" fill="none" stroke="#b97a32" stroke-width="11" stroke-linecap="round"/>
      {shine("M150 240 Q190 205 240 198")}''',
    "apple": shadow(rx=150) + f'''
      <path d="M256 150 C 200 110 90 140 95 260 C 100 380 190 445 256 410 C 322 445 412 380 417 260 C 422 140 312 110 256 150 Z" fill="#d93b30" {S}/>
      <path d="M256 150 Q250 100 270 70" fill="none" stroke="#6b4423" stroke-width="14" stroke-linecap="round"/>
      {leaf(272, 95, -25, 0.9)}
      {shine("M140 250 Q150 190 200 175")}''',

    "lemon": shadow(rx=180) + f'''
      <path d="M70 280 Q90 160 256 150 Q422 160 442 280 Q422 400 256 410 Q90 400 70 280 Z" fill="#f6d743" {S}/>
      <path d="M70 280 L45 280 M442 280 L467 280" stroke="{O}" stroke-width="14" stroke-linecap="round"/>
      {leaf(300, 155, -40, 0.8)}
      {shine("M120 270 Q140 205 210 190")}''',

    "cherries": shadow(rx=170) + f'''
      <path d="M180 320 Q200 180 280 90 M330 330 Q310 200 280 90" fill="none" stroke="#5b8a32" stroke-width="12" stroke-linecap="round"/>
      {leaf(280, 92, -20, 0.9)}
      <circle cx="170" cy="345" r="80" fill="#c8202f" {S}/><circle cx="340" cy="355" r="80" fill="#b51c2a" {S}/>
      {shine("M125 335 Q130 295 165 285")}{shine("M295 345 Q300 305 335 295")}''',

    "pear": shadow(rx=130) + f'''
      <path d="M256 115 C 210 115 205 200 175 250 C 120 330 140 430 256 430 C 372 430 392 330 337 250 C 307 200 302 115 256 115 Z" fill="#b8cf4a" {S}/>
      <path d="M256 118 Q255 80 270 55" fill="none" stroke="#6b4423" stroke-width="12" stroke-linecap="round"/>
      {leaf(268, 75, -15, 0.8)}
      {shine("M185 330 Q190 270 220 240")}''',

    "sunflower": shadow(rx=110) + f'''
      <path d="M256 250 L256 445" stroke="{O}" stroke-width="26" stroke-linecap="round"/>
      <path d="M256 250 L256 445" stroke="#5fa045" stroke-width="12" stroke-linecap="round"/>
      {leaf(256, 370, -150, 1.0)}{leaf(256, 330, -25, 1.0)}
      {sunflower_head(256, 190, 120)}''',

    "tulip": shadow(rx=110) + f'''
      <path d="M256 250 L256 445" stroke="{O}" stroke-width="24" stroke-linecap="round"/>
      <path d="M256 250 L256 445" stroke="#5fa045" stroke-width="10" stroke-linecap="round"/>
      <path d="M256 440 C 180 420 160 330 170 300 C 200 340 240 380 256 440 Z" fill="#5fa045" {THIN}/>
      <path d="M256 440 C 330 420 352 330 342 300 C 312 340 272 380 256 440 Z" fill="#6db14f" {THIN}/>
      <path d="M170 140 L215 175 L256 120 L297 175 L342 140 Q350 270 256 280 Q162 270 170 140 Z" fill="#e8608a" {S}/>
      <path d="M215 175 Q230 240 256 275 M297 175 Q282 240 256 275" fill="none" stroke="#c94672" stroke-width="7"/>''',

    "rose": shadow(rx=110) + f'''
      <path d="M256 280 L256 445" stroke="{O}" stroke-width="24" stroke-linecap="round"/>
      <path d="M256 280 L256 445" stroke="#4f8a3a" stroke-width="10" stroke-linecap="round"/>
      {leaf(256, 380, -150, 0.9, "#4f8a3a")}{leaf(256, 340, -25, 0.9, "#4f8a3a")}
      <path d="M150 200 Q150 100 256 95 Q362 100 362 200 Q362 290 256 295 Q150 290 150 200 Z" fill="#c62f3a" {S}/>
      <path d="M256 140 Q300 140 300 185 Q300 225 256 228 Q212 225 215 190 Q220 165 250 165 Q275 168 272 192" fill="none" stroke="#8f1c28" stroke-width="10" stroke-linecap="round"/>
      <path d="M170 215 Q200 260 256 262 Q312 260 342 215" fill="none" stroke="#8f1c28" stroke-width="9" stroke-linecap="round"/>''',

    "ladybird": shadow(rx=170) + f'''
      {"".join(f'<path d="M{x1} {y1} L{x2} {y2}" stroke="{O}" stroke-width="10" stroke-linecap="round"/>' for x1, y1, x2, y2 in [(130, 260, 80, 235), (125, 320, 70, 330), (140, 380, 95, 420), (382, 260, 432, 235), (387, 320, 442, 330), (372, 380, 417, 420)])}
      <path d="M256 140 C 380 140 410 260 400 320 C 390 400 320 430 256 430 C 192 430 122 400 112 320 C 102 260 132 140 256 140 Z" fill="#d7262e" {S}/>
      <path d="M190 150 Q256 95 322 150 Q256 175 190 150 Z" fill="#2b2b2b" {S}/>
      <circle cx="225" cy="128" r="9" fill="#fff"/><circle cx="287" cy="128" r="9" fill="#fff"/>
      <path d="M256 160 L256 428" stroke="{O}" stroke-width="8"/>
      {"".join(f'<circle cx="{x}" cy="{y}" r="{r}" fill="#2b2b2b"/>' for x, y, r in [(185, 230, 26), (327, 230, 26), (175, 330, 24), (337, 330, 24), (220, 395, 18), (292, 395, 18)])}
      {shine("M150 270 Q160 200 210 180")}''',

    "mushroom": shadow(rx=150) + f'''
      <path d="M205 270 Q195 380 185 420 Q256 440 327 420 Q317 380 307 270 Z" fill="#f4e6cf" {S}/>
      <path d="M80 280 Q90 120 256 110 Q422 120 432 280 Q256 310 80 280 Z" fill="#a8662f" {S}/>
      <ellipse cx="190" cy="190" rx="22" ry="15" fill="#c98a52"/><ellipse cx="300" cy="170" rx="28" ry="18" fill="#c98a52"/><ellipse cx="350" cy="235" rx="18" ry="12" fill="#c98a52"/>
      {shine("M120 245 Q130 170 190 140")}''',

    "chestnut": shadow(rx=150) + f'''
      <path d="M256 100 C 300 100 410 200 405 300 C 400 390 330 430 256 430 C 182 430 112 390 107 300 C 102 200 212 100 256 100 Z" fill="#7a3e1d" {S}/>
      <path d="M115 330 Q256 375 397 330 C 385 400 325 430 256 430 C 187 430 127 400 115 330 Z" fill="#d6b48a" {S}/>
      <path d="M256 100 L256 80" stroke="{O}" stroke-width="14" stroke-linecap="round"/>
      {shine("M165 270 Q170 190 225 150")}''',

    "bee": shadow(rx=150) + f'''
      <ellipse cx="200" cy="170" rx="70" ry="95" fill="#d6ecfa" opacity="0.9" {S} transform="rotate(-25 200 170)"/>
      <ellipse cx="300" cy="165" rx="70" ry="95" fill="#d6ecfa" opacity="0.9" {S} transform="rotate(25 300 165)"/>
      <ellipse cx="256" cy="300" rx="150" ry="115" fill="#f5c330" {S}/>
      <path d="M185 200 Q170 300 185 400 M256 186 Q240 300 256 415 M327 200 Q312 300 327 400" fill="none" stroke="#2b2b2b" stroke-width="30"/>
      <ellipse cx="256" cy="300" rx="150" ry="115" fill="none" {S}/>
      <circle cx="400" cy="285" r="10" fill="#2b2b2b"/>
      <path d="M385 320 Q400 330 415 318" fill="none" stroke="{O}" stroke-width="7" stroke-linecap="round"/>''',

    "bird": shadow(rx=150) + f'''
      <path d="M220 420 L215 450 M280 420 L285 450" stroke="#c98a4b" stroke-width="10" stroke-linecap="round"/>
      <path d="M95 300 L40 250 L60 330 Z" fill="#7a4e30" {S}/>
      <ellipse cx="250" cy="300" rx="165" ry="130" fill="#8a5a3a" {S}/>
      <path d="M300 200 C 380 220 400 300 360 370 C 330 400 260 400 250 360 C 245 300 260 230 300 200 Z" fill="#e2733a"/>
      <ellipse cx="250" cy="300" rx="165" ry="130" fill="none" {S}/>
      <path d="M140 270 Q200 230 250 300 Q190 330 140 270 Z" fill="#6e4528" {THIN}/>
      <circle cx="350" cy="240" r="13" fill="#2b2b2b"/><circle cx="354" cy="236" r="4" fill="#fff"/>
      <path d="M405 255 L450 270 L405 285 Z" fill="#e8b23c" {THIN}/>''',

    "maple_leaf": shadow(rx=130) + f'''
      <path d="M256 430 L256 340" stroke="{O}" stroke-width="16" stroke-linecap="round"/>
      <path d="M256 70 L290 160 L350 120 L340 210 L430 190 L380 260 L420 300 L320 320 L330 360 L256 330 L182 360 L192 320 L92 300 L132 260 L82 190 L172 210 L162 120 L222 160 Z" fill="#e8762c" {S}/>
      <path d="M256 330 L256 120 M256 270 L170 220 M256 270 L342 220 M256 300 L150 290 M256 300 L362 290" fill="none" stroke="#b9541c" stroke-width="7" stroke-linecap="round"/>''',

    "magnifier": shadow(rx=170) + f'''
      <path d="M320 320 L440 440" stroke="{O}" stroke-width="56" stroke-linecap="round"/>
      <path d="M320 320 L440 440" stroke="#7a4a28" stroke-width="36" stroke-linecap="round"/>
      <circle cx="220" cy="220" r="135" fill="#dcefff" opacity="0.9"/>
      <circle cx="220" cy="220" r="135" fill="none" stroke="{O}" stroke-width="44"/>
      <circle cx="220" cy="220" r="135" fill="none" stroke="#d9ab3c" stroke-width="26"/>
      {shine("M140 200 Q150 140 210 125")}''',

    "candle": shadow(rx=150) + f'''
      <defs><radialGradient id="glow"><stop offset="0" stop-color="#ffe9a8" stop-opacity="0.9"/><stop offset="1" stop-color="#ffe9a8" stop-opacity="0"/></radialGradient></defs>
      <circle cx="256" cy="110" r="95" fill="url(#glow)"/>
      <ellipse cx="256" cy="405" rx="150" ry="36" fill="#d9ab3c" {S}/>
      <rect x="200" y="175" width="112" height="235" rx="14" fill="#fbf7ef" {S}/>
      <path d="M256 175 L256 150" stroke="{O}" stroke-width="7"/>
      <path d="M256 60 C 285 100 290 130 256 152 C 222 130 227 100 256 60 Z" fill="#f6a623" {THIN}/>
      <path d="M256 95 C 270 115 270 132 256 142 C 242 132 242 115 256 95 Z" fill="#fff3b0"/>
      {shine("M222 220 L222 370")}''',

    "key": shadow(rx=180) + f'''
      <circle cx="140" cy="256" r="85" fill="#e2b23c" {S}/>
      <circle cx="140" cy="256" r="35" fill="#f4ecd8" {S}/>
      <rect x="215" y="236" width="230" height="40" rx="12" fill="#e2b23c" {S}/>
      <path d="M380 270 L380 330 L415 330 L415 300 L445 300 L445 270 Z" fill="#e2b23c" {S}/>
      {shine("M85 230 Q95 190 135 180")}''',

    "globe": shadow(rx=130) + f'''
      <path d="M256 400 L256 440 M180 445 L332 445" stroke="{O}" stroke-width="22" stroke-linecap="round"/>
      <path d="M256 400 L256 440 M180 445 L332 445" stroke="#9a6237" stroke-width="10" stroke-linecap="round"/>
      <path d="M115 120 A 185 185 0 0 0 256 405" fill="none" stroke="{O}" stroke-width="20" stroke-linecap="round"/>
      <path d="M115 120 A 185 185 0 0 0 256 405" fill="none" stroke="#d9ab3c" stroke-width="9" stroke-linecap="round"/>
      <circle cx="270" cy="240" r="150" fill="#4a90c9" {S}/>
      <path d="M190 170 Q230 140 270 165 Q260 205 220 215 Q195 210 190 170 Z M290 250 Q340 230 370 265 Q360 330 310 340 Q285 300 290 250 Z M170 270 Q200 260 215 300 Q200 330 175 315 Z" fill="#7cc06a" {THIN}/>
      <ellipse cx="270" cy="240" rx="60" ry="150" fill="none" stroke="#ffffff" stroke-width="4" opacity="0.5"/>
      <path d="M120 240 L420 240" stroke="#ffffff" stroke-width="4" opacity="0.5"/>
      {shine("M175 200 Q190 140 245 115")}''',

    "pencil": shadow(rx=190) + f'''
      <g transform="rotate(-35 256 256)">
        <rect x="70" y="215" width="300" height="82" fill="#f6c431" {S}/>
        <rect x="70" y="215" width="300" height="27" fill="#ffd95e"/>
        <rect x="70" y="215" width="300" height="82" fill="none" {S}/>
        <rect x="25" y="215" width="55" height="82" rx="14" fill="#ef8fa0" {S}/>
        <rect x="70" y="210" width="40" height="92" fill="#c9c9c9" {S}/>
        <path d="M370 215 L460 256 L370 297 Z" fill="#f0c99a" {S}/>
        <path d="M430 242 L460 256 L430 270 Z" fill="#3a3a3a"/>
      </g>''',

    "scissors": shadow(rx=170) + f'''
      <path d="M210 240 L440 120" stroke="{O}" stroke-width="34" stroke-linecap="round"/>
      <path d="M210 240 L440 120" stroke="#c9ced6" stroke-width="18" stroke-linecap="round"/>
      <path d="M210 270 L440 390" stroke="{O}" stroke-width="34" stroke-linecap="round"/>
      <path d="M210 270 L440 390" stroke="#dfe3e8" stroke-width="18" stroke-linecap="round"/>
      <circle cx="235" cy="255" r="12" fill="{O}"/>
      <circle cx="130" cy="185" r="70" fill="#d7263a" {S}/><circle cx="130" cy="185" r="35" fill="#f4ecd8" {S}/>
      <circle cx="130" cy="330" r="70" fill="#d7263a" {S}/><circle cx="130" cy="330" r="35" fill="#f4ecd8" {S}/>''',

    "newspaper": shadow(rx=190) + f'''
      <g transform="rotate(-8 256 256)">
        <rect x="80" y="110" width="330" height="300" rx="8" fill="#f4f0e6" {S}/>
        <rect x="110" y="140" width="270" height="40" rx="6" fill="#55504a"/>
        <rect x="110" y="200" width="120" height="90" rx="6" fill="#b9b2a5"/>
        {"".join(f'<rect x="250" y="{205 + i * 22}" width="130" height="10" rx="5" fill="#9c958a"/>' for i in range(4))}
        {"".join(f'<rect x="110" y="{310 + i * 22}" width="270" height="10" rx="5" fill="#9c958a"/>' for i in range(4))}
      </g>''',

    "map": shadow(rx=195) + f'''
      <path d="M60 140 L180 110 L330 140 L452 110 L452 380 L330 410 L180 380 L60 410 Z" fill="#f1dfb3" {S}/>
      <path d="M180 110 L180 380 M330 140 L330 410" stroke="{O}" stroke-width="6" opacity="0.5"/>
      <path d="M100 340 Q170 300 220 320 Q290 350 330 260 Q360 200 410 190" fill="none" stroke="#b5473a" stroke-width="9" stroke-dasharray="4 20" stroke-linecap="round"/>
      <path d="M95 220 L125 170 L155 220 Z M135 230 L160 190 L185 230 Z" fill="#7cc06a" {THIN}/>
      <path d="M350 330 Q380 300 420 330 Q395 360 350 330 Z" fill="#7fb7dc" {THIN}/>''',

    "top_hat": shadow(rx=180) + f'''
      <ellipse cx="256" cy="390" rx="185" ry="45" fill="#3a3a44" {S}/>
      <path d="M150 390 L165 120 Q256 95 347 120 L362 390 Q256 410 150 390 Z" fill="#3a3a44" {S}/>
      <path d="M156 330 Q256 350 356 330 L360 375 Q256 395 152 375 Z" fill="#8e3b5e" {S}/>
      <ellipse cx="256" cy="120" rx="91" ry="22" fill="#4a4a56" {S}/>
      {shine("M195 160 L188 300")}''',

    "umbrella": shadow(rx=170) + f'''
      <path d="M150 425 Q115 450 108 415 Q104 385 138 382" fill="none" stroke="{O}" stroke-width="26" stroke-linecap="round"/>
      <path d="M150 425 Q115 450 108 415 Q104 385 138 382" fill="none" stroke="#9a6237" stroke-width="12" stroke-linecap="round"/>
      <path d="M150 425 L410 75" stroke="{O}" stroke-width="16" stroke-linecap="round"/>
      <path d="M175 375 Q190 240 395 90 Q420 230 330 300 Q270 345 205 395 Z" fill="#3b3f58" {S}/>
      <path d="M200 365 Q250 260 390 110 M250 345 Q320 260 395 130" fill="none" stroke="#5b6189" stroke-width="9" stroke-linecap="round"/>
      <rect x="235" y="255" width="40" height="22" rx="8" fill="#8e3b5e" {THIN} transform="rotate(-50 255 266)"/>''',
    "boot": shadow(rx=190) + f'''
      <path d="M150 90 L290 90 L295 300 Q300 320 330 325 L420 335 Q462 345 460 395 L460 420 L120 420 L120 300 Q120 200 150 90 Z" fill="#8a5a33" {S}/>
      <rect x="110" y="405" width="360" height="30" rx="10" fill="#4d3220" {S}/>
      <path d="M145 120 L290 120" stroke="#6e4426" stroke-width="12"/>
      <path d="M200 160 L240 160 M200 200 L240 200 M200 240 L240 240" stroke="#e8d3a8" stroke-width="8" stroke-linecap="round"/>
      {shine("M150 330 Q150 220 170 150")}''',

    "books": shadow(rx=200) + f'''
      <rect x="70" y="330" width="370" height="80" rx="10" fill="#3f6fa8" {S}/>
      <rect x="95" y="345" width="320" height="12" rx="6" fill="#f4ecd8"/>
      <rect x="95" y="250" width="320" height="80" rx="10" fill="#c0503f" {S}/>
      <rect x="120" y="265" width="270" height="12" rx="6" fill="#f4ecd8"/>
      <rect x="85" y="170" width="340" height="80" rx="10" fill="#5e9a54" {S}/>
      <rect x="110" y="185" width="290" height="12" rx="6" fill="#f4ecd8"/>''',

    "pen": shadow(rx=190) + f'''
      <g transform="rotate(-35 256 256)">
        <rect x="60" y="222" width="290" height="68" rx="34" fill="#2f4a7a" {S}/>
        <rect x="120" y="205" width="110" height="18" rx="8" fill="#d9ab3c" {S}/>
        <rect x="330" y="228" width="30" height="56" fill="#d9ab3c" {S}/>
        <path d="M360 230 L460 256 L360 282 Z" fill="#e2b23c" {S}/>
        <path d="M380 256 L455 256" stroke="{O}" stroke-width="5"/>
      </g>''',

    "letter": shadow(rx=195) + f'''
      <rect x="60" y="130" width="392" height="270" rx="14" fill="#f7efe0" {S}/>
      <path d="M60 140 L256 290 L452 140" fill="#efe2c8" {S}/>
      <path d="M60 395 L200 260 M452 395 L312 260" stroke="{O}" stroke-width="6" opacity="0.4"/>
      <circle cx="256" cy="290" r="38" fill="#c62f3a" {S}/>
      <path d="M240 290 L272 290 M256 274 L256 306" stroke="#8f1c28" stroke-width="7" stroke-linecap="round"/>''',

    "sea": framed(f'''
      <rect x="0" y="0" width="512" height="290" fill="#bfe3f5"/>
      <circle cx="350" cy="150" r="48" fill="#ffd166"/>
      <path d="M170 250 L240 250 L210 290 L195 290 Z" fill="#9a6237" {THIN}/>
      <path d="M205 245 L205 150 L250 235 Z" fill="#ffffff" {THIN}/>
      <path d="M0 290 Q64 260 128 290 Q192 320 256 290 Q320 260 384 290 Q448 320 512 290 L512 512 L0 512 Z" fill="#4a90c9"/>
      <path d="M0 360 Q64 330 128 360 Q192 390 256 360 Q320 330 384 360 Q448 390 512 360 L512 512 L0 512 Z" fill="#3a78b0"/>
      <path d="M60 330 Q90 315 120 330 M300 400 Q330 385 360 400" fill="none" stroke="#ffffff" stroke-width="7" stroke-linecap="round" opacity="0.7"/>''', "#bfe3f5"),

    "mountains": framed(f'''
      <rect x="0" y="0" width="512" height="512" fill="#cfe8f5"/>
      <path d="M20 380 L180 150 L300 330 L380 210 L512 380 Z" fill="#7d8fae" {THIN}/>
      <path d="M180 150 L145 200 L170 195 L190 215 L215 190 Z M380 210 L355 245 L380 240 L400 250 Z" fill="#ffffff"/>
      <path d="M0 400 Q120 330 256 380 Q390 430 512 360 L512 512 L0 512 Z" fill="#7cc06a"/>
      <path d="M0 450 Q150 400 300 450 Q420 490 512 440 L512 512 L0 512 Z" fill="#5fa045"/>''', "#cfe8f5"),

    "stars": framed(
        '<rect x="0" y="0" width="512" height="512" fill="#24345a"/>'
        + "".join(f'<path d="{star_path(x, y, r, r * 0.45)}" fill="#ffe08a" {THIN}/>' for x, y, r in [(180, 190, 46), (330, 150, 34), (360, 300, 52), (200, 350, 30), (270, 245, 22)])
        + "".join(f'<circle cx="{x}" cy="{y}" r="5" fill="#ffffff" opacity="0.8"/>' for x, y in [(120, 260), (420, 220), (300, 400), (140, 140), (390, 400)]),
        "#24345a"),

    # Spell symbols
    "sym_star": f'<path d="{star_path(256, 270, 190, 82)}" fill="#f6c431" {S}/>',
    "sym_moon": f'<path d="M300 70 C 170 90 100 200 120 310 C 140 420 260 470 360 430 C 250 420 190 330 200 240 C 210 160 250 100 300 70 Z" fill="#f6d743" {S}/>',
    "sym_leaf": f'<g transform="translate(70 380) rotate(-40) scale(3.9)">'
                f'<path d="M0 0 C 25 -30 70 -30 95 0 C 70 30 25 30 0 0 Z" fill="#6db14f" stroke="{O}" stroke-width="2.6" stroke-linejoin="round"/>'
                f'<path d="M8 0 L 85 0 M30 0 L45 -12 M55 0 L68 -10 M30 0 L45 12 M55 0 L68 10" stroke="{O}" stroke-width="1.6" opacity="0.5"/></g>',
    "sym_flower": "".join(
        f'<ellipse cx="256" cy="150" rx="70" ry="105" fill="#f4a7c0" {S} transform="rotate({a} 256 256)"/>' for a in range(0, 360, 72))
        + f'<circle cx="256" cy="256" r="55" fill="#f6c431" {S}/>',

    # Sorting places
    "cat_garden": shadow(rx=170) + f'''
      <rect x="230" y="300" width="52" height="150" rx="10" fill="#8a5a33" {S}/>
      <circle cx="256" cy="200" r="130" fill="#5fa045" {S}/>
      <circle cx="170" cy="250" r="80" fill="#6db14f" {S}/><circle cx="345" cy="250" r="80" fill="#6db14f" {S}/>
      <circle cx="210" cy="160" r="16" fill="#d93b30"/><circle cx="310" cy="190" r="16" fill="#d93b30"/><circle cx="250" cy="250" r="16" fill="#d93b30"/>''',
    "cat_kitchen": shadow(rx=180) + f'''
      <path d="M330 300 L470 240" stroke="{O}" stroke-width="40" stroke-linecap="round"/>
      <path d="M330 300 L470 240" stroke="#3a3a3a" stroke-width="22" stroke-linecap="round"/>
      <ellipse cx="220" cy="320" rx="175" ry="100" fill="#4a4a4a" {S}/>
      <ellipse cx="220" cy="310" rx="140" ry="75" fill="#5c5c5c"/>
      <path d="M150 300 Q140 250 200 245 Q240 225 270 255 Q320 260 300 310 Q290 350 230 345 Q160 350 150 300 Z" fill="#ffffff" {THIN}/>
      <circle cx="225" cy="295" r="34" fill="#f6b62e" {THIN}/>''',
    "cat_bookshelf": shadow(rx=190) + f'''
      <rect x="70" y="80" width="372" height="370" rx="10" fill="#9a6237" {S}/>
      <rect x="95" y="105" width="322" height="150" fill="#6e4426"/><rect x="95" y="275" width="322" height="150" fill="#6e4426"/>
      {"".join(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="5" fill="{c}" {THIN}/>' for x, y, w, h, c in [(105, 125, 40, 130, "#c0503f"), (150, 140, 36, 115, "#3f6fa8"), (192, 120, 44, 135, "#5e9a54"), (242, 145, 34, 110, "#e2b23c"), (282, 125, 40, 130, "#8e3b5e"), (330, 150, 38, 105, "#3f6fa8"), (105, 300, 44, 125, "#5e9a54"), (155, 295, 38, 130, "#e2b23c"), (200, 310, 40, 115, "#c0503f"), (250, 300, 36, 125, "#8e3b5e"), (292, 295, 44, 130, "#3f6fa8"), (342, 310, 38, 115, "#c0503f")])}''',
    "cat_cloakroom": shadow(rx=180) + f'''
      <rect x="60" y="80" width="392" height="44" rx="12" fill="#9a6237" {S}/>
      <path d="M170 124 L170 150 M342 124 L342 150" stroke="{O}" stroke-width="14" stroke-linecap="round"/>
      <path d="M170 150 L95 210 L245 210 Z" fill="none" stroke="{O}" stroke-width="10" stroke-linejoin="round"/>
      <path d="M100 205 L80 430 L260 430 L240 205 Z" fill="#b5473a" {S}/>
      <path d="M140 205 L170 300 L200 205" fill="#9a3a2f" {THIN}/>
      <circle cx="170" cy="335" r="9" fill="{O}"/><circle cx="170" cy="380" r="9" fill="{O}"/>
      <ellipse cx="342" cy="250" rx="95" ry="24" fill="#3a3a44" {S}/>
      <path d="M287 250 L298 160 Q342 145 386 160 L397 250 Z" fill="#3a3a44" {S}/>
      <path d="M292 225 L392 225" stroke="#8e3b5e" stroke-width="16"/>''',

}
