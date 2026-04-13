# Gradient Repair Playbook

## Use This Pattern For

Use this pattern when the original screenshot background must stay visually native after removing localized UI content such as:

- status-bar time, Wi-Fi, or battery
- profile avatars
- nicknames or masked mobile text
- small chips, labels, or inline buttons

This works best when the removed region sits on a smooth surface that can be approximated from nearby clean pixels.

## Sampling Strategy

### 1. Target Rect

Make the target rect slightly larger than the visible dirty region.
A too-tight rect leaves thin edge remnants that look worse than a slightly larger repair.

### 2. Top Sample

Pick a clean horizontal band immediately adjacent to the repair area.
Use enough width to capture left-to-right drift.
Avoid bands that cross icons, text, or strong shape boundaries.

### 3. Side Samples

Pick two clean sample blocks on the same side of the repair area, usually the right side:

- one near the upper edge
- one near the lower edge

These two blocks define the vertical color change for that side.
The algorithm interpolates between them for each output row.

### 4. Why The Anchor Matters

The patch is built like this conceptually:

- horizontal surface from the top band
- plus vertical side drift from the clean edge
- minus the shared anchor color once

This avoids counting the overlap twice.
In the current implementation, the anchor is the top palette's rightmost color because the vertical samples come from the right side.

## Failure Modes And Fixes

### Right edge still lighter than the real background

Cause:
- side samples were taken too far from the seam
- lower sample came from a region that had already flattened out

Fix:
- move the right-side sample blocks closer to the seam
- sample from directly below the seam rather than a distant area

### Left-right gradient looks flipped

Cause:
- the top sample was mirrored conceptually by using the wrong clean band or an invalid crop

Fix:
- verify the sampled band preserves the screenshot's actual left-to-right order
- do not reverse columns during preprocessing

### Patch looks like a flat block

Cause:
- using a single sampled color
- top band too narrow or too short

Fix:
- use a true horizontal band, not a point sample
- widen the band so the palette captures horizontal drift

### A thin seam remains even though color is close

Cause:
- target rect was too tight
- artifact pixels remain just outside the patch boundary

Fix:
- expand the target rect by a few pixels beyond the visible problem
- if preserving a chip or badge, patch first and then paste the preserved rect back

## Proven OpenAiPay Example

Concrete working example from the OpenAiPay profile page clean-up:

- Source screenshot:
  `/Users/<username>/code/openaipay/截图资料/01_设备采集/采集批次_20260306_161806_我的页真机/页面截图_我的页真机.png`
- Concrete implementation:
  `/Users/<username>/code/openaipay/.tools/generate_profile_reference_clean.py`

Key parameters used there:

- scale: `3`
- top sample: `14,60,178,16`
- right top sample: `220,82,40,18`
- right bottom sample: `250,136,40,14`
- target: `14,76,190,74`
- restore preserved chip: `192,110,74,24`

Why this version worked:

- it used a horizontal palette instead of a flat fill
- it captured vertical drift from the clean right edge
- it restored the certification chip after patching
- it expanded the repair area enough to hide edge remnants
- it replaced pixels in the base asset instead of stacking a fake background layer
