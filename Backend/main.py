import json
import subprocess
import sys
from contextlib import asynccontextmanager

import uvicorn
from fastapi import FastAPI
from fastapi.responses import JSONResponse


def run_script(name: str, required: bool = True):
    result = subprocess.run(
        [sys.executable, name],
        capture_output=True,   # needed so result.stderr has content
        text=True,
    )
    print(result.stdout)
    if result.returncode != 0:
        print(f"Error running {name}: {result.stderr}", file=sys.stderr)
        if required:
            sys.exit(result.returncode)


@asynccontextmanager
async def lifespan(app: FastAPI):
    # runs once when the server starts
    run_script("test_image_caption_link.py")
    run_script("send_results_to_ai.py")
    run_script("toCalendar.py", required=False)
    yield


# 1. Initialize app FIRST
app = FastAPI(lifespan=lifespan)


# 2. Define routes AFTER app is created
@app.get("/data")
def get_data():
    with open("ai_analysis.json") as f:
        return JSONResponse(content=json.load(f))


@app.get("/refresh")
def refresh_data():
    # Re-run your backend collection/AI scripts when requested
    run_script("test_image_caption_link.py")
    run_script("send_results_to_ai.py")
    run_script("toCalendar.py", required=False)

    # Return the newly analyzed data
    with open("ai_analysis.json") as f:
        return JSONResponse(content=json.load(f))


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)