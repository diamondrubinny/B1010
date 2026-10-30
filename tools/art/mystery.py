"""Artwork for the gentle mystery (docs/STAGE7.md): the clues, the three suspects and Hazel the hedgehog.

Same storybook style as objects.py, 512 x 512 box. Nothing frightening: the "suspects" are friendly garden
animals, the clues are small and curious, and Hazel is drawn smiling.
"""
import math

from objects import O, S, THIN, leaf, shadow, shine


def spikes(cx, cy, rx, ry, start, end, count, length, color):
    """A soft jagged crown of spines along an ellipse arc (angles in degrees, 0 = right, 90 = down)."""
    pts = []
    for i in range(count * 2 + 1):
        a = math.radians(start + (end - start) * i / (count * 2))
        k = 1.0 if i % 2 else 0.0
        r_x, r_y = rx + length * k, ry + length * k
        pts.append(f"{cx + r_x * math.cos(a):.1f},{cy + r_y * math.sin(a):.1f}")
    return f'<path d="M{cx:.1f},{cy:.1f} L' + " L".join(pts) + f' Z" fill="{color}" {S}/>'


def hedgehog(bell: bool = False) -> str:
    body = (
        shadow(rx=190)
        # Spines first (behind), then the soft body and the friendly face.
        + spikes(230, 330, 150, 120, 180, 355, 13, 46, "#7a5434")
        + f'<ellipse cx="230" cy="335" rx="150" ry="105" fill="#9a6a44" {S}/>'
        + f'<path d="M330 260 C 400 270 450 320 455 345 C 450 375 400 395 340 395 C 320 360 315 300 330 260 Z" fill="#e8c9a0" {S}/>'
        + f'<circle cx="458" cy="346" r="15" fill="#3d2b1f"/>'
        + f'<circle cx="385" cy="315" r="11" fill="#2b2b2b"/><circle cx="388" cy="311" r="4" fill="#fff"/>'
        + '<ellipse cx="398" cy="352" rx="16" ry="10" fill="#f2a3a3" opacity="0.8"/>'
        + f'<path d="M352 300 Q360 285 372 288" fill="none" stroke="{O}" stroke-width="6" stroke-linecap="round"/>'
        + f'<ellipse cx="300" cy="438" rx="26" ry="14" fill="#7a5434" {THIN}/>'
        + f'<ellipse cx="170" cy="438" rx="26" ry="14" fill="#7a5434" {THIN}/>'
        + f'<path d="M405 375 Q420 385 435 378" fill="none" stroke="{O}" stroke-width="5" stroke-linecap="round"/>'
    )
    if bell:
        body += (
            f'<path d="M335 392 Q350 410 372 404" fill="none" stroke="#c0503f" stroke-width="10" stroke-linecap="round"/>'
            f'<circle cx="352" cy="428" r="30" fill="#f2c14e" {THIN}/>'
            f'<circle cx="352" cy="440" r="6" fill="{O}"/>'
            '<path d="M342 412 Q348 406 356 410" fill="none" stroke="#fff" stroke-width="4" stroke-linecap="round" opacity="0.7"/>'
        )
    return body


def squirrel() -> str:
    return shadow(rx=170) + f'''
      <path d="M210 400 C 90 420 50 330 80 240 C 100 170 70 110 120 80 C 190 50 230 140 200 200 C 180 250 230 290 230 330 Z" fill="#c9692e" {S}/>
      <path d="M120 120 C 150 110 170 140 160 180" fill="none" stroke="#e8955a" stroke-width="12" stroke-linecap="round"/>
      <ellipse cx="290" cy="340" rx="85" ry="95" fill="#d9783a" {S}/>
      <ellipse cx="300" cy="360" rx="45" ry="60" fill="#f3d7b0"/>
      <circle cx="330" cy="225" r="68" fill="#d9783a" {S}/>
      <path d="M300 170 L310 120 L335 165 Z" fill="#d9783a" {S}/>
      <circle cx="350" cy="215" r="11" fill="#2b2b2b"/><circle cx="353" cy="211" r="4" fill="#fff"/>
      <circle cx="396" cy="238" r="9" fill="{O}"/>
      <ellipse cx="365" cy="250" rx="13" ry="8" fill="#f2a3a3" opacity="0.8"/>
      <ellipse cx="350" cy="320" rx="26" ry="30" fill="#8a5a33" {THIN}/>
      <path d="M326 302 Q350 285 374 302 L374 296 Q350 275 326 296 Z" fill="#6b4423" {THIN}/>
      <ellipse cx="250" cy="438" rx="34" ry="14" fill="#c9692e" {THIN}/><ellipse cx="330" cy="438" rx="34" ry="14" fill="#c9692e" {THIN}/>'''


def owl() -> str:
    feathers = "".join(
        f'<path d="M{x} {y} q12 14 24 0" fill="none" stroke="#8a6440" stroke-width="5" stroke-linecap="round"/>'
        for x, y in [(222, 330), (258, 330), (240, 360), (276, 360), (204, 360), (222, 390), (258, 390)])
    return shadow(rx=150) + f'''
      <path d="M120 432 L392 432" stroke="#7a4a28" stroke-width="22" stroke-linecap="round"/>
      <path d="M160 140 L175 85 L210 125 Z" fill="#9a7048" {S}/><path d="M352 140 L337 85 L302 125 Z" fill="#9a7048" {S}/>
      <ellipse cx="256" cy="275" rx="135" ry="160" fill="#9a7048" {S}/>
      <ellipse cx="256" cy="340" rx="80" ry="88" fill="#e6cfa8"/>
      {feathers}
      <circle cx="205" cy="210" r="52" fill="#f6f0e0" {THIN}/><circle cx="307" cy="210" r="52" fill="#f6f0e0" {THIN}/>
      <circle cx="205" cy="212" r="26" fill="#f2c14e"/><circle cx="307" cy="212" r="26" fill="#f2c14e"/>
      <circle cx="205" cy="214" r="14" fill="#2b2b2b"/><circle cx="307" cy="214" r="14" fill="#2b2b2b"/>
      <circle cx="210" cy="208" r="5" fill="#fff"/><circle cx="312" cy="208" r="5" fill="#fff"/>
      <path d="M244 250 L268 250 L256 278 Z" fill="#e8a33c" {THIN}/>
      <path d="M135 280 C 120 330 140 390 180 410" fill="none" stroke="#7a5434" stroke-width="10" stroke-linecap="round"/>
      <path d="M377 280 C 392 330 372 390 332 410" fill="none" stroke="#7a5434" stroke-width="10" stroke-linecap="round"/>
      <path d="M225 425 l0 16 M240 425 l0 16 M272 425 l0 16 M287 425 l0 16" stroke="#e8a33c" stroke-width="8" stroke-linecap="round"/>'''


def paw(x, y, angle, s=1.0):
    toes = "".join(f'<ellipse cx="{dx}" cy="-34" rx="9" ry="12" fill="#6b4a32" transform="rotate({dx * 0.6})"/>' for dx in (-24, -8, 8, 24))
    return (f'<g transform="translate({x} {y}) rotate({angle}) scale({s})">'
            f'<ellipse cx="0" cy="0" rx="20" ry="17" fill="#6b4a32"/>{toes}</g>')


def pawprints() -> str:
    return f'''
      <ellipse cx="256" cy="300" rx="215" ry="150" fill="#e8d6b6" {S}/>
      <path d="M90 330 Q150 280 220 300 T 420 260" fill="none" stroke="#d6bf98" stroke-width="18" stroke-linecap="round"/>
      {paw(135, 360, 70, 1.15)}{paw(200, 300, 60, 1.15)}{paw(275, 330, 75, 1.15)}{paw(345, 265, 60, 1.15)}{paw(405, 300, 70, 1.15)}'''


def bitten_apple() -> str:
    bites = "".join(f'<circle cx="{x}" cy="{y}" r="{r}" fill="black"/>' for x, y, r in [(412, 230, 34), (420, 285, 30), (410, 335, 28)])
    return shadow(rx=150) + f'''
      <defs><mask id="bite"><rect width="512" height="512" fill="white"/>{bites}</mask></defs>
      <g mask="url(#bite)">
        <path d="M256 150 C 200 110 90 140 95 260 C 100 380 190 445 256 410 C 322 445 412 380 417 260 C 422 140 312 110 256 150 Z" fill="#d93b30" {S}/>
      </g>
      <path d="M392 215 C 372 230 376 260 394 268 C 374 282 376 312 392 322 C 378 336 380 355 392 362" fill="none" stroke="#f6e7c8" stroke-width="16" stroke-linecap="round"/>
      <path d="M256 150 Q250 100 270 70" fill="none" stroke="#6b4423" stroke-width="14" stroke-linecap="round"/>
      {leaf(272, 95, -25, 0.9)}
      {shine("M140 250 Q150 190 200 175")}'''


def leaf_trail() -> str:
    colors = ["#e8762c", "#d9862c", "#c96a2c", "#f0b84a", "#e8762c"]
    trail = "".join(leaf(x, y, a, 0.95, colors[i % len(colors)]) for i, (x, y, a) in enumerate(
        [(20, 440, -15), (70, 360, 25), (120, 420, -30), (165, 330, 15), (215, 395, -20), (255, 300, 30)]))
    return shadow(rx=210) + f'''
      <path d="M300 440 C 280 280 340 150 420 140 C 500 140 505 300 500 440 Z" fill="#3c7a52" {S}/>
      <path d="M330 300 C 350 250 380 220 420 215" fill="none" stroke="#5e9a64" stroke-width="10" stroke-linecap="round"/>
      <circle cx="430" cy="250" r="12" fill="#c62f3a"/><circle cx="465" cy="320" r="12" fill="#c62f3a"/><circle cx="395" cy="190" r="12" fill="#c62f3a"/>
      <path d="M330 440 Q 360 370 400 365 Q 440 370 455 440 Z" fill="#1f3a28" opacity="0.55"/>
      {trail}'''


def prickle() -> str:
    def spine(x1, y1, x2, y2):
        return (f'<path d="M{x1} {y1} Q {(x1 + x2) / 2 + 30} {(y1 + y2) / 2 - 30} {x2} {y2}" fill="none" stroke="{O}" stroke-width="30" stroke-linecap="round"/>'
                f'<path d="M{x1} {y1} Q {(x1 + x2) / 2 + 30} {(y1 + y2) / 2 - 30} {x2} {y2}" fill="none" stroke="#7a5434" stroke-width="18" stroke-linecap="round"/>'
                f'<circle cx="{x2}" cy="{y2}" r="9" fill="#f3ead8"/>')
    return shadow(rx=170) + f'''
      <ellipse cx="256" cy="330" rx="185" ry="95" fill="#f3ead8" {S}/>
      {spine(130, 380, 380, 250)}
      {spine(170, 400, 360, 330)}
      <path d="M400 150 l10 -30 l10 30 l30 10 l-30 10 l-10 30 l-10 -30 l-30 -10 Z" fill="#f6c431" {THIN}/>'''


MYSTERY_OBJECTS = {
    "hedgehog": hedgehog(),
    "squirrel": squirrel(),
    "owl": owl(),
    "clue_pawprints": pawprints(),
    "clue_bitten_apple": bitten_apple(),
    "clue_leaf_trail": leaf_trail(),
    "clue_prickle": prickle(),
}

# Hazel, the hedgehog who solves the mystery: with the little bell Mrs. Bramble gives her.
HAZEL = hedgehog(bell=True)
