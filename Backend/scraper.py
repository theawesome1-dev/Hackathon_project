import argparse
import json
import os
import sys
import time
from pathlib import Path
from typing import Any

from apify_client import ApifyClient


ACTOR_ID = "shu8hvrXbJbY3Eb9W"


def load_env_file() -> None:
    env_path = Path(__file__).with_name(".env")
    if not env_path.exists():
        return

    for raw_line in env_path.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue

        key, value = [part.strip() for part in line.split("=", 1)]
        os.environ.setdefault(key, value)


def build_run_input(
    username: str = "nasa",
) -> dict[str, Any]:
    return {
        "resultsType": "posts",
        "directUrls": [f"https://www.instagram.com/{username.strip('@')}/"],
        "resultsLimit": 1,
        "addParentData": False,
    }


def extract_post_fields(items: list[dict[str, Any]]) -> list[dict[str, str | None]]:
    extracted: list[dict[str, str | None]] = []
    for item in items:
        post_type = str(item.get("type", "")).lower()
        product_type = str(item.get("productType", "")).lower()
        is_video = "video" in post_type or bool(item.get("videoUrl")) or product_type == "clips"

        images = item.get("images")
        image_url = None
        if not is_video:
            image_url = (
                item.get("displayUrl")
                or item.get("imageUrl")
                or item.get("image_url")
                or (images[0] if isinstance(images, list) and images else None)
            )
        caption = item.get("caption")
        post_url = item.get("url") or item.get("post_url")
        timestamp = item.get("timestamp")

        if not image_url and not caption and not post_url:
            continue

        extracted.append(
            {
                "image_url": image_url,
                "caption": caption,
                "post_url": post_url,
                "post_date": str(timestamp) if timestamp is not None else None,
            }
        )
    return extracted


def run_scraper(username: str = "nasa") -> list[dict[str, str | None]]:
    load_env_file()
    api_token = os.getenv("APIFY_API_TOKEN")
    if not api_token:
        raise RuntimeError("Set the APIFY_API_TOKEN environment variable before running the scraper.")

    client = ApifyClient(api_token)
    run = client.actor(ACTOR_ID).call(
        run_input=build_run_input(username=username)
    )

    dataset_id = getattr(run, "default_dataset_id", None)
    if not dataset_id:
        raise RuntimeError(f"Actor run did not return a dataset ID: {run.model_dump()}")

    raw_items = list(client.dataset(dataset_id).iterate_items())
    return extract_post_fields(raw_items)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Run the Instagram scraper actor for a username or a list of usernames from accounts.txt.")
    parser.add_argument("--username", default=None, help="Instagram username to scrape.")
    parser.add_argument("--accounts-file", default="accounts.txt", help="Text file with one Instagram username per line.")
    return parser.parse_args()


def load_usernames(accounts_file: str) -> list[str]:
    path = Path(__file__).with_name(accounts_file)
    if not path.exists():
        raise FileNotFoundError(f"Accounts file not found: {path}")

    usernames = []
    for raw_line in path.read_text(encoding="utf-8-sig").splitlines():
        username = raw_line.strip().lstrip("\ufeff")
        if username and not username.startswith("#"):
            usernames.append(username)
    return usernames


def main() -> None:
    args = parse_args()

    try:
        usernames = [args.username] if args.username else load_usernames(args.accounts_file)
        if not usernames:
            raise ValueError("No usernames provided. Add one per line in accounts.txt or pass --username.")

        all_results: list[dict[str, Any]] = []
        for index, username in enumerate(usernames, start=1):
            print(f"Running scraper for '{username}' ({index}/{len(usernames)})...")
            try:
                results = run_scraper(username=username)
                print(f"Found {len(results)} result(s) for {username}")
                for item in results:
                    item_with_username = {"username": username, **item}
                    all_results.append(item_with_username)
                    print(item_with_username)
            except Exception as exc:
                print(f"Skipping '{username}' because of a scraper error: {exc}", file=sys.stderr)
                continue

            time.sleep(2)

        output_path = Path(__file__).with_name("results.json")
        output_path.write_text(json.dumps(all_results, ensure_ascii=False, indent=2), encoding="utf-8")
        print(f"Saved {len(all_results)} aggregated result(s) to {output_path}")
    except Exception as exc:
        print(f"Error: {exc}", file=sys.stderr)
        raise


if __name__ == "__main__":
    main()
