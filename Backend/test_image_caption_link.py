import json
import time
from pathlib import Path

from scraper import run_scraper

accounts_path = Path(__file__).with_name("accounts.txt")
usernames = []
if accounts_path.exists():
    usernames = [
        line.strip().lstrip("\ufeff")
        for line in accounts_path.read_text(encoding="utf-8-sig").splitlines()
        if line.strip() and not line.strip().startswith("#")
    ]

if not usernames:
    usernames = ["nasa"]

all_results = []
for index, username in enumerate(usernames, start=1):
    try:
        results = run_scraper(username=username)
        for item in results:
            if not item.get("image_url") and not item.get("caption") and not item.get("post_url"):
                continue
            record = {
                "username": username,
                "image_url": item.get("image_url"),
                "caption": item.get("caption"),
                "post_url": item.get("post_url"),
                "post_date": item.get("post_date"),
            }
            all_results.append(record)
            print(json.dumps(record, ensure_ascii=True))
    except Exception as exc:
        print(f"Skipping '{username}' due to error: {exc}")
        continue

    if index < len(usernames):
        time.sleep(2)

output_path = Path(__file__).with_name("results.json")
output_path.write_text(json.dumps(all_results, ensure_ascii=False, indent=2), encoding="utf-8")
print(f"Saved {len(all_results)} results to {output_path}")
