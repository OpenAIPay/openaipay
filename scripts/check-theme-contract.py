#!/usr/bin/env python3
"""
AiPay theme contract checker.

Default mode checks only added lines in changed Swift files.
Use --mode all to scan all Swift files under ios-app/OpenAiPay.
"""

from __future__ import annotations

import argparse
import ast
import re
import subprocess
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable


ROOT = Path(__file__).resolve().parents[1]
ALLOW_MARK = "theme-check:allow"

COLOR_CALL_RE = re.compile(
    r"(?:^|[^A-Za-z0-9_])(?:UI)?Color\s*\(\s*red:\s*([^,\n]+)\s*,\s*green:\s*([^,\n]+)\s*,\s*blue:\s*([^,\n]+)\s*,"
)
HEX_RE = re.compile(r"#([0-9A-Fa-f]{6})")

LAYER_KEYWORDS = (
    "TintOverlay",
    ".blendMode(",
    "colorMultiply(",
    "hueRotation(",
    "saturation(",
)

SKIP_FILE_KEYWORDS = (
    "GeneratedAssetSymbols.swift",
)
COLOR_SOURCE_OF_TRUTH_FILES = (
    "ios-app/OpenAiPay/Core/AppTheme.swift",
)


@dataclass
class Violation:
    path: str
    line: int
    message: str


def run_git(args: list[str]) -> str:
    proc = subprocess.run(
        ["git", *args],
        cwd=ROOT,
        check=False,
        capture_output=True,
        text=True,
    )
    if proc.returncode != 0:
        return ""
    return proc.stdout


def eval_expr(expr: str) -> float:
    node = ast.parse(expr.strip(), mode="eval")

    def walk(n: ast.AST) -> float:
        if isinstance(n, ast.Expression):
            return walk(n.body)
        if isinstance(n, ast.Constant) and isinstance(n.value, (int, float)):
            return float(n.value)
        if isinstance(n, ast.UnaryOp) and isinstance(n.op, (ast.UAdd, ast.USub)):
            value = walk(n.operand)
            return value if isinstance(n.op, ast.UAdd) else -value
        if isinstance(n, ast.BinOp) and isinstance(
            n.op, (ast.Add, ast.Sub, ast.Mult, ast.Div)
        ):
            left = walk(n.left)
            right = walk(n.right)
            if isinstance(n.op, ast.Add):
                return left + right
            if isinstance(n.op, ast.Sub):
                return left - right
            if isinstance(n.op, ast.Mult):
                return left * right
            if right == 0:
                raise ValueError("division by zero")
            return left / right
        raise ValueError(f"unsupported expression: {expr!r}")

    return walk(node)


def normalize_rgb(v: float) -> float:
    if v < 0:
        return 0.0
    if v > 1.0 and v <= 255.0:
        return v / 255.0
    return min(v, 1.0)


def is_blue_leaning(r: float, g: float, b: float) -> bool:
    return b >= 0.70 and (b - r) >= 0.14 and (b - g) >= 0.08


def parse_added_lines(diff_text: str) -> list[tuple[int, str]]:
    added: list[tuple[int, str]] = []
    new_line = 0
    for line in diff_text.splitlines():
        if line.startswith("@@"):
            m = re.search(r"\+(\d+)(?:,(\d+))?", line)
            if m:
                new_line = int(m.group(1))
            continue
        if line.startswith("+++"):
            continue
        if line.startswith("+"):
            added.append((new_line, line[1:]))
            new_line += 1
            continue
        if line.startswith("-") and not line.startswith("---"):
            continue
        if line.startswith(" "):
            new_line += 1
    return added


def changed_swift_files() -> list[Path]:
    paths: set[str] = set()
    for args in (
        ["diff", "--name-only", "--", "*.swift"],
        ["diff", "--cached", "--name-only", "--", "*.swift"],
        ["ls-files", "--others", "--exclude-standard", "--", "*.swift"],
    ):
        out = run_git(args)
        for raw in out.splitlines():
            raw = raw.strip()
            if raw:
                paths.add(raw)
    files: list[Path] = []
    for p in sorted(paths):
        full = ROOT / p
        if full.exists():
            files.append(full)
    return files


def all_swift_files() -> list[Path]:
    base = ROOT / "ios-app" / "OpenAiPay"
    return sorted(base.rglob("*.swift"))


def should_skip(path: Path) -> bool:
    return any(k in str(path) for k in SKIP_FILE_KEYWORDS)


def collect_lines_for_file(path: Path, mode: str) -> list[tuple[int, str]]:
    rel = str(path.relative_to(ROOT))
    if mode == "all":
        lines = path.read_text(encoding="utf-8").splitlines()
        return [(idx + 1, line) for idx, line in enumerate(lines)]

    untracked = run_git(["ls-files", "--others", "--exclude-standard", "--", rel]).strip()
    if untracked:
        lines = path.read_text(encoding="utf-8").splitlines()
        return [(idx + 1, line) for idx, line in enumerate(lines)]

    added = []
    added.extend(parse_added_lines(run_git(["diff", "-U0", "--", rel])))
    added.extend(parse_added_lines(run_git(["diff", "--cached", "-U0", "--", rel])))
    dedup = {}
    for ln, text in added:
        dedup[(ln, text)] = None
    return sorted(dedup.keys(), key=lambda x: x[0])


def check_line(
    path: str,
    line_no: int,
    line: str,
    skip_blue_color_check: bool,
) -> Iterable[Violation]:
    if ALLOW_MARK in line:
        return []

    violations: list[Violation] = []

    for keyword in LAYER_KEYWORDS:
        if keyword in line:
            violations.append(
                Violation(path, line_no, f"禁止通过图层改色: `{keyword}`")
            )
            break

    if not skip_blue_color_check:
        for m in COLOR_CALL_RE.finditer(line):
            try:
                r = normalize_rgb(eval_expr(m.group(1)))
                g = normalize_rgb(eval_expr(m.group(2)))
                b = normalize_rgb(eval_expr(m.group(3)))
            except Exception:
                continue
            if is_blue_leaning(r, g, b):
                violations.append(
                    Violation(
                        path,
                        line_no,
                        "检测到新增蓝色硬编码，请改为 AppTheme token 或使用暖紫风格",
                    )
                )

        for m in HEX_RE.finditer(line):
            code = m.group(1)
            r = int(code[0:2], 16) / 255.0
            g = int(code[2:4], 16) / 255.0
            b = int(code[4:6], 16) / 255.0
            if is_blue_leaning(r, g, b):
                violations.append(
                    Violation(
                        path,
                        line_no,
                        f"检测到新增蓝色 Hex `#{code}`，请改为 AppTheme token 或暖紫色",
                    )
                )

    return violations


def main() -> int:
    parser = argparse.ArgumentParser(description="Check AiPay frontend theme contract.")
    parser.add_argument(
        "--mode",
        choices=("changed", "all"),
        default="changed",
        help="changed: only added lines in changed files; all: scan all Swift files",
    )
    args = parser.parse_args()

    files = changed_swift_files() if args.mode == "changed" else all_swift_files()
    files = [f for f in files if not should_skip(f)]

    if not files:
        print("[theme-check] no Swift files to check")
        return 0

    violations: list[Violation] = []
    for file_path in files:
        rel = str(file_path.relative_to(ROOT))
        skip_blue_color_check = rel in COLOR_SOURCE_OF_TRUTH_FILES
        for line_no, line in collect_lines_for_file(file_path, args.mode):
            violations.extend(
                check_line(
                    rel,
                    line_no,
                    line,
                    skip_blue_color_check=skip_blue_color_check,
                )
            )

    if violations:
        for item in violations:
            print(f"[theme-check] {item.path}:{item.line}: {item.message}")
        print(
            f"[theme-check] failed with {len(violations)} violation(s). "
            f"若确有必要可在同一行添加 `{ALLOW_MARK}`。"
        )
        return 1

    print(f"[theme-check] passed ({args.mode}), files={len(files)}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
