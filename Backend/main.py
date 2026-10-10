import subprocess
import sys
from pathlib import Path

result = subprocess.run([sys.executable, "test_image_caption_link.py"], capture_output=False, text=True)
if result.returncode != 0:
    print(f"Error running image to text script: {result.stderr}", file=sys.stderr)
    sys.exit(result.returncode)
result = subprocess.run([sys.executable, "send_results_to_ai.py"], capture_output=False, text=True)
if result.returncode != 0:
    print(f"Error running send results to AI script: {result.stderr}", file=sys.stderr)
    sys.exit(result.returncode)
result = subprocess.run([sys.executable, "toCalendar.py"], capture_output=False, text=True)
if result.returncode != 0:
    print(f"Error running toCalendar script: {result.stderr}", file=sys.stderr)
    sys.exit(result.returncode)
