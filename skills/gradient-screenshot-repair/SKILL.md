---
name: gradient-screenshot-repair
description: Repair native-looking mobile screenshot backgrounds by erasing localized UI content and reconstructing nearby gradients from sampled pixels. Use when Codex must remove status-bar items, avatars, nicknames, labels, chips, or overlay text from app screenshots without visible seams, flat fills, or color mismatches. Also use for Chinese requests like 擦除顶部、补渐变、抹掉头像、抹掉昵称、保留原图底色、不要白块、不要假遮罩.
---

# Gradient Screenshot Repair

## Overview

Use this skill to clean a bounded region inside a mobile screenshot while keeping the original page surface believable.
Prefer rebuilding the background from nearby sampled pixels instead of drawing flat color blocks, blur overlays, or fake replacement panels.

## Workflow

1. Identify the exact region to remove.
Use a slightly larger target rect than the visible dirty pixels so edge remnants are also covered.

2. Choose clean sample bands.
Use these three samples whenever the damaged area sits on a mixed horizontal and vertical gradient:
- `top-sample`: a clean horizontal band just above or just below the target area; this captures left-to-right color drift
- `right-top-sample`: a clean block near the target's upper-right edge; this captures the upper vertical tone
- `right-bottom-sample`: a clean block near the target's lower-right edge; this captures the lower vertical tone

3. Build the patch with sampled gradients.
Run `scripts/sample_surface_patch.py` to reconstruct the target area.
The script combines a horizontal palette from the top band with a vertical delta from the clean side edge, anchored at the top-right reference color so the two gradients do not double-count.

4. Restore preserved foreground elements if needed.
If a badge, chip, or icon should remain, paste that untouched source rect back after the patch is applied.
This is often the cleanest way to hide a seam around small preserved UI.

5. Verify at native scale.
Inspect the edge nearest the clean source samples first.
If one side still shows a seam, move the sample rect closer to the seam before trying manual tint adjustments.

## Rules

- Do not use flat fills unless the source area is actually flat.
- Do not add white masks, translucent overlays, or new background cards.
- Do not sample from already-damaged pixels.
- Do not trust a single point sample when the source surface is a gradient.
- Prefer replacing pixels in the reference image or asset itself, not layering a fake background above it at runtime.

## Tuning

- If the repaired area is too light or too dark on one side, move the side samples closer to that side's seam.
- If left and right gradient direction looks reversed, the horizontal sample order is wrong or the wrong clean band was used.
- If the patch looks flat, widen the top sample or increase its height slightly.
- If a thin seam remains, expand the target rect by a few pixels past the visible artifact.
- If only vertical gradient matters, keep the same workflow but choose a top sample with minimal horizontal variation.

## Resources

### scripts/

- `scripts/sample_surface_patch.py`: paste a reconstructed patch into an image using one target rect, one horizontal sample band, two side samples, and optional restore rects.

Usage:

```bash
python3 scripts/sample_surface_patch.py \
  --input screenshot.png \
  --output repaired.png \
  --scale 3 \
  --target 14,76,190,74 \
  --top-sample 14,60,178,16 \
  --right-top-sample 220,82,40,18 \
  --right-bottom-sample 250,136,40,14 \
  --restore 192,110,74,24
```

### references/

- `references/gradient-repair-playbook.md`: parameter selection rules, failure symptoms, and the proven OpenAiPay profile-header example.
