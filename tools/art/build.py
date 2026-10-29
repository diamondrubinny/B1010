#!/usr/bin/env python3
"""Builds all game artwork: SVG source -> Chromium render -> WebP in res/drawable-nodpi.

Usage: python3 tools/art/build.py [--only obj|char|scene|banner] [--sheet PATH]
Requires Node with Playwright (Chromium) and Pillow.
"""
import argparse
import os
import subprocess
import sys
import tempfile

from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, "..", ".."))
sys.path.insert(0, HERE)

from objects import OBJECTS  # noqa: E402

DRAWABLE = os.path.join(ROOT, "app", "src", "main", "res", "drawable-nodpi")


def svg_doc(body: str, w: int, h: int, view_w: int, view_h: int) -> str:
    return (f'<svg xmlns="http://www.w3.org/2000/svg" data-w="{w}" data-h="{h}" width="{w}" height="{h}" '
            f'viewBox="0 0 {view_w} {view_h}">{body}</svg>')


def jobs(only: str | None):
    """(output name, svg, lossless?)"""
    out = []
    if only in (None, "obj"):
        for name, body in OBJECTS.items():
            prefix = "" if name.startswith(("sym_", "cat_")) else "obj_"
            out.append((prefix + name, svg_doc(body, 320, 320, 512, 512), True))
    if only in (None, "char"):
        from characters import CHARACTERS, PORTRAITS
        for name, body in CHARACTERS.items():
            if body is None:
                continue
            out.append(("char_" + name, svg_doc(body, 576, 768, 768, 1024), True))
        # Head-only square crop of Marmalade (viewBox around her head) for small companion icons.
        out.append(("char_marmalade_face", f'<svg xmlns="http://www.w3.org/2000/svg" data-w="320" data-h="320" width="320" height="320" viewBox="150 40 468 468">{CHARACTERS["marmalade"]}</svg>', True))
        for name, body in PORTRAITS.items():
            out.append(("obj_" + name, svg_doc(body, 320, 320, 512, 512), True))
    if only in (None, "scene"):
        from scenes import SCENES
        for name, body in SCENES.items():
            out.append(("scene_" + name, svg_doc(body, 1920, 1080, 1920, 1080), False))
    if only in (None, "banner"):
        from scenes import BANNER
        out.append(("tv_banner_art", svg_doc(BANNER, 640, 360, 640, 360), False))      # xhdpi (1080p TVs)
        out.append(("tv_banner_art@xxhdpi", svg_doc(BANNER, 960, 540, 640, 360), False))  # xxhdpi (4K launchers)
    return out


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--only", choices=["obj", "char", "scene", "banner"])
    ap.add_argument("--sheet", help="also write a contact sheet PNG of the rendered set")
    args = ap.parse_args()

    os.makedirs(DRAWABLE, exist_ok=True)
    work = jobs(args.only)
    with tempfile.TemporaryDirectory() as tmp:
        svg_dir, png_dir = os.path.join(tmp, "svg"), os.path.join(tmp, "png")
        os.makedirs(svg_dir)
        for name, svg, _ in work:
            with open(os.path.join(svg_dir, name + ".svg"), "w", encoding="utf-8") as f:
                f.write(svg)
        subprocess.run(["node", os.path.join(HERE, "render.cjs"), svg_dir, png_dir], cwd=ROOT, check=True)
        images = []
        for name, _, lossless in work:
            img = Image.open(os.path.join(png_dir, name + ".png")).convert("RGBA")
            # The launcher banner is a 320x180dp image: xhdpi 640x360 px and xxhdpi 960x540 px.
            folder = DRAWABLE
            if name.startswith("tv_banner_art"):
                folder = DRAWABLE.replace("drawable-nodpi", "drawable-xxhdpi" if name.endswith("@xxhdpi") else "drawable-xhdpi")
            os.makedirs(folder, exist_ok=True)
            target = os.path.join(folder, name.split("@")[0] + ".webp")
            if lossless:
                img.save(target, "WEBP", lossless=True, method=6)
            else:
                img.convert("RGB").save(target, "WEBP", quality=82, method=6)
            images.append((name, img))
            print(f"{name:24s} {os.path.getsize(target) // 1024:4d} KB")
        if args.sheet:
            cell = 200
            cols = 8
            rows = (len(images) + cols - 1) // cols
            sheet = Image.new("RGB", (cols * cell, rows * cell), (36, 52, 68))
            for i, (_, img) in enumerate(images):
                thumb = img.copy()
                thumb.thumbnail((cell - 20, cell - 20))
                sheet.paste(thumb, ((i % cols) * cell + 10, (i // cols) * cell + 10), thumb)
            sheet.save(args.sheet)
    return 0


if __name__ == "__main__":
    sys.exit(main())
