#!/usr/bin/env python3

from __future__ import annotations

import argparse
import re
import shutil
import zipfile
from pathlib import Path
from urllib.parse import unquote

NOTION_ID_PATTERN = re.compile(r"\s+[0-9a-f]{32}(?=(?:_all)?\.[^.]+$)")
LOCAL_LINK_PATTERN = re.compile(r"(!?)\[([^\]]*)\]\(([^)]+)\)")
INVALID_PATH_CHARS = re.compile(r"[<>:\"/\\|?*]+")

CATEGORY_NAMES = {
    "ssafy": "ssafy",
    "Infra / tool": "infra-tool",
    "공부 목록": "study",
    "책": "books",
    "cs": "cs",
    "영어": "english",
}


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Convert a flat Notion Markdown & CSV export into a GitHub-friendly directory tree."
    )
    parser.add_argument("zip_path", type=Path, help="Notion export ZIP file")
    parser.add_argument("output_dir", type=Path, help="Destination directory")
    return parser.parse_args()


def clean_title(filename: str) -> str:
    name = NOTION_ID_PATTERN.sub("", filename)
    return Path(name).stem.strip() or "untitled"


def slug(value: str) -> str:
    value = INVALID_PATH_CHARS.sub("-", value.strip().lower())
    value = re.sub(r"\s+", "-", value)
    value = re.sub(r"-+", "-", value).strip("-.")
    return value or "untitled"


def extract_id(value: str) -> str | None:
    match = re.search(r"([0-9a-f]{32})", value)
    return match.group(1) if match else None


def build_file_index(source_dir: Path) -> dict[str, Path]:
    index: dict[str, Path] = {}
    for path in source_dir.rglob("*"):
        if not path.is_file():
            continue
        page_id = extract_id(path.name)
        if page_id and not path.stem.endswith("_all"):
            index.setdefault(page_id, path)
    return index


def read_root_items(root_page: Path) -> list[tuple[str, str, str]]:
    section = "root"
    items: list[tuple[str, str, str]] = []

    for line in root_page.read_text(encoding="utf-8", errors="replace").splitlines():
        heading = re.match(r"^(#{1,2})\s+(.+)$", line)
        if heading:
            title = heading.group(2).strip().rstrip(".")
            if title.lower() != "about programming":
                section = title
            continue

        link = re.match(r"^\[([^\]]+)\]\(([^)]+)\)$", line.strip())
        if link and not link.group(2).startswith(("http://", "https://")):
            items.append((section, link.group(1), unquote(link.group(2))))

    return items


def rewrite_links(text: str, current_output: Path, page_paths: dict[str, Path]) -> str:
    def replace(match: re.Match[str]) -> str:
        image_marker, label, target = match.groups()
        decoded = unquote(target)

        if decoded.startswith(("http://", "https://", "#")):
            return match.group(0)

        page_id = extract_id(decoded)
        if page_id and page_id in page_paths:
            relative = Path(
                __import__("os").path.relpath(page_paths[page_id], current_output.parent)
            ).as_posix()
            return f"[{label}]({relative})"

        asset_name = clean_title(Path(decoded).name)
        relative_asset = f"assets/{slug(asset_name)}{Path(decoded).suffix.lower()}"
        return f"{image_marker}[{label}]({relative_asset})"

    return LOCAL_LINK_PATTERN.sub(replace, text)


def main() -> None:
    args = parse_args()
    zip_path = args.zip_path.resolve()
    output_dir = args.output_dir.resolve()
    work_dir = output_dir.parent / ".notion-export-work"

    if not zip_path.is_file():
        raise SystemExit(f"ZIP file not found: {zip_path}")

    shutil.rmtree(work_dir, ignore_errors=True)
    shutil.rmtree(output_dir, ignore_errors=True)
    work_dir.mkdir(parents=True)
    output_dir.mkdir(parents=True)

    with zipfile.ZipFile(zip_path) as archive:
        archive.extractall(work_dir)

    file_index = build_file_index(work_dir)
    root_id = "d520ce6c1e1d478ca8862a22c03150e6"
    root_page = file_index.get(root_id)
    if root_page is None:
        markdown_pages = sorted(work_dir.rglob("*.md"))
        if not markdown_pages:
            raise SystemExit("No Markdown pages were found in the export.")
        root_page = markdown_pages[0]

    root_items = read_root_items(root_page)
    page_paths: dict[str, Path] = {}

    for section, title, target in root_items:
        page_id = extract_id(target)
        if not page_id:
            continue
        category = CATEGORY_NAMES.get(section, slug(section))
        page_paths[page_id] = output_dir / category / slug(title) / "README.md"

    for page_id, source in file_index.items():
        destination = page_paths.get(page_id)
        if destination is None:
            destination = output_dir / "uncategorized" / slug(clean_title(source.name)) / "README.md"
            page_paths[page_id] = destination

        destination.parent.mkdir(parents=True, exist_ok=True)

        if source.suffix.lower() == ".md":
            body = source.read_text(encoding="utf-8", errors="replace")
            body = rewrite_links(body, destination, page_paths)
            destination.write_text(body.rstrip() + "\n", encoding="utf-8")
        elif source.suffix.lower() == ".csv":
            csv_target = destination.with_name("database.csv")
            shutil.copy2(source, csv_target)
            destination.write_text(
                f"# {clean_title(source.name)}\n\n"
                f"Database export: [database.csv](./database.csv)\n",
                encoding="utf-8",
            )

    for source in work_dir.rglob("*"):
        if not source.is_file() or source.suffix.lower() in {".md", ".csv"}:
            continue
        asset_dir = output_dir / "assets"
        asset_dir.mkdir(exist_ok=True)
        target = asset_dir / f"{slug(clean_title(source.name))}{source.suffix.lower()}"
        counter = 2
        while target.exists():
            target = asset_dir / f"{slug(clean_title(source.name))}-{counter}{source.suffix.lower()}"
            counter += 1
        shutil.copy2(source, target)

    readme_lines = [
        "# Notion Record",
        "",
        "Notion `About programming` 페이지에서 변환한 기록입니다.",
        "",
        "## Directories",
        "",
    ]
    for directory in sorted(path for path in output_dir.iterdir() if path.is_dir()):
        readme_lines.append(f"- [{directory.name}](./{directory.name}/)")
    (output_dir / "README.md").write_text("\n".join(readme_lines) + "\n", encoding="utf-8")

    shutil.rmtree(work_dir, ignore_errors=True)
    print(f"Migration complete: {output_dir}")


if __name__ == "__main__":
    main()
