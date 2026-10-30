#!/usr/bin/env python3
"""Turns a real photograph into a painted, framed character portrait in the game's storybook style.

Used for Dr. Rubinny, the guide. The source photo is personal data: it is NOT stored in the repository,
only the painted result is. EXIF and all other metadata are dropped.

Usage:
    python3 tools/art/portrait_from_photo.py PHOTO.jpg rubinny [--preview PREVIEW.png]
Writes app/src/main/res/drawable-nodpi/char_<name>.webp (768x768, transparent outside the frame).
Requires opencv-python-headless, numpy and Pillow.
"""
import argparse
import os
import sys

import cv2
import numpy as np
from PIL import Image, ImageDraw, ImageFilter

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
DRAWABLE = os.path.join(ROOT, "app", "src", "main", "res", "drawable-nodpi")

# Square canvas: portraits are shown in a square box, so a square image uses all of it.
W, H = 768, 768
# Oval opening of the frame (slightly taller than wide), and the frame band around it.
OVAL = (110, 58, W - 110, H - 58)
FRAME = 40

# Storybook palette (docs/DESIGN_SYSTEM.md): a warm gradient from deep brown through skin tones to cream,
# so a sepia photo reads as a gentle painting instead of an old photograph.
GRADIENT = [
    (0.00, (44, 30, 26)),
    (0.30, (112, 70, 52)),
    (0.55, (188, 128, 96)),
    (0.75, (226, 178, 140)),
    (1.00, (250, 236, 214)),
]
BACKDROP = ((70, 104, 98), (140, 170, 150))  # sage green, dark -> light, behind the head


def gradient_map(gray: np.ndarray) -> np.ndarray:
    xs = [p for p, _ in GRADIENT]
    out = np.zeros(gray.shape + (3,), np.float32)
    for c in range(3):
        out[..., c] = np.interp(gray, xs, [col[c] for _, col in GRADIENT])
    return out


def paint(photo: np.ndarray) -> np.ndarray:
    """Painterly look: edge-preserving smoothing, soft colour bands and a light ink line."""
    smooth = cv2.bilateralFilter(photo, 0, 28, 7)
    smooth = cv2.edgePreservingFilter(smooth, flags=cv2.RECURS_FILTER, sigma_s=18, sigma_r=0.12)
    gray = cv2.cvtColor(smooth, cv2.COLOR_BGR2GRAY).astype(np.float32) / 255.0
    # Gentle contrast so the face stays clear on a TV seen from a sofa.
    gray = np.clip((gray - 0.08) / 0.84, 0, 1) ** 0.92
    coloured = gradient_map(gray)
    # Thin, soft outlines like a pencil under the paint.
    edges = cv2.Canny(cv2.GaussianBlur(cv2.cvtColor(photo, cv2.COLOR_BGR2GRAY), (0, 0), 2.6), 40, 90)
    edges = cv2.GaussianBlur(edges.astype(np.float32) / 255.0, (0, 0), 1.0)
    coloured *= (1.0 - 0.18 * edges[..., None])
    return np.clip(coloured, 0, 255).astype(np.uint8)  # RGB


def build(photo_path: str) -> Image.Image:
    photo = cv2.imread(photo_path, cv2.IMREAD_COLOR)
    if photo is None:
        raise SystemExit(f"Cannot read {photo_path}")
    # Fit the whole head and shoulders into the oval: scale to the oval height, crop the sides evenly.
    ow, oh = OVAL[2] - OVAL[0], OVAL[3] - OVAL[1]
    scale = oh / photo.shape[0]
    photo = cv2.resize(photo, (round(photo.shape[1] * scale), oh), interpolation=cv2.INTER_AREA)
    x0 = (photo.shape[1] - ow) // 2
    photo = photo[:, x0: x0 + ow]

    painted = paint(photo).astype(np.float32)
    # Toward the rim, blend into a painted sage backdrop so the figure sits in a calm, coloured oval.
    yy, xx = np.mgrid[0:oh, 0:ow].astype(np.float32)
    rim = np.clip((np.hypot((xx - ow / 2) / (ow / 2), (yy - oh * 0.45) / (oh * 0.6)) - 0.62) / 0.35, 0, 1)
    rim *= np.clip((oh * 0.80 - yy) / (oh * 0.15), 0, 1)  # keep the jacket at the bottom
    rim = (rim ** 1.4)[..., None]
    dark, light = np.array(BACKDROP[0], np.float32), np.array(BACKDROP[1], np.float32)
    sage = light + (dark - light) * rim
    rgb = painted * (1 - 0.85 * rim) + sage * 0.85 * rim
    inner = Image.fromarray(np.clip(rgb, 0, 255).astype(np.uint8), "RGB")

    canvas = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    oval_mask = Image.new("L", (ow, oh), 0)
    ImageDraw.Draw(oval_mask).ellipse((0, 0, ow - 1, oh - 1), fill=255)
    canvas.paste(inner, OVAL[:2], oval_mask)

    # Warm brass frame: shadow, band, highlight and inner lip.
    frame = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(frame)
    outer = (OVAL[0] - FRAME, OVAL[1] - FRAME, OVAL[2] + FRAME, OVAL[3] + FRAME)
    shadow = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    ImageDraw.Draw(shadow).ellipse((outer[0] + 6, outer[1] + 14, outer[2] + 6, outer[3] + 14), outline=(20, 14, 10, 120), width=FRAME)
    canvas = Image.alpha_composite(shadow.filter(ImageFilter.GaussianBlur(10)), canvas)
    d.ellipse(outer, outline=(150, 104, 44, 255), width=FRAME)
    d.ellipse((outer[0] + 8, outer[1] + 8, outer[2] - 8, outer[3] - 8), outline=(214, 170, 92, 255), width=12)
    d.ellipse((outer[0] + 22, outer[1] + 22, outer[2] - 22, outer[3] - 22), outline=(238, 206, 138, 255), width=4)
    d.ellipse((OVAL[0] - 6, OVAL[1] - 6, OVAL[2] + 6, OVAL[3] + 6), outline=(96, 64, 28, 255), width=8)
    # A small lantern-gold star at the top: the guide's badge.
    cx, cy, r = W // 2, outer[1] + FRAME // 2, 26
    star = [(cx + (r if i % 2 == 0 else r * 0.45) * np.sin(i * np.pi / 5), cy - (r if i % 2 == 0 else r * 0.45) * np.cos(i * np.pi / 5)) for i in range(10)]
    d.polygon(star, fill=(252, 212, 96, 255), outline=(150, 104, 44, 255))
    return Image.alpha_composite(canvas, frame)


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("photo")
    ap.add_argument("name", help="character key, e.g. rubinny -> char_rubinny.webp")
    ap.add_argument("--preview", help="also write a PNG preview on a dark background")
    args = ap.parse_args()
    img = build(args.photo)
    target = os.path.join(DRAWABLE, f"char_{args.name}.webp")
    img.save(target, "WEBP", quality=88, method=6)  # new image: no EXIF or other metadata from the photo
    print(f"{target} {os.path.getsize(target) // 1024} KB")
    if args.preview:
        bg = Image.new("RGBA", img.size, (36, 52, 68, 255))
        Image.alpha_composite(bg, img).convert("RGB").save(args.preview)
    return 0


if __name__ == "__main__":
    sys.exit(main())
