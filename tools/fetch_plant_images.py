#!/usr/bin/env python3
"""Build-time downloader for open-licensed, species-matched plant image assets.

Only CC0/public-domain and CC BY/CC BY-SA images are used. NC and ND licenses
are rejected because assets are redistributed and converted for offline use.
"""
from __future__ import annotations

import argparse
import concurrent.futures
import html
import io
import json
import re
import sys
import time
from pathlib import Path
from typing import Any
from urllib.error import HTTPError, URLError
from urllib.parse import quote, urlencode
from urllib.request import Request, urlopen

from PIL import Image, ImageOps, UnidentifiedImageError

ROOT = Path(__file__).resolve().parents[1]
DEFAULT_INVENTORY = ROOT / "app/src/main/assets/plant-image-inventory.json"
DEFAULT_ASSETS = ROOT / "app/src/main/assets/plants"
DEFAULT_MANIFEST = ROOT / "app/src/main/assets/plant-image-manifest.json"
USER_AGENT = "Giyahnameh-OfflineBotanicalEncyclopedia/0.1 (open-license educational app)"
MAX_IMAGE_BYTES = 20 * 1024 * 1024
MAX_EDGE = 640


def http_bytes(url: str, timeout: int = 20, retries: int = 3) -> bytes:
    last_error: Exception | None = None
    for attempt in range(retries):
        request = Request(url, headers={
            "User-Agent": USER_AGENT,
            "Accept": "application/json, image/*, */*",
        })
        try:
            with urlopen(request, timeout=timeout) as response:
                body = response.read(MAX_IMAGE_BYTES + 1)
                if len(body) > MAX_IMAGE_BYTES:
                    raise ValueError("response exceeded image size limit")
                return body
        except (HTTPError, URLError, TimeoutError, ValueError) as exc:
            last_error = exc
            if isinstance(exc, HTTPError) and exc.code not in (408, 425, 429, 500, 502, 503, 504):
                break
            if attempt + 1 < retries:
                time.sleep(0.7 * (attempt + 1))
    raise RuntimeError(f"request failed: {last_error}") from last_error


def clean_html(value: Any) -> str:
    text = html.unescape(str(value or ""))
    text = re.sub(r"<[^>]+>", " ", text)
    return re.sub(r"\s+", " ", text).strip()


def taxon_phrase(scientific_name: str) -> str:
    value = scientific_name.split(" (", 1)[0].replace("×", " ")
    tokens = re.findall(r"[A-Za-zÀ-ÖØ-öø-ÿ-]+", value)
    return f"{tokens[0]} {tokens[1]}" if len(tokens) >= 2 else value.strip()


def normalized_phrase(value: str) -> str:
    return re.sub(r"\s+", " ", value.replace("×", " ")).strip().casefold()


def image_license_allowed(license_value: str, creator: str) -> bool:
    value = (license_value or "").strip().casefold()
    if not value:
        return False
    if any(term in value for term in ("-nc", "noncommercial", "non-commercial", "-nd", "no-derivatives")):
        return False
    public_domain = any(term in value for term in (
        "public domain", "publicdomain/zero", "creativecommons.org/publicdomain/zero", "cc0", "cc zero"
    ))
    attribution_license = any(term in value for term in (
        "creativecommons.org/licenses/by/", "creativecommons.org/licenses/by-sa/", "cc by ", "cc by-sa"
    ))
    return public_domain or (attribution_license and bool(creator.strip()))


def matching_taxon(record: dict[str, Any], target_name: str) -> bool:
    target = normalized_phrase(taxon_phrase(target_name))
    observed = (
        record.get("species", ""),
        record.get("acceptedScientificName", ""),
        record.get("scientificName", ""),
    )
    return any(target == normalized_phrase(taxon_phrase(str(value))) for value in observed if value)


def gbif_candidates(scientific_name: str) -> list[dict[str, str]]:
    query = urlencode({
        "scientificName": taxon_phrase(scientific_name),
        "mediaType": "StillImage",
        "limit": "18",
    })
    data = json.loads(http_bytes("https://api.gbif.org/v1/occurrence/search?" + query).decode("utf-8"))
    candidates: list[dict[str, str]] = []
    for record in data.get("results", []):
        if not matching_taxon(record, scientific_name):
            continue
        for media in record.get("media") or []:
            image_url = str(media.get("identifier") or "").strip()
            source_url = str(media.get("references") or "").strip()
            license_value = str(media.get("license") or "").strip()
            creator = str(media.get("creator") or media.get("rightsHolder") or record.get("recordedBy") or "").strip()
            if not image_url.startswith("https://") or not source_url.startswith("https://"):
                continue
            if not image_license_allowed(license_value, creator):
                continue
            candidates.append({
                "imageUrl": image_url,
                "sourceUrl": source_url,
                "license": license_value,
                "creator": creator or "خالق اثر در رکورد اصلی ذکر نشده (مجوز CC0/مالکیت عمومی)",
                "source": "GBIF",
                "recordScientificName": str(record.get("acceptedScientificName") or record.get("scientificName") or ""),
            })
    return candidates


def commons_candidates(scientific_name: str) -> list[dict[str, str]]:
    phrase = taxon_phrase(scientific_name)
    query = urlencode({
        "action": "query",
        "format": "json",
        "generator": "search",
        "gsrsearch": f'"{phrase}"',
        "gsrnamespace": "6",
        "gsrlimit": "15",
        "prop": "imageinfo",
        "iiprop": "url|extmetadata",
        "iiurlwidth": str(MAX_EDGE),
    })
    data = json.loads(http_bytes("https://commons.wikimedia.org/w/api.php?" + query).decode("utf-8"))
    pages = (data.get("query") or {}).get("pages") or {}
    candidates: list[dict[str, str]] = []
    phrase_normalized = normalized_phrase(phrase)
    for page in pages.values():
        title = str(page.get("title") or "")
        info_list = page.get("imageinfo") or []
        if not info_list:
            continue
        info = info_list[0]
        metadata = info.get("extmetadata") or {}
        description = clean_html((metadata.get("ImageDescription") or {}).get("value", ""))
        if phrase_normalized not in normalized_phrase(title + " " + description):
            continue
        license_value = clean_html((metadata.get("LicenseShortName") or {}).get("value", ""))
        license_url = clean_html((metadata.get("LicenseUrl") or {}).get("value", ""))
        creator = clean_html((metadata.get("Artist") or {}).get("value", "")) or clean_html(
            (metadata.get("Attribution") or {}).get("value", "")
        )
        if not image_license_allowed(license_value + " " + license_url, creator):
            continue
        image_url = str(info.get("thumburl") or info.get("url") or "").strip()
        if not image_url.startswith("https://"):
            continue
        page_url = "https://commons.wikimedia.org/wiki/" + quote(title.replace(" ", "_"), safe=":_()")
        candidates.append({
            "imageUrl": image_url,
            "sourceUrl": page_url,
            "license": license_value + (f" ({license_url})" if license_url else ""),
            "creator": creator or "خالق اثر در فرادادهٔ فایل مشخص نشده",
            "source": "Wikimedia Commons",
            "recordScientificName": phrase,
        })
    return candidates


def normalize_image(body: bytes, output_path: Path) -> tuple[int, int, int]:
    try:
        with Image.open(io.BytesIO(body)) as source:
            image = ImageOps.exif_transpose(source)
            image.thumbnail((MAX_EDGE, MAX_EDGE), Image.Resampling.LANCZOS)
            if image.mode not in ("RGB", "RGBA"):
                image = image.convert("RGBA" if "transparency" in image.info else "RGB")
            output_path.parent.mkdir(parents=True, exist_ok=True)
            image.save(output_path, "WEBP", quality=82, method=5)
            return image.width, image.height, output_path.stat().st_size
    except (UnidentifiedImageError, OSError) as exc:
        raise ValueError(f"download is not a supported image: {exc}") from exc


def load_previous_manifest(path: Path) -> dict[str, Any]:
    try:
        value = json.loads(path.read_text(encoding="utf-8"))
        return value if isinstance(value, dict) else {}
    except (FileNotFoundError, json.JSONDecodeError, OSError):
        return {}


def process_plant(
    plant: dict[str, str], assets_dir: Path, previous: dict[str, Any]
) -> tuple[str, dict[str, Any]]:
    plant_id = plant["id"]
    asset_rel = f"plants/{plant_id}.webp"
    asset_path = assets_dir / f"{plant_id}.webp"
    old = previous.get(plant_id) or {}
    if asset_path.is_file() and old.get("assetPath") == asset_rel and old.get("licenseVerified") is True:
        return plant_id, old

    scientific_name = plant["scientificName"]
    candidates: list[dict[str, str]] = []
    errors: list[str] = []
    try:
        candidates = gbif_candidates(scientific_name)
    except Exception as exc:
        errors.append(f"GBIF: {type(exc).__name__}")
    if not candidates:
        try:
            candidates = commons_candidates(scientific_name)
        except Exception as exc:
            errors.append(f"Commons: {type(exc).__name__}")

    for candidate in candidates:
        try:
            body = http_bytes(candidate["imageUrl"], timeout=25)
            width, height, size = normalize_image(body, asset_path)
            return plant_id, {
                "plantId": plant_id,
                "persianName": plant["persianName"],
                "scientificName": scientific_name,
                "assetPath": asset_rel,
                "altTextFa": f"تصویر {plant['persianName']} ({scientific_name})",
                "creator": candidate["creator"],
                "license": candidate["license"],
                "licenseVerified": True,
                "source": candidate["source"],
                "sourcePageUrl": candidate["sourceUrl"],
                "recordScientificName": candidate["recordScientificName"],
                "width": width,
                "height": height,
                "bytes": size,
                "imageAdjusted": "اندازه برای نمایش کوچک شده و فایل به WebP تبدیل شده است",
            }
        except Exception as exc:
            errors.append(f"image: {type(exc).__name__}")
            asset_path.unlink(missing_ok=True)

    return plant_id, {
        "plantId": plant_id,
        "persianName": plant["persianName"],
        "scientificName": scientific_name,
        "status": "unavailable",
        "licenseVerified": False,
        "errors": errors[-5:],
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--inventory", type=Path, default=DEFAULT_INVENTORY)
    parser.add_argument("--assets-dir", type=Path, default=DEFAULT_ASSETS)
    parser.add_argument("--manifest", type=Path, default=DEFAULT_MANIFEST)
    parser.add_argument("--limit", type=int, default=0, help="Only process the first N entries for diagnostics")
    parser.add_argument("--allow-missing", action="store_true", help="Development-only mode while image coverage is being improved")
    args = parser.parse_args()

    plants = json.loads(args.inventory.read_text(encoding="utf-8"))
    if not isinstance(plants, list) or len(plants) != 500:
        size = len(plants) if isinstance(plants, list) else "invalid"
        raise SystemExit(f"Expected exactly 500 plants, got {size}")
    ids = [str(item.get("id", "")) for item in plants]
    if any(not item for item in ids) or len(ids) != len(set(ids)):
        raise SystemExit("Inventory contains empty or duplicate plant IDs")

    previous = load_previous_manifest(args.manifest)
    selected = plants[:args.limit] if args.limit > 0 else plants
    results: dict[str, dict[str, Any]] = {}
    with concurrent.futures.ThreadPoolExecutor(max_workers=5) as pool:
        futures = [pool.submit(process_plant, item, args.assets_dir, previous) for item in selected]
        for number, future in enumerate(concurrent.futures.as_completed(futures), start=1):
            plant_id, metadata = future.result()
            results[plant_id] = metadata
            if number % 25 == 0 or number == len(selected):
                good = sum(item.get("licenseVerified") is True for item in results.values())
                print(f"Processed {number}/{len(selected)}; accepted licensed images: {good}")

    if args.limit > 0:
        merged = dict(previous)
        merged.update(results)
        results = merged
    else:
        results = {
            item["id"]: results.get(item["id"], previous.get(item["id"], {
                "plantId": item["id"],
                "persianName": item["persianName"],
                "scientificName": item["scientificName"],
                "status": "unavailable",
                "licenseVerified": False,
            }))
            for item in plants
        }

    args.assets_dir.mkdir(parents=True, exist_ok=True)
    args.manifest.parent.mkdir(parents=True, exist_ok=True)
    args.manifest.write_text(json.dumps(results, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    available = [
        item for item in plants
        if results.get(item["id"], {}).get("licenseVerified") is True
        and (args.assets_dir / f"{item['id']}.webp").is_file()
    ]
    missing = [item for item in plants if item not in available]
    print(f"Offline images with accepted licence: {len(available)}/500")
    print(f"Missing or unverified: {len(missing)}")
    if missing:
        print("Examples:", ", ".join(f"{item['id']} ({item['scientificName']})" for item in missing[:20]))
    if missing and not args.allow_missing:
        print("Strict mode: refusing to treat the image catalogue as complete.", file=sys.stderr)
        return 2
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
