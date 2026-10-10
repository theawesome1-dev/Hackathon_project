import json
import subprocess
import sys
from contextlib import asynccontextmanager
from pathlib import Path


from fastapi import FastAPI, HTTPException
from fastapi.responses import JSONResponse
import uvicorn


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
    # run_script("test_image_caption_link.py")
    # run_script("send_results_to_ai.py")
    yield


# 1. Initialize app FIRST
app = FastAPI(lifespan=lifespan)




def select_event_response(analysis_data):
    if isinstance(analysis_data, dict):
        return [analysis_data] if analysis_data.get("title") else []

    if isinstance(analysis_data, list):
        events = []
        for record in analysis_data:
            if not isinstance(record, dict):
                continue
            analysis = record.get("analysis")
            if isinstance(analysis, dict) and analysis.get("title"):
                events.append(analysis)
        return events

    raise HTTPException(status_code=500, detail="AI analysis has an unsupported JSON format.")


def get_event_response():
    analysis_path = Path(__file__).with_name("ai_analysis.json")
    try:
        analysis_data = json.loads(analysis_path.read_text(encoding="utf-8"))
    except FileNotFoundError as exc:
        raise HTTPException(status_code=404, detail="No AI analysis is available yet.") from exc
    except json.JSONDecodeError as exc:
        raise HTTPException(
            status_code=503,
            detail="AI analysis is invalid. Call /refresh to generate it again.",
        ) from exc
    return select_event_response(analysis_data)


# 2. Define routes AFTER app is created
@app.get("/data")
def get_data():
    return JSONResponse(content=get_event_response())



@app.get("/refresh")
def refresh_data():
    # Re-run your backend collection/AI scripts when requested
    run_script("test_image_caption_link.py")
    run_script("send_results_to_ai.py")

    return JSONResponse(content=get_event_response())


from typing import List
from pydantic import BaseModel

class AccountsRequest(BaseModel):
    accounts: List[str]

@app.post("/update")
def update_accounts(request: AccountsRequest):
    accounts_path = Path(__file__).with_name("accounts.txt")
    accounts_text = "\n".join(account.strip() for account in request.accounts)
    if request.accounts:
        accounts_text += "\n"
    accounts_path.write_text(accounts_text, encoding="utf-8")

    return {
        "status": "success", 
        "message": f"Successfully replaced accounts.txt with {len(request.accounts)} accounts"
    }


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)