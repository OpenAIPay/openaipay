#!/usr/bin/env python3
from __future__ import annotations

import argparse
from pathlib import Path
from typing import Iterable

from PIL import Image


Rect = tuple[int, int, int, int]
Color = tuple[float, float, float, float]


def parse_rect(raw: str) -> Rect:
    parts = [part.strip() for part in raw.split(',')]
    if len(parts) != 4:
        raise argparse.ArgumentTypeError(f"expected x,y,w,h but got: {raw}")
    try:
        x, y, w, h = (int(part) for part in parts)
    except ValueError as exc:
        raise argparse.ArgumentTypeError(f"rect values must be integers: {raw}") from exc
    if w <= 0 or h <= 0:
        raise argparse.ArgumentTypeError(f"rect width/height must be positive: {raw}")
    return (x, y, w, h)


def rect_to_box(rect: Rect, scale: int) -> tuple[int, int, int, int]:
    x, y, w, h = rect
    return (x * scale, y * scale, (x + w) * scale, (y + h) * scale)


def average_color(image: Image.Image, rect: Rect, scale: int) -> Color:
    sample = image.crop(rect_to_box(rect, scale))
    pixels = sample.load()
    totals = [0.0, 0.0, 0.0, 0.0]
    count = max(sample.width * sample.height, 1)
    for y in range(sample.height):
        for x in range(sample.width):
            pixel = pixels[x, y]
            for channel in range(4):
                totals[channel] += pixel[channel]
    return tuple(total / count for total in totals)


def sampled_horizontal_palette(image: Image.Image, rect: Rect, scale: int, segment_count: int) -> list[Color]:
    sample = image.crop(rect_to_box(rect, scale))
    pixels = sample.load()
    colors: list[Color] = []
    for index in range(segment_count):
        x0 = round(index * sample.width / segment_count)
        x1 = round((index + 1) * sample.width / segment_count)
        if x1 <= x0:
            x1 = min(sample.width, x0 + 1)
        totals = [0.0, 0.0, 0.0, 0.0]
        count = 0
        for y in range(sample.height):
            for x in range(x0, x1):
                pixel = pixels[x, y]
                for channel in range(4):
                    totals[channel] += pixel[channel]
                count += 1
        colors.append(tuple(total / max(count, 1) for total in totals))
    return colors


def interpolate_palette(colors: list[Color], ratio: float) -> Color:
    if not colors:
        return (240.0, 243.0, 247.0, 255.0)
    if len(colors) == 1:
        return colors[0]
    position = max(0.0, min(1.0, ratio)) * (len(colors) - 1)
    lower_index = int(position)
    upper_index = min(len(colors) - 1, lower_index + 1)
    blend = position - lower_index
    lower = colors[lower_index]
    upper = colors[upper_index]
    return tuple(lower[channel] * (1 - blend) + upper[channel] * blend for channel in range(4))


def interpolate_color(start: Color, end: Color, ratio: float) -> Color:
    t = max(0.0, min(1.0, ratio))
    return tuple(start[channel] * (1 - t) + end[channel] * t for channel in range(4))


def build_patch(
    image: Image.Image,
    scale: int,
    target: Rect,
    top_sample: Rect,
    right_top_sample: Rect,
    right_bottom_sample: Rect,
    segment_count: int,
) -> Image.Image:
    top_palette = sampled_horizontal_palette(image, top_sample, scale, segment_count)
    right_top = average_color(image, right_top_sample, scale)
    right_bottom = average_color(image, right_bottom_sample, scale)
    anchor = interpolate_palette(top_palette, 1.0)

    width = target[2] * scale
    height = target[3] * scale
    patch = Image.new('RGBA', (width, height))
    patch_pixels = patch.load()

    for y in range(height):
        ty = 0.0 if height == 1 else y / (height - 1)
        right_color = interpolate_color(right_top, right_bottom, ty)
        for x in range(width):
            tx = 0.0 if width == 1 else x / (width - 1)
            top_color = interpolate_palette(top_palette, tx)
            patch_pixels[x, y] = tuple(
                max(0, min(255, round(top_color[channel] + right_color[channel] - anchor[channel])))
                for channel in range(4)
            )
    return patch


def paste_patch(source: Image.Image, patch: Image.Image, target: Rect, scale: int) -> Image.Image:
    result = source.copy()
    x0, y0, _, _ = rect_to_box(target, scale)
    result.paste(patch, (x0, y0))
    return result


def restore_rects(result: Image.Image, source: Image.Image, rects: Iterable[Rect], scale: int) -> None:
    for rect in rects:
        box = rect_to_box(rect, scale)
        result.paste(source.crop(box), (box[0], box[1]))


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Repair a screenshot region using sampled surrounding gradients.")
    parser.add_argument("--input", required=True, help="Input screenshot path")
    parser.add_argument("--output", required=True, help="Output image path")
    parser.add_argument("--scale", required=True, type=int, help="Asset scale factor, usually 1, 2, or 3")
    parser.add_argument("--target", required=True, type=parse_rect, help="Target rect x,y,w,h in 1x coordinates")
    parser.add_argument("--top-sample", required=True, type=parse_rect, help="Top sample rect x,y,w,h in 1x coordinates")
    parser.add_argument("--right-top-sample", required=True, type=parse_rect, help="Upper side sample rect x,y,w,h in 1x coordinates")
    parser.add_argument("--right-bottom-sample", required=True, type=parse_rect, help="Lower side sample rect x,y,w,h in 1x coordinates")
    parser.add_argument("--restore", action="append", default=[], type=parse_rect, help="Rect x,y,w,h to paste back from the original after patching")
    parser.add_argument("--segments", type=int, default=36, help="Horizontal palette segment count")
    parser.add_argument("--output-patch", help="Optional path to save the generated patch image")
    return parser


def main() -> int:
    parser = build_parser()
    args = parser.parse_args()

    if args.scale <= 0:
        parser.error("--scale must be positive")
    if args.segments <= 0:
        parser.error("--segments must be positive")

    input_path = Path(args.input)
    output_path = Path(args.output)
    source = Image.open(input_path).convert('RGBA')

    patch = build_patch(
        image=source,
        scale=args.scale,
        target=args.target,
        top_sample=args.top_sample,
        right_top_sample=args.right_top_sample,
        right_bottom_sample=args.right_bottom_sample,
        segment_count=args.segments,
    )
    result = paste_patch(source, patch, args.target, args.scale)
    restore_rects(result, source, args.restore, args.scale)

    output_path.parent.mkdir(parents=True, exist_ok=True)
    result.save(output_path)

    if args.output_patch:
        patch_path = Path(args.output_patch)
        patch_path.parent.mkdir(parents=True, exist_ok=True)
        patch.save(patch_path)

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
