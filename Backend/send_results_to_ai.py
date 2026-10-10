import argparse
import base64
from io import BytesIO
import json
import os
import sys
from pathlib import Path
from typing import Any
from urllib.error import HTTPError, URLError
from urllib.parse import urlsplit
from urllib.request import Request, urlopen

from PIL import Image, ImageOps, UnidentifiedImageError


DEFAULT_API_URL = "https://api.openai.com/v1/chat/completions"
DEFAULT_MODEL = "gpt-4o-mini"
DEFAULT_IMAGE_MAX_SIDE = 1600
DEFAULT_IMAGE_JPEG_QUALITY = 85


def load_env_file() -> None:
    env_path = Path(__file__).with_name(".env")
    if not env_path.exists():
        return

    for raw_line in env_path.read_text(encoding="utf-8-sig").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue

        key, value = [part.strip() for part in line.split("=", 1)]
        if len(value) >= 2 and value[0] == value[-1] and value[0] in "\"'":
            value = value[1:-1]
        os.environ.setdefault(key, value)


def resolve_path(path_value: str) -> Path:
    path = Path(path_value)
    return path if path.is_absolute() else Path(__file__).with_name(path_value)


def load_prompt(path: Path) -> str:
    if not path.exists():
        raise FileNotFoundError(f"Prompt file not found: {path}")

    prompt = path.read_text(encoding="utf-8").strip()
    if not prompt:
        raise ValueError(f"Prompt file is empty: {path}")
    return prompt


def load_results(path: Path) -> list[dict[str, Any]]:
    if not path.exists():
        raise FileNotFoundError(f"Results file not found: {path}")

    results = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(results, list):
        raise ValueError("The results JSON must contain a list of post records.")
    if not results:
        raise ValueError(f"No results to send; {path} is empty.")
    return results


def encode_resized_image(image_url: str) -> str:
    if urlsplit(image_url).scheme != "https":
        raise ValueError("Post image URLs must use HTTPS.")

    image_request = Request(image_url, headers={"User-Agent": "Mozilla/5.0"})
    with urlopen(image_request, timeout=30) as response:
        image_data = response.read(15 * 1024 * 1024 + 1)

    if len(image_data) > 15 * 1024 * 1024:
        raise ValueError("Post image exceeds the 15 MB download limit.")

    try:
        with Image.open(BytesIO(image_data)) as source_image:
            image = ImageOps.exif_transpose(source_image).convert("RGB")
    except (UnidentifiedImageError, OSError) as exc:
        raise ValueError("Could not decode the post image.") from exc

    max_side = int(os.getenv("AI_IMAGE_MAX_SIDE", DEFAULT_IMAGE_MAX_SIDE))
    jpeg_quality = int(os.getenv("AI_IMAGE_JPEG_QUALITY", DEFAULT_IMAGE_JPEG_QUALITY))
    if max_side < 256:
        raise ValueError("AI_IMAGE_MAX_SIDE must be at least 256 pixels.")
    if not 1 <= jpeg_quality <= 95:
        raise ValueError("AI_IMAGE_JPEG_QUALITY must be between 1 and 95.")

    image.thumbnail((max_side, max_side), Image.Resampling.LANCZOS)
    output = BytesIO()
    image.save(output, format="JPEG", quality=jpeg_quality, optimize=True)
    return base64.b64encode(output.getvalue()).decode("ascii")


def send_to_ai(result: dict[str, Any], prompt: str) -> str:
    load_env_file()
    api_key = os.getenv("AI_KEY")
    if not api_key:
        raise RuntimeError("AI_KEY is missing. Add AI_KEY=<your API key> to Backend/.env.")

    api_url = os.getenv("AI_API_URL", DEFAULT_API_URL)
    model = os.getenv("AI_MODEL", DEFAULT_MODEL)
    result_text = {key: value for key, value in result.items() if key != "image_url"}
    message_content: list[dict[str, Any]] = [
        {
            "type": "text",
            "text": f"{prompt}\n\nIndividual scraped post result:\n{json.dumps(result_text, ensure_ascii=False, indent=2)}",
        }
    ]
    image_url = result.get("image_url")
    if image_url:
        resized_image = encode_resized_image(image_url)
        message_content.append(
            {
                "type": "image_url",
                "image_url": {
                    "url": f"data:image/jpeg;base64,{resized_image}",
                    "detail": "high",
                },
            }
        )

    request_body = {
        "model": model,
        "messages": [
            {
                "role": "user",
                "content": message_content,
            }
        ],
    }
    request = Request(
        api_url,
        data=json.dumps(request_body).encode("utf-8"),
        headers={
            "Authorization": f"Bearer {api_key}",
            "Content-Type": "application/json",
        },
        method="POST",
    )

    try:
        with urlopen(request, timeout=90) as response:
            response_data = json.loads(response.read().decode("utf-8"))
    except HTTPError as exc:
        error_body = exc.read().decode("utf-8", errors="replace")
        raise RuntimeError(f"AI API returned HTTP {exc.code}: {error_body}") from exc
    except URLError as exc:
        raise RuntimeError(f"Could not reach the AI API: {exc.reason}") from exc

    try:
        return response_data["choices"][0]["message"]["content"]
    except (KeyError, IndexError, TypeError) as exc:
        raise RuntimeError("AI API response did not contain a chat completion.") from exc


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Send saved Instagram scraper results to an AI chat-completions API."
    )
    parser.add_argument("--results-file", default="results.json", help="JSON results file in Backend.")
    parser.add_argument("--output-file", default="ai_analysis.txt", help="Where to save the AI response.")
    parser.add_argument("--prompt-file", default="ai_prompt.txt", help="Text file containing the AI instructions.")
    parser.add_argument("--prompt", default=None, help="Override the prompt file for this run.")
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    try:
        results_path = resolve_path(args.results_file)
        output_path = resolve_path(args.output_file)
        results = load_results(results_path)
        prompt = args.prompt if args.prompt is not None else load_prompt(resolve_path(args.prompt_file))
        sections = []
        for index, result in enumerate(results, start=1):
            print(f"Sending result {index}/{len(results)} to AI...")
            answer = send_to_ai(result, prompt)
            label = f"Result {index} - {result.get('username', 'unknown')}"
            post_url = result.get("post_url")
            if post_url:
                label += f" ({post_url})"
            sections.append(f"{label}\n{answer}")

        output_path.write_text("\n\n".join(sections) + "\n", encoding="utf-8")
        print("\n\n".join(sections))
        print(f"\nSaved AI response to {output_path}")
    except Exception as exc:
        print(f"Error: {exc}", file=sys.stderr)
        raise SystemExit(1) from exc


if __name__ == "__main__":
    main()
